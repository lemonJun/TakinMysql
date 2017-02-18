package com.lemonjun.mysql.orm;

import java.util.List;

/**
 * 数据库对外统一提供的接口类
 * 有了这一层接口的好处是：client不用再对外直接暴露sql对象了，一些关键的参数如timeout、事务可以以流的方式提供
 * 更关键的一点是  可以对sql对象提供代理和拦截的功能
 *
 * @author WangYazhou
 * @date  2016年12月23日 下午2:24:55
 * @see
 */
public interface DBOperations {

    public abstract Object insert(Object t) throws Exception;

    public abstract void upateEntity(Object t) throws Exception;

    public abstract <I> void updateByID(Class<?> clazz, String updateStatement, I id, int timeOut) throws Exception;

    public abstract void updateByWhere(Class<?> clazz, String updateStatement, String condition, int timeOut) throws Exception;

    public abstract <I> void deleteByID(Class<?> clazz, I id, int timeOut) throws Exception;

    public abstract <I> void deleteByIDS(Class<?> clazz, I[] ids, int timeOut) throws Exception;

    public abstract void deleteByWhere(Class<?> clazz, String condition, int timeOut) throws Exception;

    public abstract <I> Object getById(Class<?> clazz, I id, int timeOut) throws Exception;

    public abstract <T, I> List<T> getListByIDS(Class<T> clazz, I[] ids, int timeOut) throws Exception;

    public abstract <T> List<T> getListByWhere(Class<T> clazz, String columns, String condition, String orderBy, String limit, int timeOut) throws Exception;

    public abstract <T> List<T> pageListByWhere(Class<T> clazz, String condition, String columns, int page, int pageSize, String orderBy, int timeOut) throws Exception;

    public abstract int countBySql(Class<?> clazz, String condition, int timeOut) throws Exception;

    public abstract <T> List<T> getListByPreSQL(Class<T> clazz, String sql, int timeOut, Object... param) throws Exception;

    public abstract int execByPreSQL(String presql, int timeOut, Object... param) throws Exception;
    
    public abstract int countByPreSQL(String sql, int timeOut, Object... params) throws Exception;

}
