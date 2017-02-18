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
    protected int insertUpdateTimeOut = 5;

    private static final DBTemplateClient proxy = new DBTemplateClient();

    public DBTemplateClient() {
    }

    public static DBTemplateClient init(String configPath) throws Exception {
        ConnectionHelper ch = new ConnectionHelper(configPath);
        AbstractDAO sqlDAO = null;
        IStatementCreater creater = new MysqlPSCreater();
        sqlDAO = new BaseDAOImpl(creater);
        sqlDAO.setConnHelper(ch);
        proxy.connHelper = ch;
        proxy.sql = sqlDAO;
        proxy.presql = sqlDAO;

        logger.info("init DBTemplateClient success");
        return proxy;
    }

    @Override
    public Object insert(Object t) throws Exception {
        return sql.insert(t, qurryTimeOut);
    }

    @Override
    public void upateEntity(Object t) throws Exception {

    }

    @Override
    public <I> void updateByID(Class<?> clazz, String updateStatement, I id, int timeOut) throws Exception {

    }

    @Override
    public void updateByWhere(Class<?> clazz, String updateStatement, String condition, int timeOut) throws Exception {

    }

    @Override
    public <I> void deleteByID(Class<?> clazz, I id, int timeOut) throws Exception {

    }

    @Override
    public <I> void deleteByIDS(Class<?> clazz, I[] ids, int timeOut) throws Exception {

    }

    @Override
    public void deleteByWhere(Class<?> clazz, String condition, int timeOut) throws Exception {

    }

    @Override
    public <I> Object getById(Class<?> clazz, I id, int timeOut) throws Exception {
        return null;
    }

    @Override
    public <T, I> List<T> getListByIDS(Class<T> clazz, I[] ids, int timeOut) throws Exception {
        return null;
    }

    @Override
    public <T> List<T> getListByWhere(Class<T> clazz, String columns, String condition, String orderBy, String limit, int timeOut) throws Exception {
        return null;
    }

    @Override
    public <T> List<T> pageListByWhere(Class<T> clazz, String condition, String columns, int page, int pageSize, String orderBy, int timeOut) throws Exception {
        return null;
    }

    @Override
    public int countBySql(Class<?> clazz, String condition, int timeOut) throws Exception {
        return 0;
    }

    /**
     * 这样是可以实现  只是如果每个方法都这么实现 就有点太low了
     */
    @Override
    public <T> List<T> getListByPreSQL(Class<T> clazz, String sql, int timeOut, Object... param) throws Exception {
        beginTransaction();
        try {
            List<T> result = this.presql.getListByPreSQL(clazz, sql, timeOut, param);
            commitTransaction();
            return result;
        } catch (Exception e) {
            rollbackTransaction();
            throw e;
        } finally {
            endTransaction();
        }
    }

    @Override
    public int execByPreSQL(String presql, int timeOut, Object... param) throws Exception {
        return 0;
    }

    @Override
    public int countByPreSQL(String sql, int timeOut, Object... params) throws Exception {
        return 0;
    }

    public DBTemplateClient withTranction(boolean with) {
        LocalParam param = localParams.get();
        if (param == null) {
            param = new LocalParam();
            localParams.set(param);
        }
        param.setWithTranc(with);
        return proxy;
    }

    public DBTemplateClient timeOut(int time) {
        LocalParam param = localParams.get();
        if (param == null) {
            param = new LocalParam();
            localParams.set(param);
        }
        param.setTimeout(time);
        return proxy;
    }

    public Connection getConn() throws Exception {
        return this.connHelper.get();
    }

    public void release(Connection conn) {
        this.connHelper.release(conn);
    }

    private void beginTransaction() throws Exception {
        beginTransaction(Connection.TRANSACTION_READ_COMMITTED);
    }

    //底层对应的都需要修改
    private void beginTransaction(int level) throws Exception {
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
    private void commitTransaction() throws Exception {
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
    private void rollbackTransaction() throws Exception {
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
    private void endTransaction() throws Exception {
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