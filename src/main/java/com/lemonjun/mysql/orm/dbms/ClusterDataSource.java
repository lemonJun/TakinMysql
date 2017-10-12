package com.lemonjun.mysql.orm.dbms;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lemonjun.mysql.orm.dbms.config.ClusterConfig;
import com.lemonjun.mysql.orm.dbms.config.DbConfig;
import com.lemonjun.mysql.orm.util.MessageAlert;

/**
 * 提供集群的数据源，用户无需了解集群的细节和状态。当更改真实的数据源属性，所以任何访问该数据源的代码都无需更改。
 * @author renjun
 *
 */
public class ClusterDataSource extends AbstractDataSource {
    protected static final Logger logger = LoggerFactory.getLogger(ClusterDataSource.class);

    /** 
     * 某服务的所有数据库连接池对象。
     * 这个为什么不直接做成  主对象  从对象呢
     */
    protected final List<DbDataSource> dbDataSources = new ArrayList<DbDataSource>();
    /**
     * 当前正在使用的数据库连接。
     */
    private volatile DbDataSource curDbDataSource;
    public final DbDataSource masterDbDataSource; // 主库
    public DbDataSource slaveDbDataSource; // 从库

    private volatile long readSwitchedTime = 0;
    private static final long READ_SLAVE_DB_TIME = 1000 * 60;
    public volatile DbDataSource currentReadDataSource = null;

    public final DbConfig masterConfig;

    private final ScheduledExecutorService respScheduler = new ScheduledThreadPoolExecutor(1);

    private AtomicBoolean isInit = new AtomicBoolean(false);
    public static List<ClusterDataSource> clusterList = new CopyOnWriteArrayList<ClusterDataSource>();
    private long curDbSwitchedTime = 0;

