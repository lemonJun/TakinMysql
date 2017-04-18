package lemon.mysqlorm.test;

import com.lemonjun.mysql.orm.annotation.Table;

@Table(name = "XXL_CONF_GROUP")
public class ConfGroup {

    private String groupname;
    private String grouptitle;

    public String getGroupname() {
        return groupname;
    }

    public void setGroupname(String groupname) {
        this.groupname = groupname;
    }

    public String getGrouptitle() {
        return grouptitle;
    }

    public void setGrouptitle(String grouptitle) {
        this.grouptitle = grouptitle;
    }

}
