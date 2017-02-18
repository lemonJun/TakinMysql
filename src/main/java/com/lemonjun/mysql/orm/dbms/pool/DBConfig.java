package com.lemonjun.mysql.orm.dbms.pool;

public class DBConfig {

    private String driversClass;

    private String connetionUrl;

    private String userName;

    private String passWord;

    private int maxPoolSize;

    private int minPoolSize;

    private int idleTimeout;

    private long timeout;

    private Boolean autoShrink;

    public String getDriversClass() {
        return driversClass;
    }

    public void setDriversClass(String driversClass) {
        this.driversClass = driversClass;
    }

    public String getConnetionUrl() {
        return connetionUrl;
    }

    public void setConnetionUrl(String connetionUrl) {
        this.connetionUrl = connetionUrl;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public void setMinPoolSize(int minPoolSize) {
        this.minPoolSize = minPoolSize;
    }

    public int getMinPoolSize() {
        return minPoolSize;
    }

    public void setIdleTimeout(int idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    public int getIdleTimeout() {
        return idleTimeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setAutoShrink(Boolean autoShrink) {
        this.autoShrink = autoShrink;
    }

    public Boolean getAutoShrink() {
        return autoShrink;
    }
}