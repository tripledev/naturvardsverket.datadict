package eionet.meta.savers;

import java.util.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;
import eionet.meta.DElemAttribute;

import com.tee.util.*;
import com.tee.xmlserver.AppUserIF;
import com.tee.uit.security.*;

public class AttributeHandler extends BaseHandler {
    
    private String mode = null;
    private String type = null;
    private String attr_id = null;
    private String lastInsertID = null;
    
    private String ns_id = null;

    private String name = null;
    private String shortName = null;
    private String definition = null;
    private String obligation = null;
    
    private Hashtable typeWeights = new Hashtable();
    
    public AttributeHandler(Connection conn, HttpServletRequest req, ServletContext ctx){
        this(conn, new Parameters(req), ctx);
    }
    
    public AttributeHandler(Connection conn, Parameters req, ServletContext ctx){
        this.conn = conn;
        this.req = req;
        this.ctx = ctx;
        this.mode = req.getParameter("mode");
        this.type = req.getParameter("type");
        this.attr_id = req.getParameter("attr_id");
        this.name = req.getParameter("name");
        this.shortName = req.getParameter("short_name");
        this.definition = req.getParameter("definition");
        this.obligation = req.getParameter("obligation");
        this.ns_id = req.getParameter("ns");
        
        typeWeights.put("TBL", new Integer(64));
        typeWeights.put("FXV", new Integer(32));
        typeWeights.put("DCL", new Integer(16));
        typeWeights.put("DST", new Integer(8));
        typeWeights.put("AGG", new Integer(4));
        typeWeights.put("CH1", new Integer(2));
        typeWeights.put("CH2", new Integer(1));
    }

    public AttributeHandler(Connection conn, HttpServletRequest req, ServletContext ctx, String mode){
        this(conn, req, ctx);
        this.mode = mode;
    }

    public void execute() throws Exception {

        if (mode==null || (!mode.equalsIgnoreCase("add") &&
                          !mode.equalsIgnoreCase("edit") &&
                          !mode.equalsIgnoreCase("delete")))
            throw new Exception("AttributeHandler mode unspecified!");

        if (mode.equalsIgnoreCase("add")){
            if (type==null || (!type.equalsIgnoreCase(DElemAttribute.TYPE_SIMPLE) &&
                            !type.equalsIgnoreCase(DElemAttribute.TYPE_COMPLEX)))
                throw new Exception("AttributeHandler type unspecified!");
        }
        
        if (mode.equalsIgnoreCase("add"))
            insert();
        else if (mode.equalsIgnoreCase("edit"))
            update();
        else{
            delete();
            cleanVisuals();
        }
    }
    
    private void insert() throws Exception {
        
        SQLGenerator sqlGenerator = new SQLGenerator();
        if (type==null || type.equals(DElemAttribute.TYPE_SIMPLE))
            sqlGenerator.setTable("M_ATTRIBUTE");
        else
            sqlGenerator.setTable("M_COMPLEX_ATTR");
        
        sqlGenerator.setField("SHORT_NAME", shortName);
        sqlGenerator.setField("NAME", name);
        
        if (definition != null) sqlGenerator.setField("DEFINITION", definition);
        if (ns_id != null) sqlGenerator.setField("NAMESPACE_ID", ns_id);

        String dispOrder = req.getParameter("dispOrder");
        if (dispOrder != null && dispOrder.length() != 0)
            sqlGenerator.setField("DISP_ORDER", dispOrder);

        String inherit = req.getParameter("inheritable");
        if (inherit != null && inherit.length() != 0)
            sqlGenerator.setField("INHERIT", inherit);

        String dispType = req.getParameter("dispType");
        if (type==null || type.equals(DElemAttribute.TYPE_SIMPLE)){

            sqlGenerator.setField("DISP_WHEN", getDisplayWhen());
            sqlGenerator.setField("OBLIGATION", obligation);

            if (dispType == null || dispType.length() == 0) dispType = "NULL";
            sqlGenerator.setField("DISP_TYPE", dispType);

            String dispWidth = req.getParameter("dispWidth");
            if (dispWidth != null && dispWidth.length() != 0)
                sqlGenerator.setField("DISP_WIDTH", dispWidth);

            String dispHeight = req.getParameter("dispHeight");
            if (dispHeight != null && dispHeight.length() != 0)
                sqlGenerator.setField("DISP_HEIGHT", dispHeight);

            String dispMultiple = req.getParameter("dispMultiple");
            if (dispMultiple != null && dispMultiple.length() != 0)
                sqlGenerator.setField("DISP_MULTIPLE", dispMultiple);
        }
        
		if (type!=null && type.equals(DElemAttribute.TYPE_COMPLEX)){
			String harvesterID = req.getParameter("harv_id");
			if (harvesterID != null && !harvesterID.equals("null"))
				sqlGenerator.setField("HARVESTER_ID", harvesterID);
		}
        
        String sql = sqlGenerator.insertStatement();
        logger.debug(sql);
        
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(sql);
		setLastInsertID();
		
		String idPrefix = "";
		if (type!=null && type.equals(DElemAttribute.TYPE_COMPLEX))
			idPrefix = "c";
		else if (type!=null && type.equals(DElemAttribute.TYPE_SIMPLE))
			idPrefix = "s";
        
        // add acl
		if (user!=null){
			String aclPath = "/attributes/" + idPrefix + getLastInsertID();
			String aclDesc = "Short name: " + shortName;
			AccessController.addAcl(aclPath, user.getUserName(), aclDesc);
		}

        stmt.close();
    }
    
