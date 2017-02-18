//package com.lemonjun.mysql.orm.dbms.pool;
//
//import javax.sql.DataSource;
//
//import com.lemonjun.mysql.orm.dbms.AbstractDataSource;
//import com.lemonjun.mysql.orm.dbms.DataSourceFactory;
//import com.lemonjun.mysql.orm.dbms.config.ConfigUtil;
//import com.lemonjun.mysql.orm.dbms.config.DataSourceConfig;
//
//public class ConnectionPoolFactory {
//
//    /**
//     * 根据配置文件，得到对应的ConnectionPool，该ConnectionPool封装SWAP DataSource实现
//     * @param configPath
//     * @return
//     * @throws Exception
//     */
//    public synchronized static AbstractDataSource createPool(String configPath) throws Exception {
//
//        DataSourceConfig dataSourceConfig = ConfigUtil.getDataSourceConfig(configPath);
//        DataSourceFactory.setConfig(dataSourceConfig);
//        DataSource datasource = DataSourceFactory.getDataSource("_DEFAULT");
//
//        AbstractDataSource aDataSource = (AbstractDataSource) datasource;
//
//        //        SwapConnectionPool dbConnectionPool = new SwapConnectionPool(aDataSource);
//        //        SwapConnectionPool dbConnectionPool = null;
//        
//        return aDataSource;
//    }
//
//}
