package com.lemonjun.mysql.orm.query;

import java.lang.reflect.Field;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.lemonjun.mysql.orm.util.Common;
import com.lemonjun.mysql.orm.util.JdbcUitl;
import com.lemonjun.mysql.orm.util.OutSQL;
import com.lemonjun.mysql.orm.util.SqlInjectHelper;

public class BaseDAOImpl extends AbstractDAO {

    public BaseDAOImpl(IStatementCreater creater) {
        super.psCreater = creater;
    }

    //-----------------------real sql executor-------------------------//
    @Override
    public int countByPreSQL(String sql, int timeOut, Object... param) throws Exception {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = connHelper.getReadConnection();
            ps = conn.prepareStatement(sql);
            ps.setQueryTimeout(timeOut);

            if (param != null) {
                for (int i = 0; i < param.length; i++) {
                    Common.setPara(ps, param[i], i + 1);
                }
            }
            long startTime = System.currentTimeMillis();
            rs = ps.executeQuery();
            printlnSqlAndTime(sql, startTime);
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (Exception ex) {
            logger.error("countByPreSQL error sql:" + sql, ex);
            throw ex;
        } finally {
            JdbcUitl.closeResultSet(rs);
            JdbcUitl.closeStatement(ps);
            connHelper.release(conn);
        }
    }

    @Override
    public <T> List<T> getListByPreSQL(Class<T> clazz, String sql, int timeOut, Object... param) throws Exception {
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        List<T> dataList = null;
        try {
            conn = connHelper.getReadConnection();
            ps = conn.prepareStatement(sql);
            ps.setQueryTimeout(timeOut);

            if (param != null) {
                for (int i = 0; i < param.length; i++) {
                    Common.setPara(ps, param[i], i + 1);
                }
            }
            long startTime = System.currentTimeMillis();
            rs = ps.executeQuery();
            printlnSqlAndTime(sql, startTime);
            dataList = populateData(rs, clazz);
        } catch (Exception e) {
            logger.error("getListByPreSQL error sql:" + sql, e);
            throw e;
        } finally {
            JdbcUitl.closeResultSet(rs);
            JdbcUitl.closeStatement(ps);
            connHelper.release(conn);
        }
        return dataList;
    }

