package com.lemonjun.mysql.orm.dbms.pool;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * 连接池对外统一接口类   
 * 有点不理解  为什么这个类中还要自己维护一个连接队列   这些本应该是底层维护好的
 *
 * @author WangYazhou
 * @date  2016年12月22日 下午1:28:57
 * @see
 */
public class ConnectionPool {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionPool.class);

    //可用的连接
    private Stack<Connection> AvailableConn = new Stack<Connection>(); //Connections stack

    private DBConfig _Config; //初始化配置
    private List<Connection> Pool = Lists.newArrayList(); //Here store all connections.

    public ConnectionPool() {
    }

    public ConnectionPool(DBConfig config) {
        try {
            _Config = config;
            LoadDrivers();
            Init();
            RegisterExcetEven();
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    public int GetAllCount() {
        return Pool.size();
    }

    public int GetFreeConnCount() {
        return AvailableConn.size();
    }

    private PoolState state = new PoolState();

    public synchronized Connection Get() throws Exception {
        int freeCount = AvailableConn.size();
        state.setNoWorkCout(freeCount);
        if (freeCount > 0) {
            Connection conn = AvailableConn.pop();
            if (conn == null) {
                Pool.remove(conn);
            }
            logger.debug("Connection get " + conn + " connection size is " + GetAllCount() + " FreeConnCount is " + GetFreeConnCount());
            return conn;
        } else if (GetAllCount() < _Config.getMaxPoolSize()) {
            Connection conn = CreateConn();
            logger.debug(" Connection get " + conn + " connection size is " + GetAllCount() + " FreeConnCount is " + GetFreeConnCount());
            return conn;
        } else {
            throw new Exception("db connection pool is full");
        }
    }

    /**
     * 2011-05-24 获得一个只需要读的数据库连接,处理主库压力大的情况下，降低主库压力
     * by renjun
     * @return 一个只供读的数据库连接，可能是主库，也有可能是从库
     * @throws Exception
     */
    public Connection GetReadConnection() throws Exception {
        return Get();
    }

    public synchronized void Release(Connection conn) {
        if (conn != null) {
            try {
                if (_Config.getAutoShrink() //is allow auto shrink pool
                                && state.GetNoWorkCount(_Config.getIdleTimeout() * 1000) > 0//check no work connections count
                ) {
                    Destroy(conn);
                    state.setNoWorkCout(GetFreeConnCount());
                } else {
                    if (!conn.isClosed()) {
                        AvailableConn.push(conn);
                    } else {
                        Destroy(conn);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        logger.debug("Release method connection size is " + GetAllCount() + " FreeConnCount is " + GetFreeConnCount());
    }

    //-------------------------------------------------------------------------------------------------------------------------------------//

    private synchronized void Destroy(Connection conn) throws SQLException {
        if (conn != null) {
            try {
                conn.close();
                Pool.remove(conn); //remove this connection from pool
                logger.debug(" close one connection!!!" + conn + " connection size is " + GetAllCount() + " FreeConnCount is " + GetFreeConnCount());
            } finally {
                conn = null;
            }
        }
    }

    private synchronized Connection CreateConn() throws Exception {
        Connection conn = null;
        try {
            if (_Config.getUserName() == null) {
                conn = DriverManager.getConnection(_Config.getConnetionUrl());
            } else {
                conn = DriverManager.getConnection(_Config.getConnetionUrl(), _Config.getUserName(), _Config.getPassWord());
            }
        } catch (SQLException e) {
            //			return null;
            throw e;
        } finally {
            if (conn != null && !conn.isClosed()) {
                logger.debug(" this conn is create " + conn + " connection size is " + GetAllCount() + " FreeConnCount is " + GetFreeConnCount());
                Pool.add(conn);
            }
        }
        return conn;
    }

    private void LoadDrivers() {
        try {
            Driver driver = (Driver) Class.forName(_Config.getDriversClass()).newInstance();
            DriverManager.registerDriver(driver);
            DriverManager.setLoginTimeout(1);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void Init() throws Exception {
        for (int i = 0; i < _Config.getMinPoolSize(); i++) {
            Connection conn = CreateConn();
            if (conn != null) {
                AvailableConn.push(conn);
            }
        }
    }

    /*
     * when application exiting destroy all connections
     */
    private void RegisterExcetEven() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                logger.debug("application exiting,begin remove all connections.");
                for (Connection conn : Pool) {
                    try {
                        if (conn != null && !conn.isClosed()) {
                            conn.close();
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    //-----------------------------------------------------------------------------------------------------//

    public class PoolState {
        private long duration;
        private int noWorkCount = 0;

        public synchronized void setNoWorkCout(int noWorkCout) {
            if (this.noWorkCount <= 2 && noWorkCout > 2) {
                duration = System.currentTimeMillis();
            }
            this.noWorkCount = noWorkCout;
        }

        public synchronized int GetNoWorkCount(long duration) {
            if ((System.currentTimeMillis() - this.duration > duration)) {
                return (noWorkCount - 2);
            }
            return 0;
        }
    }
}
