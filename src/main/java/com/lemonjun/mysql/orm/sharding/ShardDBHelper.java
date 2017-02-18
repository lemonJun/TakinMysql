package com.lemonjun.mysql.orm.sharding;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.lemonjun.mysql.orm.BDProvider;
import com.lemonjun.mysql.orm.annotation.NotNull;
import com.lemonjun.mysql.orm.util.CollectionUtils;
import com.lemonjun.mysql.orm.util.JdbcUitl;
import com.lemonjun.mysql.orm.util.OutSQL;
import com.lemonjun.mysql.orm.util.StringUtils;

/**
 * 
 *
 *
 * @author wangyazhou
 * @version 1.0
 * @date 2016年4月20日 下午7:57:40
 * @see
 * @since
 */
public class ShardDBHelper {

    private static final Logger logger = LoggerFactory.getLogger(ShardDBHelper.class);

    /**
     * 分表条件下新增一条记录
     * @param bean
     * @param table
     * @throws Exception
     */
    public static void insertOne(@NotNull Object bean, String table) throws Exception {
        Connection conn = null;
        PreparedStatement ps = null;
        OutSQL sql = new OutSQL();
        try {
            Preconditions.checkNotNull(bean, "insert bean cannot be null");
            Preconditions.checkState(StringUtils.isNotEmpty(table), "insert table cannot be null");

            conn = BDProvider.getInst().Client().getConn();
            ps = ShardPSCreater.createInsertPs(bean, table, conn, sql);
            ps.setQueryTimeout(1);
            ps.executeUpdate();
        } catch (Exception e) {
            logger.error("shard insertOne error", e);
            throw e;
        } finally {
            JdbcUitl.closeStatement(ps);
            BDProvider.getInst().Client().release(conn);
        }
    }

    /**
     * 分表情况下的批量插入
     * @param beans
     * @param table
     * @throws Exception
     */
    public static void batchInsert(List<Object> beans, String table) throws Exception {
        Connection conn = null;
        PreparedStatement ps = null;
        OutSQL sql = new OutSQL();
        try {
            Preconditions.checkState(CollectionUtils.isNotEmpty(beans), "insert bean cannot be null");
            Preconditions.checkState(StringUtils.isNotEmpty(table), "insert table cannot be null");

            conn = BDProvider.getInst().Client().getConn();
            ps = ShardPSCreater.createInsertPs(beans.get(0), table, conn, sql);
            ps.setQueryTimeout(1);
            ps.executeUpdate();
        } catch (Exception e) {
            logger.error("shard batchInsert error", e);
            throw e;
        } finally {
            JdbcUitl.closeStatement(ps);
            BDProvider.getInst().Client().release(conn);
        }
    }

    /**
     * 分表条件下修改一条记录
     * @param bean
     * @param table
     * @throws Exception
     */
    public static void updatOne(Object bean, String table) throws Exception {
        Connection conn = null;
        PreparedStatement ps = null;
        OutSQL sql = new OutSQL();
        try {
            Preconditions.checkNotNull(bean, "insert bean cannot be null");
            Preconditions.checkState(StringUtils.isNotEmpty(table), "insert table cannot be null");
            conn = BDProvider.getInst().Client().getConn();
            ps = ShardPSCreater.createInsertPs(bean, table, conn, sql);
            ps.setQueryTimeout(1);
            ps.executeUpdate();
        } catch (Exception e) {
            logger.error("shard updatOne error", e);
            throw e;
        } finally {
            JdbcUitl.closeStatement(ps);
            BDProvider.getInst().Client().release(conn);
        }
    }

    /**
     * 分表条件下根据条件进行更新
     * @param table
     * @param set
     * @param where
     * @throws Exception
     */
    //    public static void updatByWhere(String table, String set, String where) throws Exception {
    //        Connection conn = null;
    //        PreparedStatement ps = null;
    //        OutSQL sql = new OutSQL();
    //        try {
    //            Preconditions.checkState(StringUtils.isNotEmpty(table), "insert table cannot be null");
    //            conn = BDProvider.getInst().Client().getConn();
    //            ps = ShardPSCreater.createInsertPs(null, table, conn, sql);
    //            ps.setQueryTimeout(1);
    //            ps.executeUpdate();
    //        } catch (Exception e) {
    //            logger.error("shard updatOne error", e);
    //            throw e;
    //        } finally {
    //            JdbcUitl.closeStatement(ps);
    //            BDProvider.getInst().Client().release(conn);
    //        }
    //    }

}
