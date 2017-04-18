package lemon.mysqlorm.test;

import java.util.List;

import org.junit.Test;

import com.lemonjun.mysql.orm.BDProvider;

public class ConfGroupTest {

    @Test
    public void findAll() {
        try {
            String sql = "select * from XXL_CONF_GROUP ";
            List<ConfGroup> list = BDProvider.getInst().Client().getListByPreSQL(ConfGroup.class, sql);
            for (ConfGroup group : list) {
                System.out.println("group:" + group.getGroupname() + " titel:" + group.getGrouptitle());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
