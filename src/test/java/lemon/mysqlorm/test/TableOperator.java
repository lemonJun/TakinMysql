package lemon.mysqlorm.test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

public class TableOperator {
    private DataSource dataSource;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private static final int COUNT = 5;

    public TableOperator() {
    }

    public void tearDown() throws Exception {
        try {
            dropTable();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insert() throws Exception {
        StringBuffer ddl = new StringBuffer();
        ddl.append("INSERT INTO t_big (");
        for (int i = 0; i < COUNT; ++i) {
            if (i != 0) {
                ddl.append(", ");
            }
            ddl.append("F" + i);
        }
        ddl.append(") VALUES (");
        for (int i = 0; i < COUNT; ++i) {
            if (i != 0) {
                ddl.append(", ");
            }
            ddl.append("?");
        }
        ddl.append(")");
        Connection conn = dataSource.getConnection();
        System.out.println(ddl.toString());
        PreparedStatement stmt = conn.prepareStatement(ddl.toString());
        for (int i = 0; i < COUNT; ++i) {
            stmt.setInt(i + 1, i);
        }
        stmt.execute();
        stmt.close();
        conn.close();
    }

    private void dropTable() throws SQLException {
        Connection conn = dataSource.getConnection();
        Statement stmt = conn.createStatement();
        stmt.execute("DROP TABLE t_big");
        stmt.close();
        conn.close();
    }

    public void createTable() throws SQLException {
        StringBuffer ddl = new StringBuffer();
        ddl.append("CREATE TABLE t_big (FID INT ");
        for (int i = 0; i < COUNT; ++i) {
            ddl.append(", ");
            ddl.append("F" + i);
            ddl.append(" varchar2(10)");
        }
        ddl.append(")");
        Connection conn = dataSource.getConnection();
        Statement stmt = conn.createStatement();
        System.out.println(ddl.toString());
        stmt.execute(ddl.toString());
        stmt.close();
        conn.close();
    }
}