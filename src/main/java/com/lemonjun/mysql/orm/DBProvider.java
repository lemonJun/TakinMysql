package com.lemonjun.mysql.orm;

import org.apache.log4j.Logger;

/**
 * 这个类应该是给调用方使用的 不应该包含在这个客户端中
 *
 * @author WangYazhou
 * @date 2016年6月8日 上午10:59:19
 * @see
 */
public class DBProvider {

    private static final Logger logger = Logger.getLogger(DBProvider.class);

    private volatile DBTemplateClient client = null;
    private volatile static DBProvider instance = null;

    private DBProvider() {
    }

    public DBTemplateClient Client() {
        return client;
    }

    public static DBProvider getInst() {
        if (instance == null) {
            synchronized (DBProvider.class) {
                if (instance != null) {
                    return instance;
                }
                try {
                    String dbpath = DBProvider.class.getClassLoader().getResource("db.properties").getPath();
                    //                    String dbpath = "E:/myworkspace/git/lemon.mysql.orm/src/test/resources/db.properties";
                    logger.info(dbpath);
                    DBProvider provider = new DBProvider();
                    provider.client = new DBTemplateClient(dbpath);
                    instance = provider;
                    logger.debug("init db success");
                } catch (Exception e) {
                    instance = null;
                    logger.error("init db error", e);
                }
            }
        }
        return instance;
    }

}
