package lemon.mysqlorm.test;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lemonjun.mysql.orm.BDProvider;

public class MysqlDaoTest {

    private static final Logger logger = LoggerFactory.getLogger(MysqlDaoTest.class);

    //    public static CategoryBean getCategoryByName(String keyCategory, int groupId) {
    //        try {
    //            String sql = "select * from kv_entity where qd_name='" + keyCategory + "' and kv_group_id=" + groupId + " limit 1 ";
    //            //            List<CategoryBean> cates = BDProvider.getInst().Client().withTranction(true).timeOut(20 - 0).getListByPreSQL(CategoryBean.class, sql, 5);
    //
    //            logger.info(String.format("result size:%s key:%s name:%s sql:%s", cates.size(), cates.get(0).getYc_id(), cates.get(0).getYc_name(), sql));
    //            return cates.get(0);
    //        } catch (Exception e) {
    //            e.printStackTrace();
    //            logger.error("", e);
    //        }
    //        return null;
    //    }

    private static final ExecutorService executor = Executors.newFixedThreadPool(10);

    public static void main(String[] args) {
        try {
            String path = "D:/log4j.properties";
            PropertyConfigurator.configure(path);
            //            for (int i = 0; i < 1; i++) {
            //                executor.submit(new TThread());
            //            }
            //            Thread.sleep(100000);
            //            getCategoryByName("ERP实施顾问", 1);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.exit(-1);
        }
    }

    static class TThread extends Thread {
        String keyCategory = "ERP实施顾问";
        int groupId = 1;

        @Override
        public void run() {
            try {
                String sql = "select * from kv_entity where qd_name='" + keyCategory + "' and kv_group_id=" + groupId + " limit 1 ";
                List<CategoryBean> cates = BDProvider.getInst().Client().getListByPreSQL(CategoryBean.class, sql, 5);

                logger.info(String.format("result size:%s key:%s name:%s sql:%s", cates.size(), cates.get(0).getYc_id(), cates.get(0).getYc_name(), sql));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
}
