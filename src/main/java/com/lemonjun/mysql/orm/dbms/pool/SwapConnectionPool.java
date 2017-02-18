//package com.lemonjun.mysql.orm.dbms.pool;
//
//import java.sql.Connection;
//
//import com.lemonjun.mysql.orm.dbms.AbstractDataSource;
//import com.lemonjun.mysql.orm.dbms.DbUtils;
//
///**
// * 该ConnectionPool封装SWAP DataSource实现
// * @author renjun
// *
// */
//class SwapConnectionPool extends ConnectionPool {
//
//    private AbstractDataSource datasource;
//
//    public SwapConnectionPool(AbstractDataSource datasource) {
//        this.datasource = datasource;
//    }
//
//    public int GetAllCount() {
//        return -1;
//    }
//
//    public int GetFreeConnCount() {
//        return -1;
//    }
//
//    public synchronized Connection Get() throws Exception {
//        return datasource.getConnection();
//    }
//
//    public synchronized void Release(Connection connection) {
//        DbUtils.closeConnection(connection);
//    }
//
//    public Connection GetReadConnection() throws Exception {
//        return datasource.GetReadConnection();
//    }
//}
