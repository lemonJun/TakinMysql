package com.lemonjun.mysql.orm.dbms;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TransferQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lemonjun.mysql.orm.dbms.config.DbConfig;
import com.lemonjun.mysql.orm.util.MessageAlert;

/**
 * 对应一个具体的数据库连接池
 * 之前出现的数据库连接池错误   主要出现在此类中   这个类应该使用condition+阻塞队列做一个限制的  
 * 要不就无法实现等待超时 和 动态调整大小的功能
 * 分别用了两种锁  来新建连接 和 记数  这种方式显然不好
 * 读写锁: 读读可以共存  写读不能共存  读读不能共存
 * 
 * @author lemon
 * 
 */
public class DbDataSource extends AbstractDataSource {

    private static final Logger logger = LoggerFactory.getLogger(DbDataSource.class);

    private String name = "";

    private DbConfig config;

    private final List<DbMonitor> monitors = new ArrayList<DbMonitor>();

    private volatile boolean isAlive = true;

    /** set to true if the connection pool has been flagged as shutting down. */
    protected volatile boolean isShutDown = false;

    /** Connections available to be taken */
    private TransferQueue<ConnectionWrapper> freeConnections;

    /** Prevent repeated termination of all connections when the DB goes down. */
    protected Lock terminationLock = new ReentrantLock();

    protected ReentrantReadWriteLock statsLock = new ReentrantReadWriteLock();//读写锁
    private int size = 0;//已经创建的连接数 

    protected String checkLiveSQL = "SELECT 1";

    // 上次回收时间
    private volatile long lastReleaseTime = 0;
    // 回收间隔
    private volatile static long releaseInterval = 10 * 60 * 1000;

    private static int releaseStrategyValve = 10;

    private AtomicLong creatingConnCount = new AtomicLong(0);
    private static int MAX_CREATING_THREADS = 20;
    private AtomicBoolean isInit = new AtomicBoolean(false);

    private final ReentrantLock releaseLock = new ReentrantLock();

