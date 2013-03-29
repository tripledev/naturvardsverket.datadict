package eionet.util.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
 *
 */
public class SQL {

    /** */
    private static final Logger LOGGER = Logger.getLogger(SQL.class);

    /**
     *
     * @param sql
     * @param inParams
     * @param conn
     * @return
     * @throws SQLException
     */
    public static PreparedStatement preparedStatement(String sql, INParameters inParams, Connection conn) throws SQLException {

        PreparedStatement stmt = conn.prepareStatement(sql);
        populate(stmt, inParams);
        return stmt;
    }

    /**
     *
     * @param parameterizedSQL
     * @param values
     * @param conn
     * @return
     * @throws SQLException
     */
    public static PreparedStatement preparedStatement(String parameterizedSQL, Collection<?> values, Connection conn)
    throws SQLException {

        PreparedStatement stmt = conn.prepareStatement(parameterizedSQL);
        populate(stmt, values);
        return stmt;
    }

    /**
     *
     * @param parameterizedSQL
     * @param values
     * @param conn
     * @param autoGeneratedKeys
     * @return PreparedStatement
     * @throws SQLException
     */
    public static PreparedStatement preparedStatement(String parameterizedSQL, List<?> values, Connection conn,
            boolean autoGeneratedKeys) throws SQLException {

        PreparedStatement pstmt =
            conn.prepareStatement(parameterizedSQL, autoGeneratedKeys ? Statement.RETURN_GENERATED_KEYS
                    : Statement.NO_GENERATED_KEYS);
        for (int i = 0; values != null && i < values.size(); i++) {
            pstmt.setObject(i + 1, values.get(i));
        }
        return pstmt;
    }


    /**
     *
     * @param stmt
     * @param values
     * @throws SQLException
     */
    public static void populate(PreparedStatement stmt, Collection<?> values) throws SQLException {

        int i = 1;
        for (Object value : values) {
            stmt.setObject(i, value);
            i++;
        }
    }

    /**
     *
     * @param stmt
     * @param inParams
     * @throws SQLException
     */
    public static void populate(PreparedStatement stmt, INParameters inParams) throws SQLException {

        for (int i = 0; stmt != null && inParams != null && i < inParams.size(); i++) {

            Integer sqlType = inParams.getSQLType(i);
            if (sqlType == null) {
                stmt.setObject(i + 1, inParams.getValue(i));
            } else {
                stmt.setObject(i + 1, inParams.getValue(i), sqlType.intValue());
            }
        }
    }

    /**
     *
     *
     * @param tableName
     * @param columns
     * @return
     */
    public static String insertStatement(String tableName, LinkedHashMap columns) {

        if (columns == null || columns.size() == 0) {
            return null;
        }

        StringBuffer buf = new StringBuffer("insert into ");
        buf.append(tableName).append(" (");

        // add names
        Iterator iter = columns.keySet().iterator();
        for (int i = 0; iter.hasNext(); i++) {
            if (i > 0) {
                buf.append(", ");
            }
            buf.append(iter.next());
        }

        buf.append(") values (");

        // add values
        iter = columns.values().iterator();
        for (int i = 0; iter.hasNext(); i++) {
            if (i > 0) {
                buf.append(", ");
            }
            buf.append(iter.next());
        }

        return buf.append(")").toString();
    }

    /**
     *
     *
     * @param tableName
     * @param columns
     * @return
     */
    public static String updateStatement(String tableName, LinkedHashMap columns) {

        if (columns == null || columns.size() == 0) {
            return null;
        }

        StringBuffer buf = new StringBuffer("update ");
        buf.append(tableName).append(" set ");

        Iterator colNames = columns.keySet().iterator();
        for (int i = 0; colNames.hasNext(); i++) {
            if (i > 0) {
                buf.append(", ");
            }
            String colName = colNames.next().toString();
            buf.append(colName).append("=").append(columns.get(colName));
        }

        return buf.toString();
    }

    /**
     *
     * @param s
     * @return
     */
    public static String surroundWithApostrophes(String s) {

        if (s == null) {
            return null;
        } else {
            StringBuffer buf = new StringBuffer("'");
            return buf.append(s).append("'").toString();
        }
    }

