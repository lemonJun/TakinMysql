package lemon.mysqlorm.test;

import org.junit.Test;

import com.lemonjun.mysql.orm.util.SqlInjectHelper;

public class SqlInjectTest {

    @Test
    public void helper() {
        try {
            String sql = "select * from user where name='a' or 1=1 ; drop table user;";
            String result = SqlInjectHelper.simpleFilterSql(sql);
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
