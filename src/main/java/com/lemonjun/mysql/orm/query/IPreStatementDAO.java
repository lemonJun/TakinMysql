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

    public abstract <T> List<T> getListByPreSQL(Class<T> clazz, String sql, int timeOut, Object... param) throws Exception;

    public abstract int execByPreSQL(String sql, int timeOut, Object... param) throws Exception;

    public abstract int countByPreSQL(String sql, int timeOut, Object... params) throws Exception;
    
    public int updateByPreSql(String sqlquery, int timeOut, Object... param) throws Exception;

    public abstract <T> List<T> pageListByPreSql(Class<T> clazz, String sqlquery, int page, int pageSize, int timeOut, Object... params) throws Exception;

    public int deleteByPreSql(String sqlquery, int timeOut, Object... param) throws Exception;

}