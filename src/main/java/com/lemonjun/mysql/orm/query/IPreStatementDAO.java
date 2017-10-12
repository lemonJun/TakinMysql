package com.lemonjun.mysql.orm.query;

import java.util.List;

/**
 * 对外提供的底层接口类
 *
 * @author WangYazhou
 * @date  2016年6月15日 下午3:20:10
 * @see
 */
public interface IPreStatementDAO {

    /**---------------------------的分页 -------------------*/
    public abstract <T> List<T> getListByPreSQL(Class<T> clazz, String sql, int timeOut, Object... param) throws Exception;

    /**-------------------------如批量设置一个删除标记位-----------------*/
    public abstract int execByPreSQL(String sql, int timeOut, Object... param) throws Exception;

    //通过preparedstatement 计数
    public abstract int countByPreSQL(String sql, int timeOut, Object... params) throws Exception;

    public int updateByPreSql(String sql, int timeOut, Object... param) throws Exception;

}