package com.lemonjun.mysql.orm.dbms.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 所有数据源的配置文件
 * @author renjun
 *
 */
public class DataSourceConfig {
    /**
     * 系统当前的所有数据源，主键为数据源的服务名称，值为该服务所包含的多数据源对象集合。
     */
    Map<String, ClusterConfig> clusterConfigMap = new ConcurrentHashMap<String, ClusterConfig>();

    public void addClusterConfig(ClusterConfig clusterConfig) {
        if (clusterConfig == null)
            return;
        clusterConfigMap.put(clusterConfig.getName(), clusterConfig);
    }

    /**
     * 根据datasource名获得对应配置文件
     * @param datasourceName
     * @return
     */

    public ClusterConfig getDataSourceConfig(String datasourceName) {
        return clusterConfigMap.get(datasourceName);
    }

    /**
     * 返回全部ClusterConfig对象
     * @return
     */
    public Map<String, ClusterConfig> getDataSourceConfig() {
        return clusterConfigMap;
    }
}
