package lemon.mysqlorm.test;

import com.lemonjun.mysql.orm.annotation.Id;
import com.lemonjun.mysql.orm.annotation.Table;

@Table(name = "kv_entity")
public class CategoryBean {

    @Id
    private int id;
    private String qd_id;
    private String qd_name;
    private String yc_id;
    private String yc_name;
    private int root;
    private int kv_group_id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getQd_id() {
        return qd_id;
    }

    public void setQd_id(String qd_id) {
        this.qd_id = qd_id;
    }

    public String getQd_name() {
        return qd_name;
    }

    public void setQd_name(String qd_name) {
        this.qd_name = qd_name;
    }

    public String getYc_id() {
        return yc_id;
    }

    public void setYc_id(String yc_id) {
        this.yc_id = yc_id;
    }

    public String getYc_name() {
        return yc_name;
    }

    public void setYc_name(String yc_name) {
        this.yc_name = yc_name;
    }

    public int getRoot() {
        return root;
    }

    public void setRoot(int root) {
        this.root = root;
    }

    public int getKv_group_id() {
        return kv_group_id;
    }

    public void setKv_group_id(int kv_group_id) {
        this.kv_group_id = kv_group_id;
    }

}
