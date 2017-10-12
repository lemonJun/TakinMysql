package com.lemonjun.mysql.orm.dbms;

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 用于合适的方式关闭Connection,Statement,ResultSet.
 * </br>
 * close by the right way
 * 
 * @author renjun
 *
 */
public class DbUtils {

    private static final Logger logger = LoggerFactory.getLogger(DbUtils.class);

    /**
     * 从jdbc连串接串中分离出主机
     * @param jdbcUrl
     * @return
     */
    public static String getIpFromUrl(String jdbcUrl) {
        String ip = "";
        String tmpSocket = jdbcUrl.substring(jdbcUrl.indexOf("//") + 2);
        ip = tmpSocket.substring(0, tmpSocket.indexOf(":"));
        return ip;
    }

    /**
     * 判断主机是否活着
     * @param hostIp
     * @return
     */
    public static boolean checkHostLive(String hostIp) {
        boolean rtn = true;
        int checkTimes = 0;
        try {
            InetAddress address = InetAddress.getByName(hostIp);
            rtn = address.isReachable(1000);
            while (!rtn && (checkTimes < 10)) {
                rtn = address.isReachable(1000);
                checkTimes++;
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            rtn = false;
        }
        logger.debug("HaPlus: checkHostLive hostIp : rtn" + hostIp + " : " + rtn);
        return rtn;
    }

    /**
     * close a connection
     * @param connection
     */
    public static void closeConnection(Connection connection) {
        if (connection == null) {
            return;
        }
        try {
            connection.close();
        } catch (SQLException ex) {
            logger.debug("Could not close JDBC Connection", ex);
        } catch (Throwable ex) {
            logger.debug("Unexpected exception on closing JDBC Connection", ex);
        }
    }

    /**
     * close a statement
     * @param stmt
     */
    public static void closeStatement(Statement stmt) {
        if (stmt == null) {
            return;
        }
        try {
            stmt.close();
        } catch (SQLException ex) {
            logger.debug("Could not close JDBC Statement", ex);
        } catch (Throwable ex) {
            logger.debug("Unexpected exception on closing JDBC Statement", ex);
        }
    }

    /**
     * close a ResultSet
     * @param rs
     */
    public static void closeResultSet(ResultSet rs) {
        if (rs == null) {
            return;
        }
        try {
            rs.close();
        } catch (SQLException ex) {
            logger.debug("Could not close JDBC ResultSet", ex);
        } catch (Throwable ex) {
            logger.debug("Unexpected exception on closing JDBC ResultSet", ex);
        }
    }
    
    /**
     * close a series of database-related activities(resultset, statement, connection).
     * @param rs
     * @param stmt
     * @param connection
     */
    public static void close(ResultSet rs, Statement stmt, Connection connection) {
        closeResultSet(rs);
        closeStatement(stmt);
        closeConnection(connection);

    }
}
