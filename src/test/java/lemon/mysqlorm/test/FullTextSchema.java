package lemon.mysqlorm.test;

import com.lemonjun.mysql.orm.annotation.Id;
import com.lemonjun.mysql.orm.annotation.Table;

@Table(name = "t_esearch_schema")
public class FullTextSchema {

    @Id
    private long id;

    private String indexName = "";
    private int fieldNum;
    private int fieldType;
    private String lengthFieldName = "";
    private String fieldName = "";
    private String fieldBoost = "";

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public int getFieldNum() {
        return fieldNum;
    }

    public void setFieldNum(int fieldNum) {
        this.fieldNum = fieldNum;
    }

    public int getFieldType() {
        return fieldType;
    }

    public void setFieldType(int fieldType) {
        this.fieldType = fieldType;
    }

    public String getLengthFieldName() {
        return lengthFieldName;
    }

    public void setLengthFieldName(String lengthFieldName) {
        this.lengthFieldName = lengthFieldName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldBoost() {
        return fieldBoost;
    }

    public void setFieldBoost(String fieldBoost) {
        this.fieldBoost = fieldBoost;
    }

}
