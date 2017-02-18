package com.lemonjun.mysql.orm.dbms;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

import javax.sql.DataSource;

public abstract class AbstractDataSource implements DataSource {

    /**
     * 2011-05-24 获得一个只需要读的数据库连接,处理主库压力大的情况下，降低主库压力
     * by renjun
     * @return 一个只供读的数据库连接，可能是主库，也有可能是从库
     * @throws SQLException
     */
    public Connection GetReadConnection() throws SQLException {
        throw new SQLException("Not Implemented");
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        throw new SQLException("Not Implemented");
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        throw new SQLException("Not Implemented");
    }

    @Override
    public abstract Connection getConnection() throws SQLException;

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        throw new SQLException("Not Implemented");
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        throw new SQLException("Not Implemented");
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new SQLException("Not Implemented");
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        throw new SQLException("Not Implemented");
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        throw new SQLException("Not Implemented");
    }

    @Override
    public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }
}