    private void update() throws SQLException {
        
        lastInsertID = attr_id;
        
        SQLGenerator sqlGenerator = new SQLGenerator();
        if (type==null || type.equals(DElemAttribute.TYPE_SIMPLE))
            sqlGenerator.setTable("M_ATTRIBUTE");
        else
            sqlGenerator.setTable("M_COMPLEX_ATTR");
        
        sqlGenerator.setField("SHORT_NAME", shortName);
        sqlGenerator.setField("NAME", name);
        
        if (definition != null) sqlGenerator.setField("DEFINITION", definition);
        if (ns_id != null) sqlGenerator.setField("NAMESPACE_ID", ns_id);
        
        String dispOrder = req.getParameter("dispOrder");
        if (dispOrder == null || dispOrder.length() == 0) dispOrder = "999";
        sqlGenerator.setField("DISP_ORDER", dispOrder);

        String inherit = req.getParameter("inheritable");
        if (inherit != null && inherit.length() != 0)
            sqlGenerator.setField("INHERIT", inherit);
        else
            sqlGenerator.setField("INHERIT", "0");

        if (type==null || type.equals(DElemAttribute.TYPE_SIMPLE)){

            sqlGenerator.setField("OBLIGATION", obligation);
            sqlGenerator.setField("DISP_WHEN", getDisplayWhen());
            
            String dispType = req.getParameter("dispType");
            if (dispType == null || dispType.length() == 0)
                sqlGenerator.setFieldExpr("DISP_TYPE", "NULL");
            else
                sqlGenerator.setField("DISP_TYPE", dispType);
            
            String dispWidth = req.getParameter("dispWidth");
            if (dispWidth == null || dispWidth.length() == 0) dispWidth = "20";
            sqlGenerator.setField("DISP_WIDTH", dispWidth);
            
            String dispHeight = req.getParameter("dispHeight");
            if (dispHeight == null || dispHeight.length() == 0) dispHeight = "20";
            sqlGenerator.setField("DISP_HEIGHT", dispHeight);

            String dispMultiple = req.getParameter("dispMultiple");
            if (dispMultiple != null && dispMultiple.length() != 0)
                sqlGenerator.setField("DISP_MULTIPLE", dispMultiple);
            else
                sqlGenerator.setField("DISP_MULTIPLE", "0");
        }
        
		String harvesterID = req.getParameter("harv_id");
		if (type!=null && type.equals(DElemAttribute.TYPE_COMPLEX)){			
			if (harvesterID != null && !harvesterID.equals("null"))
				sqlGenerator.setField("HARVESTER_ID", harvesterID);
			else
				sqlGenerator.setFieldExpr("HARVESTER_ID", "NULL");
		}

        StringBuffer buf = new StringBuffer(sqlGenerator.updateStatement());
        if (type==null || type.equals(DElemAttribute.TYPE_SIMPLE))
            buf.append(" where M_ATTRIBUTE_ID=");
        else
            buf.append(" where M_COMPLEX_ATTR_ID=");
        buf.append(attr_id);

        Statement stmt = conn.createStatement();
        stmt.executeUpdate(buf.toString());
        
        // if this is a compelx attribute and harvester link is being
        // removed, set all links to harvested rows to NULL as well
		if (type!=null && type.equals(DElemAttribute.TYPE_COMPLEX)){			
			if (harvesterID == null || harvesterID.equals("null")){
				sqlGenerator = new SQLGenerator();
				sqlGenerator.setTable("COMPLEX_ATTR_ROW");
				sqlGenerator.setFieldExpr("HARV_ATTR_ID", "NULL");
				buf = new StringBuffer(sqlGenerator.updateStatement());
				buf.append(" where M_COMPLEX_ATTR_ID=").append(attr_id);
				
				stmt.executeUpdate(buf.toString());
			}
		}
				
        stmt.close();
    }
    
