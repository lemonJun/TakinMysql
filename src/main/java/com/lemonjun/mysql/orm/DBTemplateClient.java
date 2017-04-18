package com.lemonjun.mysql.orm;

import java.sql.Connection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lemonjun.mysql.orm.query.AbstractDAO;
import com.lemonjun.mysql.orm.query.BaseDAOImpl;
import com.lemonjun.mysql.orm.query.ConnectionHelper;
import com.lemonjun.mysql.orm.query.IPreStatementDAO;
import com.lemonjun.mysql.orm.query.IStatementCreater;
import com.lemonjun.mysql.orm.query.IStatementDAO;
import com.lemonjun.mysql.orm.query.MysqlPSCreater;

/**
 * 主要用来生成数据库的实例 提供事务等功能
 * 实现事务的方式：不同的DAO获取到同一个CONNECTION  
 * 不过 最好的方式是withTransaction.sql(xxxx)  不知道是不是可以用模板方法   关键sql是直接调用的  中间一直没能加入代理成
 * 
 * @author WangYazhou
 * @date 2016年6月8日 上午11:11:15
 * @see
 */
public class DBTemplateClient implements DBOperations {

    private static final Logger logger = LoggerFactory.getLogger(DBTemplateClient.class);

    private IStatementDAO sql = null;
    private IPreStatementDAO presql = null;
    public ConnectionHelper connHelper;//

    private static final ThreadLocal<LocalParam> localParams = new ThreadLocal<LocalParam>();

    protected int qurryTimeOut = 2;
    protected int insertUpdateTimeOut = 3;

    public DBTemplateClient(String configPath) throws Exception {
        ConnectionHelper ch = new ConnectionHelper(configPath);
        AbstractDAO sqlDAO = null;
        IStatementCreater creater = new MysqlPSCreater();
        sqlDAO = new BaseDAOImpl(creater);
        sqlDAO.setConnHelper(ch);
        this.connHelper = ch;
        this.sql = sqlDAO;
        this.presql = sqlDAO;

        logger.info("init DBTemplateClient success");
    }

    @Override
    public Object insert(Object t) throws Exception {
        return sql.insert(t, insertUpdateTimeOut);
    }

    @Override
    public void upateEntity(Object t) throws Exception {
        sql.upateEntity(t, insertUpdateTimeOut);
    }

    @Override
    public <I> void updateByID(Class<?> clazz, String updateStatement, I id) throws Exception {
        sql.updateByID(clazz, updateStatement, id, insertUpdateTimeOut);
    }

    @Override
    public void updateByWhere(Class<?> clazz, String updateStatement, String condition) throws Exception {
        sql.updateByWhere(clazz, updateStatement, condition, insertUpdateTimeOut);
    }

    @Override
    public <I> void deleteByID(Class<?> clazz, I id) throws Exception {
        sql.deleteByID(clazz, id, insertUpdateTimeOut);
    }

    @Override
    public <I> void deleteByIDS(Class<?> clazz, I[] ids) throws Exception {
        sql.deleteByIDS(clazz, ids, insertUpdateTimeOut);
    }

    @Override
    public void deleteByWhere(Class<?> clazz, String where) throws Exception {
        sql.deleteByWhere(clazz, where, insertUpdateTimeOut);
    }

    @Override
    public <I> Object getById(Class<?> clazz, I id) throws Exception {
        return sql.getById(clazz, id, qurryTimeOut);
    }

    @Override
    public <T, I> List<T> getListByIDS(Class<T> clazz, I[] ids) throws Exception {
        return sql.getListByIDS(clazz, ids, qurryTimeOut);
    }

    @Override
    public <T> List<T> getListByWhere(Class<T> clazz, String columns, String where, String orderBy, String limit) throws Exception {
        return sql.getListByWhere(clazz, columns, where, orderBy, limit, qurryTimeOut);
    }

