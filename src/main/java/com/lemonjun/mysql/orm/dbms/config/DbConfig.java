/**
 * 
 */
package com.lemonjun.mysql.orm.dbms.config;

/**
 * 一个具体数据库连接配置
 * @author renjun
 *
 */
public class DbConfig {

    /**
     * 驱动程序类
     */
    private String driversClass;
    /**
     * 连接字符串
     */
    private String connetionURL;
    /**
     * 用户名
     */
    private String username;
    /**
     * 密码
     */
    private String password;
    /**
     * 池内连接最大容量
     */
    private int maxPoolSize;
    /**
     * 池内连接最小数量
     */
    private int minPoolSize;
    /**
     * 闲置超时时间(当连接闲置时间超过当前设置则进行回收)
     */
    private int idleTimeout;
    /**
     * 查询等待超时设置(等待查询超过此时间则返回)
     */
    private long queryTimeout;

    /**
     * 新增/更新超时时间(等待新增/更新超过此时间则返回)
     */
    private long insertUpdateTimeout;
    /**
     * 数据库是否只读
     */
    private boolean readonly = false;

    /**
     * 支持系统级别存储过程的数据库配置 本属性仅供 IndieDataSource 连接池使用
     */
    private DbConfig managerDbConfig;

    private long releaseInterval;

    private int releaseStrategyValve;

    public DbConfig getManagerDbConfig() {
        return managerDbConfig;
    }

    public void setManagerDbConfig(DbConfig managerDbConfig) {
        this.managerDbConfig = managerDbConfig;
    }

    public String getDriversClass() {
        return driversClass;
    }

    public void setDriversClass(String driversClass) {
        this.driversClass = driversClass;
    }

    public String getConnetionURL() {
        return connetionURL;
    }

    public void setConnetionURL(String connetionURL) {
        this.connetionURL = connetionURL;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public int getMinPoolSize() {
        return minPoolSize;
    }

    public void setMinPoolSize(int minPoolSize) {
        this.minPoolSize = minPoolSize;
    }

    public int getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(int idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    public long getQueryTimeout() {
        return queryTimeout;
    }

    public void setQueryTimeout(long queryTimeout) {
        this.queryTimeout = queryTimeout;
    }

    public long getInsertUpdateTimeout() {
        return insertUpdateTimeout;
    }

    public void setInsertUpdateTimeout(long insertUpdateTimeout) {
        this.insertUpdateTimeout = insertUpdateTimeout;
    }

    public boolean isReadonly() {
        return readonly;
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    public long getReleaseInterval() {
        return releaseInterval;
    }

    public void setReleaseInterval(long releaseInterval) {
        this.releaseInterval = releaseInterval;
    }

    public int getReleaseStrategyValve() {
        return releaseStrategyValve;
    }

    public void setReleaseStrategyValve(int releaseStrategyValve) {
        this.releaseStrategyValve = releaseStrategyValve;
    }

}
