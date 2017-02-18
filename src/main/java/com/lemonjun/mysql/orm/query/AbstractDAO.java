package com.lemonjun.mysql.orm.query;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.lemonjun.mysql.orm.query.IStatementCreater;
import com.lemonjun.mysql.orm.util.Common;

/**
 * 实现IDAO的抽象基类
 * @author Administrator
 *
 */
public abstract class AbstractDAO implements IStatementDAO, IPreStatementDAO {

    protected IStatementCreater psCreater;

    protected ConnectionHelper connHelper;

    /**
     * @param connHelper the connHelper to set
     */
    public void setConnHelper(ConnectionHelper connHelper) {
        this.connHelper = connHelper;
    }

    /**
     * 默认查询超时时间
     */
    protected int qurryTimeOut = 2;

    /**
     * 默认添加/修改超时时间
     */
    protected int insertUpdateTimeOut = 5;

    /**
     * 默认是否输出执行SQL和执行时间
     */
    protected boolean printlnSqlAndTime = true;

    protected static final Logger logger = Logger.getLogger(AbstractDAO.class);

    protected AbstractDAO() {
    }

    protected <T> List<T> populateData(ResultSet resultSet, Class<T> clazz) throws Exception {
        List<T> dataList = new ArrayList<T>();
        List<Field> fieldList = Common.getAllFields(clazz);

        ResultSetMetaData rsmd = resultSet.getMetaData();
        int columnsCount = rsmd.getColumnCount();
        List<String> columnNameList = new ArrayList<String>();
        for (int i = 0; i < columnsCount; i++) {
            columnNameList.add(rsmd.getColumnLabel(i + 1).toLowerCase());
        }

        while (resultSet.next()) {
            T bean = clazz.newInstance();
            for (Field f : fieldList) {
                String columnName = Common.getDBCloumnName(clazz, f).toLowerCase();
                if (columnNameList.contains(columnName)) {
                    Object columnValueObj = null;
                    Class<?> filedCls = f.getType();

                    if (filedCls == int.class || filedCls == Integer.class) {
                        columnValueObj = resultSet.getInt(columnName);
                    } else if (filedCls == String.class) {
                        columnValueObj = resultSet.getString(columnName);
                    } else if (filedCls == boolean.class || filedCls == Boolean.class) {
                        columnValueObj = resultSet.getBoolean(columnName);
                    } else if (filedCls == byte.class || filedCls == Byte.class) {
                        columnValueObj = resultSet.getByte(columnName);
                    } else if (filedCls == short.class || filedCls == Short.class) {
                        columnValueObj = resultSet.getShort(columnName);
                    } else if (filedCls == long.class || filedCls == Long.class) {
                        columnValueObj = resultSet.getLong(columnName);
                    } else if (filedCls == float.class || filedCls == Float.class) {
                        columnValueObj = resultSet.getFloat(columnName);
                    } else if (filedCls == double.class || filedCls == Double.class) {
                        columnValueObj = resultSet.getDouble(columnName);
                    } else if (filedCls == BigDecimal.class) {
                        columnValueObj = resultSet.getBigDecimal(columnName);
                    }

                    /*
                     * 记住这次教训啊~~~~~~~~~~~
                    else if (filedCls == java.util.Date.class) {
                        columnValueObj = resultSet.getDate(columnName);
                        if (columnValueObj != null) {
                          columnValueObj = new java.util.Date(((java.sql.Date)columnValueObj).getTime());
                        }
                    } 
                    */

                    else {
                        columnValueObj = resultSet.getObject(columnName);
                    }

                    if (columnValueObj != null) {
                        Method setterMethod = Common.getSetterMethod(clazz, f);
                        setterMethod.invoke(bean, new Object[] { columnValueObj });
                    }
                }
            }
            dataList.add(bean);
        }
        return dataList;
    }

    //此处应该用stopwatch
    public void printlnSqlAndTime(String sql, long stime) {
        if (printlnSqlAndTime) {
            logger.info("Execute Sql: " + sql + " Time: " + (System.currentTimeMillis() - stime) + " ms");
        }
    }

}