    @Override
    public int execByPreSQL(String sql, int timeOut, Object... param) throws Exception {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = connHelper.get();
            ps = conn.prepareStatement(sql);
            ps.setQueryTimeout(timeOut);

            if (param != null) {
                for (int i = 0; i < param.length; i++) {
                    Common.setPara(ps, param[i], i + 1);
                }
            }
            long startTime = System.currentTimeMillis();
            int result = ps.executeUpdate();
            printlnSqlAndTime(sql, startTime);
            return result;
        } catch (SQLException e) {
            logger.error("execByPreSQL error sql:" + sql, e);
            throw e;
        } finally {
            JdbcUitl.closeStatement(ps);
            connHelper.release(conn);
        }
    }

    @Override
    public int updateByPreSql(String sql, int timeOut, Object... param) throws Exception {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            //conn = connHelper.getReadConnection();
            //modify by haoxb 2012-09-27
            conn = connHelper.get();
            ps = conn.prepareStatement(sql);
            ps.setQueryTimeout(timeOut);

            if (param != null) {
                for (int i = 0; i < param.length; i++) {
                    Common.setPara(ps, param[i], i + 1);
                }
            }
            long startTime = System.currentTimeMillis();
            int result = ps.executeUpdate();
            printlnSqlAndTime(sql, startTime);
            return result;
        } catch (SQLException e) {
            logger.error("execByPreSQL error sql:" + sql, e);
            throw e;
        } finally {
            JdbcUitl.closeStatement(ps);
            connHelper.release(conn);
        }
    }

    @Override
    public <T, I> List<T> getListByIDS(Class<T> clazz, I[] ids, int timeOut) throws Exception {
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        List<T> dataList = null;
        OutSQL sql = new OutSQL();
        try {
            conn = connHelper.getReadConnection();
            ps = psCreater.createGetByIDS(clazz, conn, ids, sql);
            ps.setQueryTimeout(timeOut);
            long startTime = System.currentTimeMillis();
            rs = ps.executeQuery();
            printlnSqlAndTime(sql.getRealSql(), startTime);
            dataList = populateData(rs, clazz);
        } catch (SQLException e) {
            logger.error("getListByIDS error sql:" + sql.getSql(), e);
            throw e;
        } finally {
            JdbcUitl.closeResultSet(rs);
            JdbcUitl.closeStatement(ps);
            connHelper.release(conn);
        }
        return dataList;
    }

    @Override
    public Object insert(Object bean, int timeOut) throws Exception {
        Class<?> beanCls = bean.getClass();

        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        Object rst = null;
        OutSQL sql = new OutSQL();
        try {
            conn = connHelper.get();
            ps = psCreater.createInsert(bean, conn, sql);
            ps.setQueryTimeout(timeOut);
            long startTime = System.currentTimeMillis();
            ps.executeUpdate();
            printlnSqlAndTime(sql.getRealSql(), startTime);
            boolean isProc = false;
            Class<?>[] clsAry = ps.getClass().getInterfaces();
            for (Class<?> cls : clsAry) {
                if (cls == CallableStatement.class) {
                    isProc = true;
                    break;
                }
            }

            List<java.lang.reflect.Field> identityFields = Common.getIdentityFields(beanCls);
            if (isProc) {
                if (identityFields.size() == 1) {
                    rst = ((CallableStatement) ps).getObject(Common.getDBCloumnName(beanCls, identityFields.get(0)));
                }
            } else {
                if (identityFields.size() == 1) {
                    rs = ps.getGeneratedKeys();
                    if (rs.next()) {
                        List<Field> idFieldList = Common.getIdFields(beanCls);
                        if (idFieldList.size() == 1) {
                            if (idFieldList.get(0).getType() == int.class || idFieldList.get(0).getType() == Integer.class) {
                                rst = rs.getInt(1);
                            } else if (idFieldList.get(0).getType() == long.class || idFieldList.get(0).getType() == Long.class) {
                                rst = rs.getLong(1);
                            } else if (idFieldList.get(0).getType() == String.class) {
                                rst = rs.getString(1);
                            } else {
                                rst = rs.getObject(1);
                            }
                        } else {
                            rst = rs.getObject(1);
                        }
                    }
                } else if (identityFields.size() == 0) {
                    List<java.lang.reflect.Field> idFields = Common.getIdFields(beanCls);
                    if (idFields.size() == 1) {
                        Field id = idFields.get(0);
                        id.setAccessible(true);
                        rst = id.get(bean);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("insert error sql:" + sql.getSql(), e);
            throw e;
        } finally {
            JdbcUitl.closeResultSet(rs);
            JdbcUitl.closeStatement(ps);
            connHelper.release(conn);
        }

        return rst;
    }

    @Override
    public int upateEntity(Object bean, int timeOut) throws Exception {
        Connection conn = null;
        PreparedStatement ps = null;
        OutSQL sql = new OutSQL();
        try {
            conn = connHelper.get();
            ps = psCreater.createUpdateEntity(bean, conn, sql);
            ps.setQueryTimeout(timeOut);
            long startTime = System.currentTimeMillis();
            int rows = ps.executeUpdate();
            printlnSqlAndTime(sql.getRealSql(), startTime);
            return rows;
        } catch (Exception e) {
            logger.error("upateEntity error sql:" + sql.getSql(), e);
            throw e;
        } finally {
            JdbcUitl.closeStatement(ps);
            connHelper.release(conn);
        }
    }

    @Override
    public <I> int updateByID(Class<?> clazz, String updateStatement, I id, int timeOut) throws Exception {
        Connection conn = null;
        PreparedStatement ps = null;
        OutSQL sql = new OutSQL();
        try {
            conn = connHelper.get();
            ps = psCreater.createUpdateByID(clazz, conn, updateStatement, id, sql);
            ps.setQueryTimeout(timeOut);
            long startTime = System.currentTimeMillis();
            int rows = ps.executeUpdate();
            printlnSqlAndTime(sql.getRealSql(), startTime);
            return rows;
        } catch (Exception e) {
            logger.error("upateByID error sql:" + sql.getSql(), e);
            throw e;
        } finally {
            JdbcUitl.closeStatement(ps);
            connHelper.release(conn);
        }
    }

    @Override
    public <I> int deleteByID(Class<?> clazz, I id, int timeOut) throws Exception {
        Connection conn = null;
        PreparedStatement ps = null;
        OutSQL sql = new OutSQL();
        try {
            conn = connHelper.get();
            ps = psCreater.createDelete(clazz, conn, id, sql);
            ps.setQueryTimeout(timeOut);
            long startTime = System.currentTimeMillis();
            //            ps.execute();
            int rows = ps.executeUpdate();
            printlnSqlAndTime(sql.getRealSql(), startTime);
            return rows;
        } catch (Exception e) {
            logger.error("deleteByID error sql:" + sql.getSql(), e);
            throw e;
        } finally {
            JdbcUitl.closeStatement(ps);
            connHelper.release(conn);
        }
    }

    @Override
    public <I> int deleteByIDS(Class<?> clazz, I[] ids, int timeOut) throws Exception {
        Connection conn = null;
        PreparedStatement ps = null;
        OutSQL sql = new OutSQL();
        try {
            conn = connHelper.get();
            ps = psCreater.createDeleteByIDS(clazz, conn, ids, sql);
            ps.setQueryTimeout(timeOut);
            long startTime = System.currentTimeMillis();
            //            ps.execute();
            int rows = ps.executeUpdate();
            printlnSqlAndTime(sql.getRealSql(), startTime);
            return rows;
        } catch (Exception e) {
            logger.error("deleteByIDS error sql:" + sql.getSql(), e);
            throw e;
        } finally {
            JdbcUitl.closeStatement(ps);
            connHelper.release(conn);
        }
    }

    @Override
    public <I> Object getById(Class<?> clazz, I id, int timeOut) throws Exception {
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        List<?> dataList = null;
        OutSQL sql = new OutSQL();
        try {
            // 2011-05-24 使用只读连接
            //			conn = connHelper.get();
            conn = connHelper.getReadConnection();

            ps = psCreater.createGetEntity(clazz, conn, id, sql);
            ps.setQueryTimeout(timeOut);
            long startTime = System.currentTimeMillis();
            rs = ps.executeQuery();
            printlnSqlAndTime(sql.getRealSql(), startTime);
            dataList = populateData(rs, clazz);
        } catch (Exception e) {
            logger.error("getById error sql:" + sql.getSql(), e);
            throw e;
        } finally {
            JdbcUitl.closeResultSet(rs);
            JdbcUitl.closeStatement(ps);
            connHelper.release(conn);
        }

        if (dataList != null && dataList.size() > 0) {
            return dataList.get(0);
        } else {
            return null;
        }
    }

    @Override
    public int countByWhere(Class<?> clazz, String condition, int timeOut) throws Exception {
        condition = SqlInjectHelper.simpleFilterSql(condition);

        int count = 0;
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        OutSQL sql = new OutSQL();
        try {
            conn = connHelper.getReadConnection();

            ps = psCreater.createGetCount(clazz, conn, condition, sql);
            ps.setQueryTimeout(timeOut);
            long startTime = System.currentTimeMillis();
            rs = ps.executeQuery();
            printlnSqlAndTime(sql.getRealSql(), startTime);
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (Exception e) {
            logger.error("countBySql error sql:" + sql.getSql(), e);
            throw e;
        } finally {
            JdbcUitl.closeResultSet(rs);
            JdbcUitl.closeStatement(ps);
            connHelper.release(conn);
        }
        return count;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public <T> List<T> pageListByWhere(Class<T> clazz, String condition, String columns, int page, int pageSize, String orderBy, int timeOut) throws Exception {
        columns = SqlInjectHelper.simpleFilterSql(columns);
        condition = SqlInjectHelper.simpleFilterSql(condition);
        orderBy = SqlInjectHelper.simpleFilterSql(orderBy);

        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        List dataList = null;
        OutSQL sql = new OutSQL();
        try {
            // 2011-05-24 使用只读连接
            //   conn = connHelper.get();
            conn = connHelper.getReadConnection();

            ps = psCreater.createGetByPage(clazz, conn, condition, columns, page, pageSize, orderBy, sql);
            ps.setQueryTimeout(timeOut);
            long startTime = System.currentTimeMillis();
            rs = ps.executeQuery();
            printlnSqlAndTime(sql.getRealSql(), startTime);
            dataList = populateData(rs, clazz);
        } catch (Exception e) {
            logger.error("pageListBySql error sql:" + sql.getSql(), e);
            throw e;
        } finally {
            JdbcUitl.closeResultSet(rs);
            JdbcUitl.closeStatement(ps);
            connHelper.release(conn);
        }
        return dataList;
    }

    @Override
    public int updateByWhere(Class<?> clazz, String updateStatement, String where, int timeOut) throws Exception {
        where = SqlInjectHelper.simpleFilterSql(where);
        updateStatement = SqlInjectHelper.simpleFilterSql(updateStatement);

        Connection conn = null;
        PreparedStatement ps = null;
        OutSQL out = new OutSQL();
        try {
            conn = connHelper.get();
            ps = psCreater.createUpdateByCustom(clazz, conn, updateStatement, where, out);
            ps.setQueryTimeout(timeOut);
            long startTime = System.currentTimeMillis();
            int rows = ps.executeUpdate();
            printlnSqlAndTime(out.getRealSql(), startTime);
            return rows;
        } catch (Exception e) {
            logger.error("updateBySql error sql:" + out.getSql(), e);
            throw e;
        } finally {
            JdbcUitl.closeStatement(ps);
            connHelper.release(conn);
        }
    }

    @Override
    public int deleteByWhere(Class<?> clazz, String where, int timeOut) throws Exception {
        where = SqlInjectHelper.simpleFilterSql(where);
        Connection conn = null;
        PreparedStatement ps = null;
        OutSQL sql = new OutSQL();
        try {
            conn = connHelper.get();
            ps = psCreater.createDeleteByCustom(clazz, conn, where, sql);
            ps.setQueryTimeout(timeOut);
            long startTime = System.currentTimeMillis();
            //            ps.execute();
            int rows = ps.executeUpdate();
            printlnSqlAndTime(sql.getRealSql(), startTime);
            return rows;
        } catch (Exception e) {
            logger.error("deleteBySql error sql:" + sql.getSql(), e);
            throw e;
        } finally {
            JdbcUitl.closeStatement(ps);
            connHelper.release(conn);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public <T> List<T> getListByWhere(Class<T> clazz, String columns, String condition, String orderBy, String limit, int timeOut) throws Exception {
        columns = SqlInjectHelper.simpleFilterSql(columns);
        condition = SqlInjectHelper.simpleFilterSql(condition);
        orderBy = SqlInjectHelper.simpleFilterSql(orderBy);

        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        List dataList = null;
        OutSQL sql = new OutSQL();
        try {
            conn = connHelper.getReadConnection();

            ps = psCreater.createGetByCustom(clazz, conn, columns, condition, orderBy, limit, sql);
            ps.setQueryTimeout(timeOut);
            long startTime = System.currentTimeMillis();
            rs = ps.executeQuery();
            printlnSqlAndTime(sql.getRealSql(), startTime);
            dataList = populateData(rs, clazz);
        } catch (SQLException e) {
            logger.error("getListBySql error sql:" + sql.getSql(), e);
            throw e;
        } finally {
            JdbcUitl.closeResultSet(rs);
            JdbcUitl.closeStatement(ps);
            connHelper.release(conn);
        }
        return dataList;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public <T> List<T> getListBySql(Class<T> clazz, String sqlquery, int timeOut) throws Exception {

        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        List dataList = null;
        OutSQL sql = new OutSQL();
        try {
            conn = connHelper.getReadConnection();
            ps = conn.prepareStatement(sqlquery);

            ps.setQueryTimeout(timeOut);
            long startTime = System.currentTimeMillis();
            rs = ps.executeQuery();
            printlnSqlAndTime(sql.getRealSql(), startTime);
            dataList = populateData(rs, clazz);
        } catch (SQLException e) {
            logger.error("getListBySql error sql:" + sql.getSql(), e);
            throw e;
        } finally {
            JdbcUitl.closeResultSet(rs);
            JdbcUitl.closeStatement(ps);
            connHelper.release(conn);
        }
        return dataList;
    }

}