package com.lemonjun.mysql.orm;

/**
 * 返回封装结果
 * 这个可以参考es的
 *
 * @author WangYazhou
 * @date  2016年12月16日 下午1:21:54
 * @see 
 * @param <T>
 */
public class Total<T> {

    private T total;//结果
    private String sql;//执行的查询
    private long time;//耗时 ms

    public T getTotal() {
        return total;
    }
    
    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setTotal(T total) {
        this.total = total;
    }

}
