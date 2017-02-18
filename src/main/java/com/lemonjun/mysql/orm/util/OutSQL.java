package com.lemonjun.mysql.orm.util;

public class OutSQL {

    private String sql;
    private String realSql;

    public OutSQL() {

    }

    public OutSQL(String sql) {
        this.sql = sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getSql() {
        return sql;
    }

    public String getRealSql() {
        return realSql;
    }

    public void setRealSql(String realSql) {
        this.realSql = realSql;
    }

}