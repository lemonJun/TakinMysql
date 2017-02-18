package com.lemonjun.mysql.orm.query;

import java.io.File;
import java.sql.Connection;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.lemonjun.mysql.orm.dbms.AbstractDataSource;
import com.lemonjun.mysql.orm.dbms.DataSourceFactory;
import com.lemonjun.mysql.orm.dbms.DbUtils;
import com.lemonjun.mysql.orm.dbms.config.ConfigUtil;
import com.lemonjun.mysql.orm.dbms.config.DataSourceConfig;
import com.lemonjun.mysql.orm.util.PropertiesHelper;

/**
 * 对外提供clusterdatasource的类
 * 
 * @author WangYazhou
 * @date 2016年6月8日 上午11:50:35
 * @see
 */
public class ConnectionHelper {

    private static final Logger logger = Logger.getLogger(ConnectionHelper.class);

    private AbstractDataSource connPool;

    private static final ThreadLocal<Connection> threadLocal = new ThreadLocal<Connection>();

    //做连接池的初始化
    public ConnectionHelper(String configPath) throws Exception {
        PropertiesHelper ph = new PropertiesHelper(configPath);
        String swapDataSource = ph.getString("swapDataSource");
        String realPath = "";
        if (swapDataSource != null) {
            realPath = (new File(configPath)).getParent() + File.separator + swapDataSource;
            logger.info("realPath:" + realPath);
            DataSourceConfig dataSourceConfig = ConfigUtil.getDataSourceConfig(realPath);
            DataSourceFactory.setConfig(dataSourceConfig);
            DataSource datasource = DataSourceFactory.getDataSource("_DEFAULT");
            AbstractDataSource aDataSource = (AbstractDataSource) datasource;
            connPool = aDataSource;
        }
        if (connPool == null) {
            throw new Exception("conn pool is null: " + realPath);
        }
        logger.info("init ConnectionHelper...");

    }

    /**
     * 获取连接
     * @return
     * @throws Exception
     */
    public Connection get() throws Exception {
        Connection conn = threadLocal.get();
        if (conn == null) {
            conn = connPool.getConnection();
        }
        return conn;
    }

    /**
     * 获得可能只读的数据库连接
     * @return
     * @throws Exception
     */
    public Connection getReadConnection() throws Exception {
        Connection conn = threadLocal.get();
        if (conn == null) {
            conn = connPool.GetReadConnection();
        }
        if (conn == null) {
            conn = connPool.getConnection();
        }
        return conn;
    }

    /**
     * 释放连接
     * @param conn
     * @throws Exception
     */
    public void release(Connection conn) {
        Connection tconn = threadLocal.get();
        if (tconn == null || (tconn != null && (tconn.hashCode() != conn.hashCode()))) {
            logger.debug("this conn is release " + conn);
            DbUtils.closeConnection(conn);
        }
    }

    //线程中记录此conn  做事务用
    public void lockConn(Connection conn) {
        threadLocal.set(conn);
    }

    //线程中除去此conn
    public void unLockConn() {
        threadLocal.set(null);
    }

}