    //初始化集群连接池   根据配置 分别生成主从连接  此部分没有问题
    public ClusterDataSource(ClusterConfig dataSourceConfig) throws Exception {
        this.curDbDataSource = null;
        DbConfig mconfig = null;
        DbConfig sconfig = null;
        //默认第一个是主库  第二个是从库
        for (DbConfig dbConfig : dataSourceConfig.getDbConfigList()) {
            DbDataSource dbDataSource = new DbDataSource(dbConfig);
            dbDataSource.setName(dbConfig.getConnetionURL());
            DbMonitor monitor = new DbStateMonitor();
            dbDataSource.addMonitor(monitor);
            if (this.curDbDataSource == null) {
                curDbDataSource = dbDataSource;
                mconfig = dbConfig;
            } else {
                slaveDbDataSource = dbDataSource;
                sconfig = dbConfig;
            }
            dbDataSources.add(dbDataSource);
        }

        masterDbDataSource = curDbDataSource; // 2011-05-24 主库
        masterConfig = mconfig;
        logger.debug(String.format("init cluster datasource\n master:%s\n slave:%s", mconfig != null ? mconfig.getConnetionURL() : "null", sconfig != null ? sconfig.getConnetionURL() : "null"));
        if (masterDbDataSource == null) {
            throw new Exception("SWAP no DbConfig in " + dataSourceConfig.getName());
        }

        clusterList.add(this);
        if (isInit.compareAndSet(false, true)) {
            logger.debug("HaPlus ClusterDataSource init check ");
            respScheduler.scheduleAtFixedRate(checkAndChangeThread, 1000, 1000, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 获取一个只读连接
     */
    @Override
    public Connection GetReadConnection() throws SQLException {
        return getReadDataSource().getConnection();
    }

    /**
     * 获得数据库连接
     * 
     * @throws SQLException
     */
    @Override
    public Connection getConnection() throws SQLException {
        return this.getDataSource().getConnection();
    }

    //通过配置初始化连接池
    protected void addDataSource(DbDataSource dbDataSource) {
        if (dbDataSource == null)
            return;
        dbDataSources.add(dbDataSource);
    }

    /**
     * 此处逻辑存在问题  不知道是为了保证线上性能  还是不愿改 
     * 1 当知道从库的时候   直接返回
     * 2 当不确定从库的时候   遍历所有的数据库    并从中找出一个连接还未满的  这时候也没分是主库还是从库 
     * 
     * 
     * 不知道此处为什么这么重的逻辑   简单可信点不是挺好的吗
     * 有从库且从库可以有   直接读从库
     * 没从库有主库 且主库可以用  
     * 
     * @return
     * @throws SQLException
     */
    protected DbDataSource getReadDataSource() throws SQLException {
        long currentTime = System.currentTimeMillis();
        if (currentReadDataSource != null) { // 继续读从库
            if ((currentTime - readSwitchedTime) < READ_SLAVE_DB_TIME) {
                return currentReadDataSource;
            }
        }
        DbDataSource dataSourceread = null;
        for (int index = 0; index < dbDataSources.size(); index++) {
            DbDataSource dataSource = dbDataSources.get(index);
            if (dataSource == null) {
                continue;
            }
            // double check
            if (readSwitchedTime >= System.currentTimeMillis()) {
                return currentReadDataSource;
            }

            if (dataSource.isAlive() && (!dataSource.isFull())) {
                if (dataSource == this.masterDbDataSource) {
                    currentReadDataSource = null;
                } else {
                    logger.error("SWAP readable datasource TO " + dataSource);
                    currentReadDataSource = dataSource;
                    readSwitchedTime = currentTime;
                    MessageAlert.Factory.get().sendMessage("读切库" + dataSource.getConfig().getConnetionURL());
                }
                dataSourceread = dataSource;
            }
        }
        if (dataSourceread == null) {
            logger.debug("warning!warning! need swap to readonly datasource,but not");
            dataSourceread = masterDbDataSource;
        }
        if (dataSourceread != null) {
            return dataSourceread;
        }
        throw new SQLException("SWAP no available datasource. " + this, "08S01");
    }

    /**
     * 获得当前有效数据源首次获得DbDataSource设置为默认连接，每次返回DbDataSource之前都要执行一次
     * notNeedSwap(strategys)方法对每个切换策略的运行结果进行扫描。
     * 
     * @return
     * @throws Exception
     */
    protected DbDataSource getDataSource() {
        for (int index = 0; index < dbDataSources.size(); index++) {
            DbDataSource dataSource = dbDataSources.get(index);
            if (dataSource != null && dataSource.isAlive()) {
                if (dataSource != curDbDataSource) {
                    synchronized (this) {
                        if (dataSource != curDbDataSource) {
                            logger.error("SWAP datasource FROM " + curDbDataSource + " TO " + dataSource);
                            curDbDataSource = dataSource;
                            MessageAlert.Factory.get().sendMessage("切库" + dataSource.getConfig().getConnetionURL());
                        }
                    }
                }
                return curDbDataSource;
            }
        }

        logger.debug("SWAP no available datasource. but current datasource is " + curDbDataSource);
        return curDbDataSource;
    }

    /**
     * 此线程用来检查主从切换
     * 此处需要修改 要不会频繁的发生切库行为   至少应该做个计数
     * 
     * 
     */
    private Thread checkAndChangeThread = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                // 如果配置了从库 ，启用HA
                if (slaveDbDataSource != null) {
                    for (Iterator iterator = clusterList.iterator(); iterator.hasNext();) {
                        ClusterDataSource clusterDb = (ClusterDataSource) iterator.next();

                        // 从主库切到从库
                        if (((clusterDb.currentReadDataSource == null) && (!DbUtils.checkHostLive(DbUtils.getIpFromUrl(clusterDb.curDbDataSource.getName())) || !clusterDb.curDbDataSource.checkDbLiveForTimes()))) {
                            clusterDb.currentReadDataSource = clusterDb.slaveDbDataSource;
                            clusterDb.curDbDataSource = clusterDb.currentReadDataSource;
                            curDbSwitchedTime = System.currentTimeMillis();
                            logger.debug("HaPlus:读切库  checkAndChangeThread switched to slave curDbDataSource dbs , current is :" + clusterDb.curDbDataSource.toString());
                            MessageAlert.Factory.get().sendMessage("HaPlus: 读切库" + clusterDb.slaveDbDataSource.getConfig().getConnetionURL());
                        }

                        //从库切到主库
                        if ((clusterDb.currentReadDataSource != null) && (System.currentTimeMillis() - curDbSwitchedTime > READ_SLAVE_DB_TIME) && (DbUtils.checkHostLive(DbUtils.getIpFromUrl(clusterDb.masterDbDataSource.getName()))) && (clusterDb.masterDbDataSource.checkDbLiveForTimes())) {
                            clusterDb.currentReadDataSource = null;
                            clusterDb.curDbDataSource = clusterDb.masterDbDataSource;
                            logger.debug("HaPlus: 读恢复 , checkAndChangeThread switched to master  curDbDataSource dbs , current is :" + clusterDb.curDbDataSource.toString());
                            MessageAlert.Factory.get().sendMessage("HaPlus: 读恢复" + clusterDb.masterDbDataSource.getConfig().getConnetionURL());
                        }
                        logger.debug("HaPlus: checkAndChangeThread iterator dbs , current is :" + clusterDb.curDbDataSource.toString());
                    }
                }
                logger.debug("HaPlus: checkAndChangeThread check status is normal  , cluster size is : " + clusterList.size());
            } catch (Exception e) {
                logger.error("checkAndChange Errors " + e.getMessage(), e);
            }
        }
    });

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(" real datasources count: ").append(this.dbDataSources.size()).append(", current datasource: ").append(this.curDbDataSource == null ? "null" : curDbDataSource);
        return sb.toString();
    }

}
