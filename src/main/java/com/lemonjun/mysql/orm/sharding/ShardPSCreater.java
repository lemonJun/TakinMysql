package com.lemonjun.mysql.orm.sharding;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lemonjun.mysql.orm.util.Common;
import com.lemonjun.mysql.orm.util.OutSQL;

/**
 * 生成分表时的sql语句
 *
 *
 * @author wangyazhou
 * @version 1.0
 * @date  2016年4月20日 下午8:01:13
 * @see 
 * @since
 */
public class ShardPSCreater {

    private static final Logger logger = LoggerFactory.getLogger(ShardPSCreater.class);

    /**
     * 为分表创建一个ps
     * @param bean
     * @param table
     * @param conn
     * @param sql
     * @return
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    protected static PreparedStatement createInsertPs(Object bean, String table, Connection conn, OutSQL sql) throws Exception {
        Class clazz = bean.getClass();
        StringBuffer sbSql = new StringBuffer(" INSERT INTO ");
        sbSql.append(table);
        sbSql.append("(");
        List<Field> listField = Common.getInsertableFields(clazz);

        StringBuilder sbColumn = new StringBuilder();
        StringBuilder sbValue = new StringBuilder();
        boolean isFirst = true;
        for (int i = 0; i < listField.size(); i++) {
            if (!isFirst) {
                sbColumn.append(", ");
                sbValue.append(", ");
            }
            sbColumn.append("`");
            sbColumn.append(Common.getDBCloumnName(clazz, listField.get(i)));
            sbColumn.append("`");

            sbValue.append("?");
            isFirst = false;
        }

        sbSql.append(sbColumn);
        sbSql.append(" ) VALUES ( ");
        sbSql.append(sbValue);
        sbSql.append(" ) ");
        sql.setSql(sbSql.toString());

        logger.debug(sbSql.toString());

        PreparedStatement ps = conn.prepareStatement(sql.getSql(), 1);
        for (int i = 0; i < listField.size(); i++) {
            Method m = Common.getGetterMethod(clazz, (Field) listField.get(i));
            Object value = m.invoke(bean, new Object[0]);
            Common.setPara(ps, value, i + 1);
        }
        return ps;
    }

    /**
     * 为分表更新ps<br>
     * @param bean
     * @param table
     * @param conn
     * @param sql
     * @return
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    protected static PreparedStatement createUpdatePs(Object bean, String table, Connection conn) throws Exception {
        Class clazz = bean.getClass();
        StringBuffer sbSql = new StringBuffer(" UPDATE ");
        sbSql.append(table);
        sbSql.append(" SET ");
        List<Field> listField = Common.getUpdatableFields(clazz);

        StringBuilder sbColumn = new StringBuilder();
        StringBuilder sbValue = new StringBuilder();
        boolean isFirst = true;
        for (int i = 0; i < listField.size() - 1; i++) {
            if (!isFirst) {
                sbColumn.append(", ");
                sbValue.append(", ");
            }
            sbSql.append(Common.getDBCloumnName(clazz, listField.get(i)));
            sbSql.append(" = ");
            sbSql.append("?,");
            isFirst = false;
        }
        sbSql.deleteCharAt(sbSql.length() - 1);
        sbSql.append(" WHERE ").append(" buid=? AND ");
        sbSql.append("id=?");
        logger.debug(sbSql.toString());

        PreparedStatement ps = conn.prepareStatement(sbSql.toString(), 1);
        for (int i = 0; i < listField.size(); i++) {
            Method m = Common.getGetterMethod(clazz, (Field) listField.get(i));
            Object value = m.invoke(bean, new Object[0]);
            if (m.getName().equals("getBuid")) {
            }
            Common.setPara(ps, value, i + 1);
        }
        Field field = clazz.getDeclaredField("buid");
        field.setAccessible(true);
        Common.setPara(ps, field.get(bean), listField.size());
        field = clazz.getDeclaredField("id");
        field.setAccessible(true);
        Common.setPara(ps, field.get(bean), listField.size() + 1);
        return ps;
    }

    /**
     * 为分表更新ps
     * @param bean
     * @param table
     * @param conn
     * @param sql
     * @return
     * @throws Exception
     */
    protected static PreparedStatement createUpdatePs(String table, String set, String where, Connection conn) throws Exception {
        StringBuffer sbSql = new StringBuffer(" UPDATE ");
        sbSql.append(table);
        sbSql.append(" SET ");
        sbSql.append(set);
        sbSql.append(" WHERE ");
        sbSql.append(where);
        return conn.prepareStatement(sbSql.toString());
    }

}
