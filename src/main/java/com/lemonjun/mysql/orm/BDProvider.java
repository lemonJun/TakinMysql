package com.lemonjun.mysql.orm;

import org.apache.log4j.Logger;

/**
 * 这个类应该是给调用方使用的 不应该包含在这个客户端中
 *
 * @author WangYazhou
 * @date 2016年6月8日 上午10:59:19
 * @see
 */
public class BDProvider {

    private static final Logger logger = Logger.getLogger(BDProvider.class);

    private volatile DBTemplateClient client = null;
    private volatile static BDProvider instance = null;

    private BDProvider() {
    }

    public DBTemplateClient Client() {
        return client;
    }

    public static BDProvider getInst() {
        if (instance == null) {
            synchronized (BDProvider.class) {
                if (instance != null) {
                    return instance;
                }
                try {
                    String dbpath = BDProvider.class.getClassLoader().getResource("db.properties").getPath();
                    //                    String dbpath = "E:/myworkspace/git/lemon.mysql.orm/src/test/resources/db.properties";
                    logger.info(dbpath);
                    instance = new BDProvider();
                    instance.client = new DBTemplateClient(dbpath);
                    logger.info("init db success");
                } catch (Exception e) {
                    instance = null;
                    logger.error("init db error", e);
                }
            }
        }
        return instance;
    }

}
