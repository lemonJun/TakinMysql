package com.lemonjun.mysql.orm.dbms.config;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据源集群的配置文件
 * @author renjun
 *
 */
public class ClusterConfig {

    private String name;

    private String dataSource = "com.lemonjun.mysql.orm.dbms.ClusterDataSource";

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    private List<DbConfig> dbConfigList = new ArrayList<DbConfig>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addDbConfig(DbConfig dbConfig) {
        if (dbConfig == null)
            return;
        this.dbConfigList.add(dbConfig);
    }

    public List<DbConfig> getDbConfigList() {
        return dbConfigList;
    }

}
