package com.lemonjun.mysql.orm.dbms.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.ho.yaml.Yaml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lemonjun.mysql.orm.util.MessageAlert;

/**
 * 根据yaml配置文件获得DataSourceConfig的配置文件
 *  
 * @author renjun
 * 
 */
public class ConfigUtil {

    private static Logger logger = LoggerFactory.getLogger(ConfigUtil.class);

    /**
     * 根据yaml配置文件获得DataSourceConfig的配置文件
     * 
     * @param filePath
     * @return
     * @throws SQLException
     */
    @SuppressWarnings("unchecked")
    public static DataSourceConfig getDataSourceConfig(String filePath) throws SQLException {
        // 2011-06-11
        MessageAlert.Factory.setConfig((new File(filePath)).getParent());

        DataSourceConfig dataSourceConfig = new DataSourceConfig();
        ClusterConfig clusterConfig = new ClusterConfig();

        List<HashMap<String, Object>> obj;
        try {
            obj = (List<HashMap<String, Object>>) Yaml.loadType(new File(filePath), ArrayList.class);
            for (HashMap<String, Object> config : obj) {
                DbConfig dbConfig = assemblyDbConfig(config);
                clusterConfig.addDbConfig(dbConfig);
                clusterConfig.setName("_DEFAULT");
            }
            dataSourceConfig.addClusterConfig(clusterConfig);
        } catch (FileNotFoundException e) {
            logger.error("", e);
        }

        return dataSourceConfig;
    }

    private static DbConfig assemblyDbConfig(HashMap<String, Object> config) {
        DbConfig dbConfig = new DbConfig();
        dbConfig.setConnetionURL((String) config.get("connectionURL"));
        dbConfig.setDriversClass((String) config.get("driversClass"));
        dbConfig.setUsername(String.valueOf(config.get("username")));
        dbConfig.setPassword(String.valueOf(config.get("password")));
        dbConfig.setIdleTimeout(Integer.parseInt(String.valueOf(config.get("idleTimeout"))));
        dbConfig.setInsertUpdateTimeout(Integer.parseInt(String.valueOf(config.get("insertUpdateTimeout"))));
        dbConfig.setMaxPoolSize(Integer.parseInt(String.valueOf(config.get("maxPoolSize"))));
        dbConfig.setMinPoolSize(Integer.parseInt(String.valueOf(config.get("minPoolSize"))));
        dbConfig.setQueryTimeout(Integer.parseInt(String.valueOf(config.get("queryTimeout"))));

        boolean readonlyValue = false;
        String readonly = String.valueOf(config.get("readonly"));
        if (readonly != null && readonly.equals("true"))
            readonlyValue = true;

        dbConfig.setReadonly(readonlyValue);
        if (config.get("releaseInterval") != null)
            dbConfig.setReleaseInterval(Long.parseLong(String.valueOf(config.get("releaseInterval"))));

        if (config.get("releaseStrategyValve") != null)
            dbConfig.setReleaseStrategyValve(Integer.parseInt(String.valueOf(config.get("releaseStrategyValve"))));

        dbConfig.setReadonly(readonlyValue);
        return dbConfig;
    }
    
}