    /**
     *
     * @param preparedSQL
     * @param inParams
     * @param conn
     * @throws SQLException
     */
    public static void executeUpdate(String preparedSQL, INParameters inParams, Connection conn) throws SQLException {

        PreparedStatement stmt = null;
        try {
            stmt = SQL.preparedStatement(preparedSQL, inParams, conn);
            stmt.executeUpdate();
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
                LOGGER.error(e.toString(), e);
            }
        }
    }

    /**
     *
     * @param conn
     */
    public static void close(Connection conn) {

        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                LOGGER.error(e.toString(), e);
            }
        }
    }

    /**
     *
     * @param stmt
     */
    public static void close(Statement stmt) {

        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                LOGGER.error(e.toString(), e);
            }
        }
    }

    /**
     *
     * @param rs
     */
    public static void close(ResultSet rs) {

        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                LOGGER.error(e.toString(), e);
            }
        }
    }

    /**
     *
     * @param conn
     */
    public static void rollback(Connection conn) {

        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException e) {
                LOGGER.error(e.toString(), e);
            }
        }
    }

    /**
     * Returns a new string by putting the given input string between apostrophes and SQL-escaping all other apostrophes inside it.
     *
     * @param str
     *            Given input string.
     *
     * @return
     */
    public static String toLiteral(String str) {

        if (str == null) {
            return null;
        }

        StringBuffer result = new StringBuffer("'");
        for (int i = 0; i < str.length(); i++) {

            char c = str.charAt(i);
            if (c == '\'') {
                result.append("''");
            } else {
                result.append(c);
            }
        }
        result.append('\'');

        return result.toString();
    }

    /**
     *
     * @param sql
     * @param conn
     * @return Object
     * @throws SQLException
     */
    public static Object executeSingleReturnValueQuery(String sql, Connection conn) throws SQLException {

        ResultSet rs = null;
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            return (rs != null && rs.next()) ? rs.getObject(1) : null;
        } finally {
            SQL.close(rs);
            SQL.close(stmt);
        }
    }

    /**
     *
     * @param parameterizedSQL
     * @param values
     * @param conn
     * @return Object
     * @throws SQLException
     */
    public static Object executeSingleReturnValueQuery(String parameterizedSQL, List<?> values, Connection conn)
    throws SQLException {

        ResultSet rs = null;
        PreparedStatement pstmt = null;
        try {
            pstmt = preparedStatement(parameterizedSQL, values, conn);
            rs = pstmt.executeQuery();
            return (rs != null && rs.next()) ? rs.getObject(1) : null;
        } finally {
            SQL.close(rs);
            SQL.close(pstmt);
        }
    }

    /**
     *
     * @param parameterizedSQL
     * @param values
     * @param conn
     * @return int
     * @throws SQLException
     */
    public static int executeUpdate(String parameterizedSQL, List<?> values, Connection conn) throws SQLException {

        PreparedStatement pstmt = null;
        try {
            pstmt = preparedStatement(parameterizedSQL, values, conn);
            return pstmt.executeUpdate();
        } finally {
            try {
                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (SQLException e) {
                LOGGER.error(e.toString(), e);
            }
        }
    }

    /**
     *
     * @param parameterizedSQL
     * @param values
     * @param conn
     * @return int
     * @throws SQLException
     */
    public static int executeUpdateReturnAutoID(String parameterizedSQL, List<?> values, Connection conn) throws SQLException {

        PreparedStatement pstmt = null;
        try {
            pstmt = preparedStatement(parameterizedSQL, values, conn, true);
            pstmt.executeUpdate();
            ResultSet genKeys = pstmt.getGeneratedKeys();
            if (genKeys.next()) {
                return genKeys.getInt(1);
            } else {
                throw new SQLException("No auto-generated keys returned!");
            }
        } finally {
            SQL.close(pstmt);
        }
    }

    /**
     *
     * @param sql
     * @param conn
     * @return int
     * @throws SQLException
     */
    public static int executeUpdate(String sql, Connection conn) throws SQLException {

        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            return stmt.executeUpdate(sql);
        } finally {
            SQL.close(stmt);
        }
    }
}
