package lemon.mysqlorm.test;

import java.io.File;
import java.util.List;

import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.lemonjun.mysql.orm.BDProvider;

public class FullTextDao {

    @Test
    public void readdao() {
        try {
            List<FullTextSchema> list = BDProvider.getInst().Client().getAllByLimit(FullTextSchema.class, "");
            System.out.println(JSON.toJSONString(list));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void jsonfile() {
        try {
            List<String> lines = Files.readLines(new File("D:/json.conf"), Charsets.UTF_8);
            StringBuffer sb = new StringBuffer();
            for (String str : lines) {
                sb.append(str);
            }
            JSONArray json = JSONObject.parseArray(sb.toString());
            System.out.println(json.toJSONString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