    private void delete() throws Exception {
        
        String[] simpleAttrs = req.getParameterValues("simple_attr_id");
        String[] complexAttrs = req.getParameterValues("complex_attr_id");
        
        if (simpleAttrs != null && simpleAttrs.length != 0){
            StringBuffer buf = new StringBuffer("delete from M_ATTRIBUTE where ");
            for (int i=0; i<simpleAttrs.length; i++){
                if (i>0) buf.append(" or ");
                buf.append("M_ATTRIBUTE_ID=");
                buf.append(simpleAttrs[i]);
            }
            
            logger.debug(buf.toString());
        
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(buf.toString());
            stmt.close();
            
            deleteSimpleAttributeValues(simpleAttrs);
            deleteFixedValues(simpleAttrs);
        }
        
        if (complexAttrs != null && complexAttrs.length != 0){
            StringBuffer buf = new StringBuffer("delete from M_COMPLEX_ATTR where ");
            for (int i=0; i<complexAttrs.length; i++){
                if (i>0) buf.append(" or ");
                buf.append("M_COMPLEX_ATTR_ID=");
                buf.append(complexAttrs[i]);
            }
            
            logger.debug(buf.toString());
        
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(buf.toString());
            stmt.close();
            
            deleteComplexAttributeValues(complexAttrs);
        }
        
		// remove acls
		for (int i=0; simpleAttrs!=null && i<simpleAttrs.length; i++){
			try{
				AccessController.removeAcl("/attributes/s" + simpleAttrs[i]);
			}
			catch (Exception e){}
		}
		
		for (int i=0; complexAttrs!=null && i<complexAttrs.length; i++){
			try{
				AccessController.removeAcl("/attributes/c" + complexAttrs[i]);
			}
			catch (Exception e){}
		}

    }
    
    private void deleteSimpleAttributeValues(String[] attr_ids) throws SQLException {
        
        if (attr_ids==null || attr_ids.length==0)
            return;
        
        StringBuffer buf = new StringBuffer("delete from ATTRIBUTE where ");
        for (int i=0; i<attr_ids.length; i++){
            if (i>0)
                buf.append(" or ");
            buf.append("M_ATTRIBUTE_ID=");
            buf.append(attr_ids[i]);
        }
        
        logger.debug(buf.toString());
        
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(buf.toString());
        stmt.close();
    }
    
    private void deleteComplexAttributeValues(String[] attr_ids) throws SQLException {
        
        StringBuffer buf = new StringBuffer("select distinct ROW_ID from COMPLEX_ATTR_ROW where ");
        for (int i=0; i<attr_ids.length; i++){
            if (i>0) buf.append(" or ");
            buf.append("M_COMPLEX_ATTR_ID=");
            buf.append(attr_ids[i]);
        }
        
        logger.debug(buf.toString());
        
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(buf.toString());

        buf = null;        
        for (int i=0; rs.next(); i++){
            if (buf == null)
                buf = new StringBuffer("delete from COMPLEX_ATTR_FIELD where ");
                
            if (i>0) buf.append(" or ");
            buf.append("ROW_ID='");
            buf.append(rs.getString("ROW_ID"));
            buf.append("'");
        }
        
        if (buf != null){
        	logger.debug(buf.toString());
            
            stmt = conn.createStatement();
            stmt.executeUpdate(buf.toString());
        }
        
        buf = new StringBuffer("delete from COMPLEX_ATTR_ROW where ");
        for (int i=0; i<attr_ids.length; i++){
            if (i>0) buf.append(" or ");
            buf.append("M_COMPLEX_ATTR_ID=");
            buf.append(attr_ids[i]);
        }
        
        logger.debug(buf.toString());
        
        stmt = conn.createStatement();
        stmt.executeUpdate(buf.toString());
        stmt.close();
    }
    
    private void deleteFixedValues(String[] attr_ids) throws Exception {
        
        if (attr_ids==null || attr_ids.length==0)
            return;
        
        StringBuffer buf = new StringBuffer();
        buf.append("select distinct FXV_ID from FXV where OWNER_TYPE='attr' and (");
        for (int i=0; i<attr_ids.length; i++){
            if (i>0) buf.append(" or ");
            buf.append("OWNER_ID=");
            buf.append(attr_ids[i]);
        }
        buf.append(")");
        
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(buf.toString());
        Parameters pars = new Parameters();
        while (rs.next()) pars.addParameterValue("del_id", rs.getString("FXV_ID"));
        stmt.close();
        
        pars.addParameterValue("mode", "delete");
        FixedValuesHandler fvHandler = new FixedValuesHandler(conn, pars, ctx);
        fvHandler.execute();
    }
    
    private void setLastInsertID() throws SQLException {
        
        String qry = "SELECT LAST_INSERT_ID()";
        
        logger.debug(qry);
        
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(qry);        
        rs.clearWarnings();
        if (rs.next())
            lastInsertID = rs.getString(1);
        stmt.close();
    }
    
    public String getLastInsertID(){
        return lastInsertID;
    }
    
    private String getDisplayWhen(){
        
        // the default here is 2^typeWeights.size(), because if no
        // "dispWhen" has been set there is no point in storing an
        // attribute that wouldn't be displayed anyway, so we rather
        // display it for all then
        
        String[] dispWhen = req.getParameterValues("dispWhen");
        if (dispWhen == null || dispWhen.length == 0)
            return String.valueOf(Math.pow(2, typeWeights.size()) - 1);
            
        int k = 0;
        for (int i=0; i<dispWhen.length; i++){
            Integer weight = (Integer)typeWeights.get(dispWhen[i]);
            if (weight != null)
                k = k + weight.intValue();
        }
        
        if (k == 0) k = (int)Math.pow(2, typeWeights.size()) - 1;
        
        return String.valueOf(k);
    }
    
	public void setUser(AppUserIF user){
		this.user = user;
	}

}
