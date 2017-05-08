package lemon.mysqlorm.test;

import com.lemonjun.mysql.orm.annotation.Id;
import com.lemonjun.mysql.orm.annotation.Table;

@Table(name = "t_esearch_schema")
public class FullTextSchema {

    @Id
    private long id;// 唯一ID
    private String indexName = "";// 索引名称 同一个配置的索引名称必须一样
    private int fieldNum; // 字段序号
    private boolean markLastPos = false;// 是否在倒排中标识出最后一个分词的位置
    private String fieldName = "";// 字段名称 对参数字段 必须以param开头 方便解析
    private float fieldBoost = 1.0f;// 字段权重

    private String fieldLengthName = "";

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

    public boolean isMarkLastPos() {
        return markLastPos;
    }

    public void setMarkLastPos(boolean markLastPos) {
        this.markLastPos = markLastPos;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public float getFieldBoost() {
        return fieldBoost;
    }

    public void setFieldBoost(float fieldBoost) {
        this.fieldBoost = fieldBoost;
    }

    public String getFieldLengthName() {
        return fieldLengthName;
    }

    public void setFieldLengthName(String fieldLengthName) {
        this.fieldLengthName = fieldLengthName;
    }

}
