package com.lemonjun.mysql.orm;

import java.sql.Connection;

/**
 * 本地线程中缓存的对像
 * 其中withTranc=true和conn是同时存在的
 *
 * @author WangYazhou
 * @date  2016年12月23日 下午2:48:34
 * @see
 */
public class LocalParam {
    
    private Connection conn;
    private boolean withTranc;
    private int timeout;

    public Connection getConn() {
        return conn;
    }

    public void setConn(Connection conn) {
        this.conn = conn;
    }

    public boolean isWithTranc() {
        return withTranc;
    }

    public void setWithTranc(boolean withTranc) {
        this.withTranc = withTranc;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

}
