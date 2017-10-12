package com.lemonjun.mysql.orm.dbms;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lemonjun.mysql.orm.dbms.config.ClusterConfig;
import com.lemonjun.mysql.orm.dbms.config.DataSourceConfig;

/**
 * Datasource的类工厂
 * 
 * @author renjun
 * 
 */
public class DataSourceFactory {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceFactory.class);

    /**
     * 系统当前所有的数据库池群
     */
    private static Map<String, AbstractDataSource> clouds = new HashMap<String, AbstractDataSource>();

    /**
     * 根据datasource名称获得datasource
     * 
     * @param dataSourceName
     *            大小写不敏感
     * @return
     * @throws Exception
     */
    public static DataSource getDataSource(String dataSourceName) throws Exception {
        return getAbstractDataSource(dataSourceName);
    }

    public static AbstractDataSource getAbstractDataSource(String dataSourceName) throws Exception {
        AbstractDataSource dataSource = clouds.get(dataSourceName);

        if (dataSource != null)
            return dataSource;

        throw new Exception("there is no dataSourceConfig for " + dataSourceName);
    }
    
    /**
     * 设置配置文件
     * 
     * @param config
     * @throws Exception
     */
    public synchronized static void setConfig(DataSourceConfig config) throws Exception {
        if (clouds.size() != 0) {
            clouds.clear();
        }
        //此处执行数据库连接池的初始化  默认是idle的连接池
        Map<String, ClusterConfig> clusters = config.getDataSourceConfig();
        for (String key : clusters.keySet()) {
            String clazzName = clusters.get(key).getDataSource();
            logger.debug("key:" + key + " datasource class:" + clazzName);
            Object obj = Class.forName(clazzName).getConstructor(ClusterConfig.class).newInstance(clusters.get(key));
            if (obj instanceof AbstractDataSource) {
                clouds.put(key, (AbstractDataSource) obj);
            }
        }
    }

    /**
     * 私有构造函数
     */
    private DataSourceFactory() {
    }

}