    private final ExecutorService releaseExecutor = new ThreadPoolExecutor(1, 1, 1500, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

    /**
     * 初始化操作  先创建最小的连接池数
     * @param config
     * @param monitor
     */
    public DbDataSource(DbConfig config, DbMonitor monitor) {
        releaseInterval = 9090L;
        this.config = config;
        try {
            freeConnections = new LinkedTransferQueue<ConnectionWrapper>();
            LoadDrivers();
            registerExcetEven();
            addMonitor(monitor);

            int min = config.getMinPoolSize();
            releaseInterval = config.getReleaseInterval() * 1000;
            releaseStrategyValve = config.getReleaseStrategyValve();
            min = min < 1 ? 1 : min;

            if (isInit.compareAndSet(false, true)) {
                MAX_CREATING_THREADS = MessageAlert.Factory.getMaxThreadsPerDs();
                logger.info("HaPlus : MAX_CREATING_THREADS is : " + MAX_CREATING_THREADS);
            }

            for (int index = 0; index < min; index++) {
                Connection connection = createConnection();
                connection.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("init DbDataSource error" + e);
            this.isAlive = false;
        }

    }

    public DbDataSource(DbConfig config) {
        this(config, null);
    }

    /**
     * shutdown the datasource
     */
    public synchronized void shutdown() {
        logger.info("-->connectinpoll shutdown " + this.isShutDown + "\n\n");
        if (this.isShutDown)
            return;

        logger.info("Shutting down connection pool...");
        this.isShutDown = true;

        for (int index = 0; index < this.getMonitors().size(); index++) {
            DbMonitor monitor = this.getMonitors().get(index);
            monitor.onShutDown(this);
        }

        terminateAllConnections();
        logger.info("Connection pool has been shutdown.");

    }

    /**
     * 判断数据库连接池是否满  并发情况下  这个判断会出问题
     * 这个要用Condition对象才对啊
     * @return
     */
    public boolean isFull() {
        int min = config.getMinPoolSize();
        int max = config.getMaxPoolSize();

        min = min > 0 ? min : 1;
        max = max > min ? max : min;

        int totalSize = this.getSize();
        int freeSize = this.getAvailableConnections();

        return ((totalSize - freeSize) >= max) ? true : false;
    }

    /**
     * 
     * 断定数据库是活的
     * 
     * @throws SQLException
     */
    private void assertLive() throws SQLException {
        logger.info(String.format("isalive:%s isshutdown:%s", isAlive, isShutDown));
        if ((!isAlive) || isShutDown) {
            throw new SQLException("SWAP " + this.getName() + " db connection pool is no alive or shutdown!");
        }
    }

    /**
     * 是线程安全的
     * 
     */
    @Override
    public Connection getConnection() throws SQLException {
        try {
            assertLive();
            ConnectionWrapper connection = this.freeConnections.poll();
            if (connection == null) {
                synchronized (freeConnections) {
                    if (isFull()) {//满就别创建了   直接等待吧  之前是直接抛出异常 这样肯定是不对的
                        connection = this.freeConnections.poll(config.getQueryTimeout(), TimeUnit.MILLISECONDS);
                    }
                    connection = this.freeConnections.poll();
                    if (connection == null) {
                        assertLive();
                        connection = createConnection();
                    }
                }
            }
            connection.renew();
            connection.setReadOnly(config.isReadonly());//只读
            for (int index = 0; index < this.getMonitors().size(); index++) {
                DbMonitor monitor = this.getMonitors().get(index);
                monitor.onCheckOut(this, connection);
            }
            return connection;
        } catch (Exception e) {
            e.printStackTrace();
            throw new SQLException("get connection timeout ", e);
        }
    }

    protected void release(ConnectionWrapper connection) throws SQLException {
        for (int index = 0; index < this.getMonitors().size(); index++) {
            DbMonitor monitor = this.getMonitors().get(index);
            monitor.onCheckIn(this, connection);
        }
        logger.debug("Release Connection dataSource url:" + connection.getDataSource().getName() + "  isAlive:" + connection.getDataSource().isAlive());
        // // 池关闭或连接有严重错误，摧毁connection
        if (connection.isBroken() || isShutDown) {
            // hook calls
            if (connection.isBroken()) {
                logger.info("SWAP connection.isBroken, pool is" + this);
                for (int index = 0; index < this.getMonitors().size(); index++) {
                    DbMonitor monitor = this.getMonitors().get(index);
                    monitor.onBroken(this, connection);
                }
            }

            destroyConnection(connection);
            return;
        }

        this.addFreeConnection(connection); // 放回池

        for (int index = 0; index < this.getMonitors().size(); index++) {
            DbMonitor monitor = this.getMonitors().get(index);
            monitor.onSuccess(this, connection);
        }

        keepSize();

        // executor.execute(keepSizeExecute);
    }

    final Runnable keepSizeExecute = new Runnable() {
        @Override
        public void run() {
            keepSize();
        }
    };

    public boolean isAlive() {
        return isAlive;
    }

    public void addMonitor(DbMonitor monitor) {
        if (monitor == null)
            return;
        if (!this.monitors.contains(monitor)) {
            this.monitors.add(monitor);
            monitor.onBound(this);
        }
    }

    public void removeMonitor(DbMonitor monitor) {
        this.monitors.remove(monitor);
    }

    public List<DbMonitor> getMonitors() {
        return monitors;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * 探测数据库是否活着多次
     * @return
     */
    public boolean checkDbLiveForTimes() {
        boolean rtn = true;
        int checkTimes = 0;
        try {
            rtn = checkLive();
            while (!rtn && (checkTimes < 10)) {
                rtn = checkLive();
                checkTimes++;
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            logger.error("HaPlus: checkDbLiveForTimes Errors " + e.getMessage(), e);
            rtn = false;
        }
        logger.debug("HaPlus: checkDbLiveForTimes name : rtn" + getName() + " : " + rtn);
        return rtn;
    }

    /**
     * 检查数据库是否活着
     * 此处逻辑存在问题  只检查了连接是否有用 但并没有关闭连接
     * @return
     */
    public boolean checkLive() {
        logger.debug("SWAP BEGIN check DataSource live. NAME: " + name);
        Connection connection = null;
        boolean result = false;

        // first, try by a wrapped connection.
        try {
            connection = this.getConnection();
            if (connection != null) {
                result = checkConnectionAlive(connection);
            }
        } catch (SQLException e) {
            result = false;
        }

        DbUtils.closeConnection(connection);

        if (result) {
            this.isAlive = true;
            logger.info("SWAP END of check live with wrapped." + ", RESULT: " + isAlive + "; NAME: " + name + "; Thread Id: " + Thread.currentThread().getId());
            return result;
        }

        // second try by a new raw connection.
        try {
            // safety， 新建一个原始连接
            connection = createConnection();
            result = checkConnectionAlive(connection);
        } catch (SQLException e) {
            result = false;
        }
        DbUtils.closeConnection(connection);
        this.isAlive = result;

        if (result)
            logger.debug("SWAP END of check live with raw." + ", RESULT: " + isAlive + "; NAME: " + name + "; Thread Id: " + Thread.currentThread().getId());
        else
            logger.info("SWAP END of check live with raw." + ", RESULT: " + isAlive + "; NAME: " + name + "; Thread Id: " + Thread.currentThread().getId());

        return result;
    }

    @Override
    public String toString() {
        int min = config.getMinPoolSize();
        int max = config.getMaxPoolSize();
        int totalSize = this.getSize();
        int freeSize = this.getAvailableConnections();
        return this.getName() + (this.isAlive ? " ALIVE" : " DEAD") + ", url: " + config.getConnetionURL() + ", min=" + min + ", max=" + max + ", totalSize=" + totalSize + ", freeSize=" + freeSize;
    }

    protected void setAlive(boolean isAlive) {
        this.isAlive = isAlive;
        if (!this.isAlive)
            this.terminateAllConnections();
    }

    /**
     * 保持数据库连接池的大小合适
     */
    protected void keepSize() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - this.lastReleaseTime < releaseInterval)
            return;

        int min = config.getMinPoolSize();
        int max = config.getMaxPoolSize();

        min = min > 0 ? min : 1;
        max = max > min ? max : min;

        int totalSize = this.getSize();
        if (totalSize <= min)
            return;

        int freeSize = this.getAvailableConnections();

        // 保证线程池有2个
        if (freeSize <= 2)
            return;

        if (freeSize <= releaseStrategyValve) {
            // 空闲连接与使用连接比例为2:1时可以收缩
            if (((freeSize * 3) > (totalSize * 2)) || totalSize > max) {
                if (currentTime - this.lastReleaseTime < releaseInterval)
                    return;
                this.lastReleaseTime = currentTime;
                logger.debug("SWAP release a connection. pool is" + this);
                ConnectionWrapper connection = this.freeConnections.poll();
                if (connection == null)
                    return;
                try {
                    this.destroyConnection(connection);
                } catch (SQLException e) {
                    logger.error("SWAP Error in keepSize to release connection", e);
                }
            }
        } else {

            final ReentrantLock lock = this.releaseLock;
            lock.lock();

            try {
                if (currentTime - this.lastReleaseTime < releaseInterval) {
                    return;
                }
                this.lastReleaseTime = currentTime;
                logger.debug("SWAP release " + freeSize / 2 + " connections. pool is" + this);
                //异步回收连接
                releaseExecutor.execute(new ReleaseTask(freeSize, this));
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * 检查一个链接是否有效
     * 
     * @param connection
     * @return
     */
    protected boolean checkConnectionAlive(Connection connection) {
        Statement stmt = null;
        ResultSet rs = null;
        boolean result = false;
        try {
            stmt = connection.createStatement();
            rs = stmt.executeQuery(checkLiveSQL);
            result = true;
        } catch (SQLException e) {
            e.printStackTrace();
            result = false;
        }
        DbUtils.closeResultSet(rs);
        DbUtils.closeStatement(stmt);
        return result;
    }

    /**
     * Adds a free connection.
     * 
     * @param connectionHandle
     * @throws SQLException
     *             on error
     */
    protected void addFreeConnection(ConnectionWrapper connection) throws SQLException {
        if (!this.freeConnections.offer(connection)) {
            connection.internalClose();
        }
    }

    /**
     * 得到一个新连接
     * 
     * @return
     * @throws SQLException
     */
    protected ConnectionWrapper createConnection() throws SQLException {
        logger.debug("HaPlus: createConnection  cucrent creatingConnCount  :" + creatingConnCount.get());
        this.statsLock.writeLock().lock();
        //如果出现这种情况  那应该是没有控制住大小
        if (creatingConnCount.get() > MAX_CREATING_THREADS) {
            throw new SQLException("HaPlus: createConnection creatingConnCount is to max :" + creatingConnCount.get());
        }
        creatingConnCount.incrementAndGet();
        ConnectionWrapper lgconnection = null;
        try {
            Connection connection = null;
            String url = this.config.getConnetionURL();
            String username = this.config.getUsername();
            String password = this.config.getPassword();

            logger.info(String.format("url:%s user:%s pwd:%s", url, username, password));
            connection = DriverManager.getConnection(url, username, password);
            lgconnection = new ConnectionWrapper(this, connection);
            updateSize(1);
            
            this.lastReleaseTime = System.currentTimeMillis();

            for (int index = 0; index < this.getMonitors().size(); index++) {
                DbMonitor monitor = this.getMonitors().get(index);
                monitor.onCreate(this, lgconnection);
            }
            logger.debug("SWAP Created a new connection by " + this);
        } catch (SQLException e) {
            throw e;
        } finally {
            creatingConnCount.decrementAndGet();
            this.statsLock.writeLock().unlock();
        }

        return lgconnection;
    }

    /**
     * 完全摧毁一个数据库连接
     * 
     * @param connectionHandle
     * @throws SQLException
     */
    protected void destroyConnection(ConnectionWrapper connection) throws SQLException {

        updateSize(-1);
        for (int index = 0; index < this.getMonitors().size(); index++) {
            DbMonitor monitor = this.getMonitors().get(index);
            monitor.onDestroy(this, connection);
        }
        connection.internalClose();
        logger.debug("SWAP Destroyed a connection by " + this);
    }

    /**
     * Updates leased connections statistics
     * 
     * @param increment
     *            value to add/subtract
     */
    protected void updateSize(int increment) {
        try {
            this.statsLock.writeLock().lock();
            this.size += increment;
        } finally {
            this.statsLock.writeLock().unlock();
        }
    }

    /**
     * 读写锁 一定能读到真实的数据大小吗？
     */
    protected int getSize() {
        try {
            this.statsLock.readLock().lock();
            return this.size;
        } finally {
            this.statsLock.readLock().unlock();
        }
    }

    private void LoadDrivers() {
        try {
            Driver driver = (Driver) Class.forName(config.getDriversClass()).newInstance();
            DriverManager.registerDriver(driver);
            DriverManager.setLoginTimeout(1);

        } catch (Exception e) {
            logger.error("", e);
        }
    }

    /** Closes off all connections in all partitions. */
    protected void terminateAllConnections() {
        this.terminationLock.lock();
        try {
            ConnectionWrapper conn;
            while ((conn = this.freeConnections.poll()) != null) {
                try {
                    this.destroyConnection(conn);
                    // conn.internalClose();
                } catch (SQLException e) {
                    logger.error("Error in attempting to close connection", e);
                }
            }
        } finally {
            this.terminationLock.unlock();
        }
    }

    /**
     * Returns the number of avail connections
     * 
     * @return avail connections.
     */
    public int getAvailableConnections() {
        return this.freeConnections.size();
    }

    /*
     * when application exiting destroy all connections
     */
    private void registerExcetEven() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                shutdown();
            }
        });
    }

    public DbConfig getConfig() {
        return this.config;
    }

    //资源释放任务
    class ReleaseTask implements Runnable {
        int freeSize;
        DbDataSource source;

        public ReleaseTask(int freeSize, DbDataSource source) {
            this.freeSize = freeSize;
            this.source = source;
        }

        public void run() {
            for (int i = 0; i < freeSize / 2; i++) {

                ConnectionWrapper connection = source.freeConnections.poll();
                if (connection == null)
                    return;
                try {
                    source.destroyConnection(connection);
                } catch (SQLException e) {
                    logger.error("SWAP Error in keepSize to release connection", e);
                }
            }
        }
    }

}