    @Override
    public <T> List<T> pageListByWhere(Class<T> clazz, String where, String columns, int page, int pageSize, String orderBy) throws Exception {
        return sql.pageListByWhere(clazz, where, columns, page, pageSize, orderBy, qurryTimeOut);
    }

    @Override
    public int countBySql(Class<?> clazz, String where) throws Exception {
        return sql.countBySql(clazz, where, qurryTimeOut);
    }

    /**
     * 这样是可以实现  只是如果每个方法都这么实现 就有点太low了
     */
    @Override
    public <T> List<T> getListByPreSQL(Class<T> clazz, String sql, Object... param) throws Exception {
        return presql.getListByPreSQL(clazz, sql, qurryTimeOut, param);
    }

    @Override
    public int execByPreSQL(String sql, Object... param) throws Exception {
        return presql.execByPreSQL(sql, insertUpdateTimeOut, param);
    }

    @Override
    public int countByPreSQL(String sql, Object... params) throws Exception {
        return presql.countByPreSQL(sql, qurryTimeOut, params);
    }

    //    public DBTemplateClient withTranction(boolean with) {
    //        LocalParam param = localParams.get();
    //        if (param == null) {
    //            param = new LocalParam();
    //            localParams.set(param);
    //        }
    //        param.setWithTranc(with);
    //        return proxy;
    //    }
    //
    //    public DBTemplateClient timeOut(int time) {
    //        LocalParam param = localParams.get();
    //        if (param == null) {
    //            param = new LocalParam();
    //            localParams.set(param);
    //        }
    //        param.setTimeout(time);
    //        return proxy;
    //    }

    public Connection getConn() throws Exception {
        return this.connHelper.get();
    }

    public void release(Connection conn) {
        this.connHelper.release(conn);
    }

    //事务的级别
    public void beginTransaction() throws Exception {
        beginTransaction(Connection.TRANSACTION_READ_COMMITTED);
    }

    //底层对应的都需要修改
    public void beginTransaction(int level) throws Exception {
        LocalParam param = localParams.get();
        if (param == null || !param.isWithTranc()) {
            return;
        }
        Connection conn = connHelper.get();
        if (conn != null) {
            try {
                conn.setAutoCommit(false);
                conn.setTransactionIsolation(level);
                connHelper.lockConn(conn);
                logger.info(String.format("Tid:%s open transaction", Thread.currentThread().getId()));
            } catch (Exception ex) {
                logger.error(String.format("Tid:%s error", Thread.currentThread().getId()), ex);
            }
        } else {
            throw new Exception("conn is null when beginTransaction");
        }
    }

    //对应的实现都需要修改
    public void commitTransaction() throws Exception {
        LocalParam param = localParams.get();
        if (param == null || !param.isWithTranc()) {
            return;
        }
        Connection conn = connHelper.get();
        if (conn != null) {
            conn.commit();
            logger.info(String.format("Tid:%s commit transaction", Thread.currentThread().getId()));
        } else {
            throw new Exception("conn is null when commitTransaction");
        }
    }

    //改成localparam中就可以
    public void rollbackTransaction() throws Exception {
        LocalParam param = localParams.get();
        if (param == null || !param.isWithTranc()) {
            return;
        }
        Connection conn = connHelper.get();
        if (conn != null) {
            conn.rollback();
            logger.info(String.format("Tid:%s rollback transaction", Thread.currentThread().getId()));
        } else {
            throw new Exception("conn is null when rollbackTransaction");
        }
    }

    /**
     * 结束事务
     * @throws Exception
     */
    public void endTransaction() throws Exception {
        LocalParam param = localParams.get();
        if (param == null || !param.isWithTranc()) {
            return;
        }
        Connection conn = connHelper.get();
        if (conn != null) {
            try {
                //恢复默认
                conn.setAutoCommit(true);
                conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            } finally {
                connHelper.unLockConn();
                connHelper.release(conn);
                logger.info(String.format("Tid:%s end transaction", Thread.currentThread().getId()));
            }
        } else {
            throw new Exception("conn is null when endTransaction");
        }
    }
}