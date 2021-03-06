package eionet.meta.savers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import eionet.util.Util;
import eionet.util.sql.INParameters;
import eionet.util.sql.SQL;
import eionet.util.sql.SQLGenerator;

/**
 *
 * @author Jaanus Heinlaid
 *
 */
public class NamespaceHandler extends BaseHandler {

    /** */
    private static final Logger LOGGER = Logger.getLogger(NamespaceHandler.class);

    /** */
    private String mode = null;
    private String[] nsID = null;
    private String shortName = null;
    private String fullName = null;
    private String definition = null;
    private String dsID = null;
    private String tblID = null;
    private String dsName = null;
    private String tblName = null;

    /** */
    private String lastInsertID = null;

    /**
     *
     * @param conn
     * @param req
     * @param ctx
     */
    public NamespaceHandler(Connection conn, HttpServletRequest req, ServletContext ctx) {
        this(conn, new Parameters(req), ctx);
    }

    /**
     *
     * @param conn
     * @param req
     * @param ctx
     */
    public NamespaceHandler(Connection conn, Parameters req, ServletContext ctx) {

        this.conn = conn;
        this.req = req;
        this.ctx = ctx;

        this.mode = req.getParameter("mode");
        this.nsID = req.getParameterValues("ns_id");
        this.shortName = req.getParameter("short_name");
        this.fullName = req.getParameter("fullName");
        this.definition = req.getParameter("description");
        this.dsID = req.getParameter("ds_id");
        this.tblID = req.getParameter("tbl_id");

        this.dsName = req.getParameter("ds_name");
        this.tblName = req.getParameter("tbl_name");
    }

    /**
     *
     * @param conn
     * @param req
     * @param ctx
     * @param mode
     */
    public NamespaceHandler(Connection conn, HttpServletRequest req, ServletContext ctx, String mode) {
        this(conn, req, ctx);
        this.mode = mode;
    }

    /*
     *  (non-Javadoc)
     * @see eionet.meta.savers.BaseHandler#execute_()
     */
    public void execute_() throws Exception {

        if (mode == null || (!mode.equalsIgnoreCase("add") &&
                !mode.equalsIgnoreCase("edit") &&
                !mode.equalsIgnoreCase("delete"))) {
            throw new Exception("NamespaceHandler mode unspecified!");
        }

        if (mode.equalsIgnoreCase("add")) {
            insert();
        } else if (mode.equalsIgnoreCase("edit")) {
            update();
        } else {
            delete();
        }
    }

    /**
     *
     * @throws Exception
     */
    private void insert() throws Exception {

        // at least the short_name is required
        if (Util.isEmpty(shortName)) {
            throw new Exception("NamespaceHandler: at least the short_name " +
            "is required!");
        }

        INParameters inParams = new INParameters();
        // build SQL
        SQLGenerator gen = new SQLGenerator();
        gen.setTable("NAMESPACE");
        if (!Util.isEmpty(fullName)) {
            gen.setFieldExpr("FULL_NAME", inParams.add(fullName));
        }
        if (!Util.isEmpty(shortName)) {
            gen.setFieldExpr("SHORT_NAME", inParams.add(shortName));
        }
        if (!Util.isEmpty(definition)) {
            gen.setFieldExpr("DEFINITION", inParams.add(definition));
        }

        String wrkUser = req.getParameter("wrk_user");
        if (!Util.isEmpty(wrkUser)) {
            gen.setFieldExpr("WORKING_USER", inParams.add(wrkUser));
        } else {
            gen.setFieldExpr("WORKING_USER", "NULL");
        }

        String parentNS = req.getParameter("parent_ns");
        if (!Util.isEmpty(parentNS)) {
            gen.setFieldExpr("PARENT_NS", inParams.add(parentNS, Types.INTEGER));
        }

        // execute
        String sql = gen.insertStatement();
        PreparedStatement stmt = SQL.preparedStatement(sql, inParams, conn);
        stmt.executeUpdate();
        stmt.close();

        setLastInsertID();
    }

    private void update() throws Exception {

        if (nsID == null || nsID.length == 0) {
            throw new Exception("Namespace ID not specified!");
        }

        INParameters inParams = new INParameters();
        SQLGenerator gen = new SQLGenerator();
        gen.setTable("NAMESPACE");
        if (!Util.isEmpty(fullName)) {
            gen.setFieldExpr("FULL_NAME", inParams.add(fullName));
        }
        if (!Util.isEmpty(definition)) {
            gen.setFieldExpr("DEFINITION", inParams.add(definition));
        }

        String wrkUser = req.getParameter("wrk_user");
        if (!Util.isEmpty(wrkUser)) {
            gen.setFieldExpr("WORKING_USER", inParams.add(wrkUser));
        } else {
            gen.setFieldExpr("WORKING_USER", "NULL");
        }

        StringBuffer buf = new StringBuffer(gen.updateStatement());
        buf.append(" where NAMESPACE_ID=");
        buf.append(inParams.add(nsID[0], Types.INTEGER));

        LOGGER.debug(buf.toString());

        PreparedStatement stmt = SQL.preparedStatement(buf.toString(), inParams, conn);
        stmt.executeUpdate();

        stmt.close();

        lastInsertID = nsID[0];
    }

    private void delete() throws Exception {

        // don't allow manual deletion of namespaces in the first approach
        return;
    }

    private void setLastInsertID() throws SQLException {

        String qry = "SELECT LAST_INSERT_ID()";

        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(qry);
        rs.clearWarnings();
        if (rs.next()) {
            lastInsertID = rs.getString(1);
        }

        stmt.close();
    }

    public String getLastInsertID() {
        return lastInsertID;
    }

    private boolean exists() throws SQLException {

        INParameters inParams = new INParameters();
        String qry = "";
        if (Util.isEmpty(dsID) && Util.isEmpty(tblID)) {

            qry = "select count(*) as COUNT from NAMESPACE where " +
            "DATASET_ID is null and TABLE_ID is null and SHORT_NAME=" +
            inParams.add(shortName);
        } else if (!Util.isEmpty(tblID)) {

            qry = "select count(*) as COUNT from NAMESPACE where " +
            "TABLE_ID=" + inParams.add(tblID, Types.INTEGER);
        } else {

            qry = "select count(*) as COUNT from NAMESPACE where " +
            "TABLE_ID is null and DATASET_ID=" + inParams.add(dsID, Types.INTEGER);
        }

        LOGGER.debug(qry);

        PreparedStatement stmt = SQL.preparedStatement(qry, inParams, conn);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            if (rs.getInt("COUNT")>0) {
                return true;
            }
        }

        stmt.close();

        return false;
    }
}
