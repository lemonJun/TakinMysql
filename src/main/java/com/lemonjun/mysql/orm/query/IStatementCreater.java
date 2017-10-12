package com.lemonjun.mysql.orm.query;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Map;

import com.lemonjun.mysql.orm.util.OutSQL;

/**
 * statement或preparestatement生成接口类 如果不考虑其它数据库的话 这个接口类是不必须的
 * 
 * @author WangYazhou
 * @date 2016年6月8日 上午11:05:19
 * @see
 */
public interface IStatementCreater {

    public <I> PreparedStatement createDelete(Class<?> clazz, Connection conn, I id, OutSQL sql) throws Exception;

    public <I> PreparedStatement createDeleteByIDS(Class<?> clazz, Connection conn, I[] ids, OutSQL sql) throws Exception;

    public PreparedStatement createDeleteByCustom(Class<?> clazz, Connection conn, String condition, OutSQL sql) throws Exception;

    //    public PreparedStatement createDeleteByCustom(Class<?> clazz, Connection conn, Map<String, Object> condition, OutSQL sql) throws Exception;

    public PreparedStatement createGetByCustom(Class<?> clazz, Connection conn, String columns, String condition, String orderBy, String limit, OutSQL sql) throws Exception;

    //    public PreparedStatement createGetByCustom(Class<?> clazz, Connection conn, String columns, Map<String, Object> condition, String orderBy, OutSQL sql) throws Exception;

    public <I> PreparedStatement createGetByIDS(Class<?> clazz, Connection conn, I[] ids, OutSQL sql) throws Exception;

    public PreparedStatement createGetByPage(Class<?> clazz, Connection conn, String condition, String columns, int page, int pageSize, String orderBy, OutSQL sql) throws Exception;

    public <I> PreparedStatement createGetEntity(Class<?> clazz, Connection conn, I id, OutSQL sql) throws Exception;

    public PreparedStatement createUpdateByCustom(Class<?> clazz, Connection conn, String updateStatement, String condition, OutSQL sql) throws Exception;

    public PreparedStatement createUpdateEntity(Object bean, Connection conn, OutSQL sql) throws Exception;

    public <I> PreparedStatement createUpdateByID(Class<?> clazz, Connection conn, String updateStatement, I id, OutSQL sql) throws Exception;

    public PreparedStatement createInsert(Object bean, Connection conn, OutSQL sql) throws Exception;

    public PreparedStatement createGetCount(Class<?> clazz, Connection conn, String condition, OutSQL sql) throws Exception;

}