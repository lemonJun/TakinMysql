package com.lemonjun.mysql.orm.query;

import java.util.List;

/**
 * 对外提供的底层接口类
 *
 * @author WangYazhou
 * @date  2016年6月15日 下午3:20:10
 * @see
 */
public interface IStatementDAO {

    //应该使用泛型
    public Object insert(Object t, int timeOut) throws Exception;

    /**
     * 更新一个实体，没有说明对为空的字段是直接跳过，还是默认更新
     * @param t
     * @throws Exception
     */
    public int upateEntity(Object t, int timeOut) throws Exception;

    /**----------普通增删改查操作-----------------*/
    public <I> int updateByID(Class<?> clazz, String updateStatement, I id, int timeOut) throws Exception;

    public int updateByWhere(Class<?> clazz, String updateStatement, String condition, int timeOut) throws Exception;

    public <I> int deleteByID(Class<?> clazz, I id, int timeOut) throws Exception;

    public <I> int deleteByIDS(Class<?> clazz, I[] ids, int timeOut) throws Exception;

    public int deleteByWhere(Class<?> clazz, String condition, String limit, int timeOut) throws Exception;

    public <I> Object getById(Class<?> clazz, I id, int timeOut) throws Exception;

    public <T, I> List<T> getListByIDS(Class<T> clazz, I[] ids, int timeOut) throws Exception;

    /**-------------------带条件语句的查询--------------------*/
    public <T> List<T> getListByWhere(Class<T> clazz, String columns, String condition, String orderBy, String limit, int timeOut) throws Exception;

    public <T> List<T> getListBySql(Class<T> clazz, String sqlquery, int timeOut) throws Exception;

    public abstract <T> List<T> getListByConditionForUpdate(Class<T> clazz, String condition, int timeOut) throws Exception;

    /**-------------------带条件语句的分页--------------------*/

    public <T> List<T> pageListByWhere(Class<T> clazz, String condition, String columns, int page, int pageSize, String orderBy, int timeOut) throws Exception;

    /**--------------------------计数类接口-----------------------------*/
    //自已拼sql实现 计数
    public abstract int countByWhere(Class<?> clazz, String condition, int timeOut) throws Exception;

    public abstract int execBySQL(String sqlquery, int timeOut) throws Exception;

}