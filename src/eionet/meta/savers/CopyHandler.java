package eionet.meta.savers;

import java.sql.*;
import java.util.*;

import eionet.meta.*;
import eionet.util.Log4jLoggerImpl;
import eionet.util.LogServiceIF;
import eionet.util.sql.SQL;

import javax.servlet.*;
import com.tee.util.*;
import com.tee.uit.security.*;

public class CopyHandler extends Object {

  /**
   * Constructor
   */
    private Connection conn = null;
    private DDSearchEngine searchEngine = null;
    private ServletContext ctx = null;
    
    private DDUser user = null;
    private static LogServiceIF logger = new Log4jLoggerImpl();

    /**
     * 
     * @param conn
     * @param ctx
     * @param searchEngine
     */
    public CopyHandler(Connection conn, ServletContext ctx, DDSearchEngine searchEngine) {
        this.conn = conn;
        this.ctx = ctx;
        if (searchEngine!=null)
        	this.searchEngine = searchEngine;
        else
        	this.searchEngine = new DDSearchEngine(conn);
    }
    
    /**
     * 
     * @param user
     */
    public void setUser(DDUser user){
        this.user = user;
    }

    /**
     * 
     * @param dstGen
     * @param srcConstraint
     * @return
     * @throws SQLException
     */
    public String copy(SQLGenerator dstGen, String srcConstraint)
        throws SQLException{

        return copy(dstGen, srcConstraint, true);
    }

    /**
     * Create a working copy of an object. Table name and preset values
     * of the working copy are given in <code>SQLGenerator</code>. The
     * <code>String</code> provides the constraint for selecting the
     * object to copy (e.g. "DATAELEM_ID=123"). If the <code>boolean</code>
     * is <code>false</code>, the fields in <code>SQLGenerator</code> will
     * not be selected from the original and they will be omitted from the
     * final insert query (i.e. they will be auto_generated by the DB).
     *
     * @return  id of the working copy.
     * @exception   SQLException
     */
    public String copy(SQLGenerator dstGen, String srcConstraint,
                        boolean includeDstGenFields) throws SQLException{

        if (dstGen==null) return null;
        srcConstraint = srcConstraint==null ? "" : " where " + srcConstraint;

        String tableName = dstGen.getTableName();
        Vector colNames = getTableColumnNames(tableName);
        if (colNames==null || colNames.size()==0)
        	throw new SQLException("Failed to retreive any column names of this table: " + tableName);
        
        String q = "select * from " + dstGen.getTableName() + srcConstraint;

        Statement stmt = null;
        Statement stmt1 = null;
        ResultSet rs = null;
        try{
        	stmt = conn.createStatement();
        	rs = stmt.executeQuery(q);
	        while (rs.next()){
	            SQLGenerator gen = (SQLGenerator)dstGen.clone();
	            for (int i=0; i<colNames.size(); i++){
	                String colName = (String)colNames.get(i);
	                String colValue = rs.getString(colName);
	                if ((dstGen.getFieldValue(colName))==null){
	                    if (colValue!=null)
	                        gen.setField(colName, colValue);
	                }
	                else if (!includeDstGenFields){
	                    if(dstGen.getFieldValue(colName).equals(""))
	                        gen.removeField(colName);
	                }
	            }
	            logger.debug(gen.insertStatement());
	            
	            if (stmt1==null)
	            	stmt1 = conn.createStatement();
	            stmt1.executeUpdate(gen.insertStatement());
	        }
        }
        finally{
        	try{
	        	if (rs!=null) rs.close();
	        	if (stmt!=null) stmt.close();
	        	if (stmt1!=null) stmt1.close();
        	}
        	catch (Exception e){}
        }

        if (includeDstGenFields)
            return null;
        else
            return searchEngine.getLastInsertID();
    }
    
    /**
     * 
     * @param tableName
     * @return
     * @throws SQLException 
     */
    private Vector getTableColumnNames(String tableName) throws SQLException{
    	
    	Vector result = new Vector();
    	if (tableName==null || tableName.length()==0)
    		return result;
    	
    	StringBuffer buf = new StringBuffer("select * from ");
    	buf.append(tableName);
    	buf.append(" limit 0,1");
    	
    	int colCount = 0;
    	Statement stmt = null;
    	ResultSet rs = null;
    	ResultSetMetaData rsmd = null;
    	try{
    		stmt = conn.createStatement();
    		rs = stmt.executeQuery(buf.toString());
    		rsmd = rs.getMetaData();
    		if (rsmd!=null){
    			colCount = rsmd.getColumnCount();
    			for (int i=1; colCount>0 && i<=colCount; i++){
	                String colName = rsmd.getColumnName(i);
	                if (colName!=null && colName.length()>0)
	                	result.add(colName);
    			}
    		}
    	}
    	finally{
    		try{
	        	if (rs!=null) rs.close();
	        	if (stmt!=null) stmt.close();
        	}
        	catch (Exception e){}
    	}
    	
    	if (result.size()<colCount)
    		throw new SQLException("Failed to retreive names of all columns of this table: " + tableName);
    	
    	return result;
    }
    
	/**
	 * 
	 * @param elmID
	 * @param isMakeWorkingCopy
	 * @param isCopyTbl2ElmRelations
	 * @return
	 * @throws Exception
	 */
    public String copyElm(String elmID,
    					   boolean isMakeWorkingCopy,
    					   boolean isCopyTbl2ElmRelations,
    					   boolean resetVersionAndStatus) throws Exception{

        if (elmID==null)
        	return null;
        
        // copy row in DATAELEM table
        SQLGenerator gen = new SQLGenerator();
        gen.setTable("DATAELEM");
        gen.setField("DATAELEM_ID", "");
        String newID = copy(gen, "DATAELEM_ID=" + elmID, false);
        
        if (newID==null)
            return null;

		// if requestd, make it a working copy
        gen.clear();
        gen.setTable("DATAELEM");
        if (isMakeWorkingCopy)
            gen.setField("WORKING_COPY", "Y");            
        // if requested, reset VERSION and REG_STATUS
        if (resetVersionAndStatus){
        	gen.setFieldExpr("VERSION", "1");
        	gen.setField("REG_STATUS", "Incomplete");
        }
        else
        	gen.setFieldExpr("VERSION", "VERSION+1");
        gen.setFieldExpr("DATE", String.valueOf(System.currentTimeMillis()));
        if (user!=null)
        	gen.setField("USER", user.getUserName());
        // update the new copy
        conn.createStatement().executeUpdate(gen.updateStatement() +
                " where DATAELEM_ID=" + newID);
        
		// if requested, copy TBL2ELEM relations
		if (isCopyTbl2ElmRelations){
			gen.clear();
			gen.setTable("TBL2ELEM");
			gen.setField("DATAELEM_ID", newID);
			copy(gen, "DATAELEM_ID=" + elmID);
		}
		
		// copy simple attributes
		gen.clear();
		gen.setTable("ATTRIBUTE");
		gen.setField("DATAELEM_ID", newID);
		copy(gen, "DATAELEM_ID=" + elmID + " and PARENT_TYPE='E'");
		
        // copy complex attributes
        copyComplexAttrs(newID, elmID, "E");
        
        // copy fixed values
		copyFxv(newID, elmID, "elem");
		
        // copy fk relations
		gen.clear();
		gen.setTable("FK_RELATION");
		gen.setField("REL_ID", "");
		gen.setField("A_ID", newID);
		copy(gen, "A_ID=" + elmID, false);
		gen.clear();
		gen.setTable("FK_RELATION");
		gen.setField("REL_ID", "");
		gen.setField("B_ID", newID);
		copy(gen, "B_ID=" + elmID);
		
        return newID;
    }
    
	/*
	 * 
	 */
	public String convertElm(String elmID) throws Exception{
		
		if (elmID==null) return null;
        
		SQLGenerator gen = new SQLGenerator();
		gen.setTable("DATAELEM");
		gen.setField("DATAELEM_ID", "");
		String newID = copy(gen, "DATAELEM_ID=" + elmID, false);
		if (newID==null) return null;

		// copy rows in ATTRIBUTE, with lastInsertID
		gen.clear();
		gen.setTable("ATTRIBUTE");
		gen.setField("DATAELEM_ID", newID);
		copy(gen, "DATAELEM_ID=" + elmID + " and PARENT_TYPE='E'");

		// copy fixed values
		copyFxv(newID, elmID, "elem");

		return newID;
	}
    
    /**
	 * @param newOwner
	 * @param oldOwner
	 * @param ownerType
	 */
	public void copyFxv(String newOwner, String oldOwner, String ownerType)
														throws SQLException{
		
		StringBuffer buf = new StringBuffer("select * from FXV where ").
		append("OWNER_ID=").append(oldOwner).append(" and OWNER_TYPE=").
		append(Util.strLiteral(ownerType));

		Vector v = new Vector();
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(buf.toString());
		while (rs!=null && rs.next()){
			SQLGenerator gen = new SQLGenerator();
			gen.setTable("FXV");
			gen.setFieldExpr("OWNER_ID", newOwner);
			gen.setField("OWNER_TYPE", ownerType);
			gen.setField("VALUE", rs.getString("VALUE"));
			gen.setField("IS_DEFAULT", rs.getString("IS_DEFAULT"));
			gen.setField("DEFINITION", rs.getString("DEFINITION"));
			gen.setField("SHORT_DESC", rs.getString("SHORT_DESC"));
			v.add(gen);
		}
		rs.close();
		for (int i=0; i<v.size(); i++){
			SQLGenerator gen = (SQLGenerator)v.get(i);
			stmt.executeUpdate(gen.insertStatement());
		}
		
		stmt.close();
	}
    
    /**
    *
    */
    public String copyTbl(String tblID) throws Exception{

        if (tblID==null)
        	return null;

        // copy row in DS_TABLE table
        SQLGenerator gen = new SQLGenerator();
        gen.setTable("DS_TABLE");
        gen.setField("TABLE_ID", "");
        String newID = copy(gen, "TABLE_ID=" + tblID, false);
        if (newID==null)
            return null;

        Statement stmt = null;
        ResultSet rs = null;
        try{
        	stmt = conn.createStatement();
        
	        // set the date
	        gen.clear();
	        gen.setTable("DS_TABLE");
	        gen.setFieldExpr("DATE", String.valueOf(System.currentTimeMillis()));
	        if (user!=null)
	        	gen.setField("USER", user.getUserName());
	        stmt.executeUpdate(gen.updateStatement() +
	                                            " where TABLE_ID=" + newID);
	        
	        // copy simple attributes
	        gen.clear();
	        gen.setTable("ATTRIBUTE");
	        gen.setField("DATAELEM_ID", newID);
	        copy(gen, "DATAELEM_ID=" + tblID + " and PARENT_TYPE='T'");
	        
	        // copy complex attributes
	        copyComplexAttrs(newID, tblID, "T");
	        
			// copy documents
			gen.clear();
			gen.setTable("DOC");
			gen.setField("OWNER_ID", newID);
			copy(gen, "OWNER_TYPE='tbl' and OWNER_ID=" + tblID);
	
	        // copy elements
			
	        Vector elms = new Vector();
	        Vector commonnessFlags = new Vector();
	        
	        StringBuffer buf = new StringBuffer();
	        buf.append("select TBL2ELEM.DATAELEM_ID, DATAELEM.PARENT_NS from TBL2ELEM ").
	        append("left outer join DATAELEM on TBL2ELEM.DATAELEM_ID=DATAELEM.DATAELEM_ID ").
	        append("where DATAELEM.DATAELEM_ID is not null and TABLE_ID=").
	        append(tblID).append(" order by POSITION asc");
	        
	        rs = stmt.executeQuery(buf.toString());
	        while (rs.next()){
	        	elms.add(rs.getString(1));
	        	commonnessFlags.add(new Boolean(rs.getString("DATAELEM.PARENT_NS")==null));
	        }
	        
	        for (int i=0; i<elms.size(); i++){
	        	
	        	String elmId = (String)elms.get(i);
	        	if (((Boolean)commonnessFlags.get(i)).booleanValue() == false) // non-common element, has to copied
	        		elmId = copyElm(elmId, false, false, false);
	        	
	        	gen.clear();
	            gen.setTable("TBL2ELEM");
	            gen.setFieldExpr("TABLE_ID", newID);
	            gen.setFieldExpr("DATAELEM_ID", elmId);
	            gen.setFieldExpr("POSITION", String.valueOf(i+1));
	            stmt.executeUpdate(gen.insertStatement());
	        }
        }
        finally{
        	SQL.close(rs);
        	SQL.close(stmt);
        }
        
        return newID;
    }

    /**
     * 
     * @param dstID
     * @param isMakeWorkingCopy
     * @param resetVersionAndStatus
     * @return
     * @throws Exception
     */
    public String copyDst(String dstID,
                          boolean isMakeWorkingCopy,
                          boolean resetVersionAndStatus)throws Exception{

        if (dstID==null)
        	return null;
        
        // copy row in DATASET table
        SQLGenerator gen = new SQLGenerator();        
        gen.setTable("DATASET");
        gen.setField("DATASET_ID", "");
        String newID = copy(gen, "DATASET_ID=" + dstID, false);
        
        if (newID==null) return null;

        // make it a working copy if needed,
        // also change the value of VERSION
        gen.clear();
        gen.setTable("DATASET");
        if (isMakeWorkingCopy)
            gen.setField("WORKING_COPY", "Y");            
        if (resetVersionAndStatus){
        	gen.setFieldExpr("VERSION", "1");
        	gen.setField("REG_STATUS", "Incomplete");
        }
        else
        	gen.setFieldExpr("VERSION", "VERSION+1");
        gen.setField("DATE", String.valueOf(System.currentTimeMillis()));
        if (user!=null)
        	gen.setField("USER", user.getUserName());
        conn.createStatement().executeUpdate(gen.updateStatement() +
                " where DATASET_ID=" + newID);
        
        // copy simple attributes
        gen.clear();
        gen.setTable("ATTRIBUTE");
        gen.setField("DATAELEM_ID", newID);
        copy(gen, "DATAELEM_ID=" + dstID + " and PARENT_TYPE='DS'");

        // copy complex attributes
        copyComplexAttrs(newID, dstID, "DS");
        
        // copy rod links
		gen.clear();
		gen.setTable("DST2ROD");
		gen.setField("DATASET_ID", newID);
		copy(gen, "DATASET_ID=" + dstID);
		
		// copy documents
		gen.clear();
		gen.setTable("DOC");
		gen.setField("OWNER_ID", newID);
		copy(gen, "OWNER_TYPE='dst' and OWNER_ID=" + dstID);

		// copy tables
		copyDstTables(dstID, newID);
        
        return newID;
    }
    
    /**
     * 
     * @param oldDstID
     * @param newDstID
     * @throws Exception
     */
    public void copyDstTables(String oldDstID, String newDstID) throws Exception{
        
    	// get id numbers of tables to copy 
    	Vector tbls = new Vector();
        StringBuffer buf = new StringBuffer();
        buf.append("select TABLE_ID from DST2TBL where DATASET_ID=").append(oldDstID);
        buf.append(" order by POSITION asc");
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(buf.toString());
        while (rs.next())
        	tbls.add(rs.getString(1));
        
        // copy the tables, get id numbers of new ones
        Vector newTbls = new Vector();
        for (int i=0; i<tbls.size(); i++)
        	newTbls.add(copyTbl((String)tbls.get(i)));
        
        // relate new tables to new dataset
        SQLGenerator gen = new SQLGenerator();
        for (int i=0; i<newTbls.size(); i++){
        	if (i>0)
        		gen.clear();
            gen.setTable("DST2TBL");
            gen.setFieldExpr("DATASET_ID", newDstID);
            gen.setFieldExpr("TABLE_ID", (String)newTbls.get(i));
            gen.setFieldExpr("POSITION", String.valueOf(i+1));
            stmt.executeUpdate(gen.insertStatement());
        }
    }
    
    /**
    *
    */
    public void copyComplexAttrs(String newID, String oldID, String type)
													throws SQLException{
	    	copyComplexAttrs(newID, oldID, type, null, null);
  	}
    public void copyComplexAttrs(String newID, String oldID, String type, String newType, String mAttrID)
                                                    throws SQLException {

        if (newID==null || oldID==null || type==null)
            return;

        // get the attributes of the parent to copy and loop over them
        Vector v = searchEngine.getComplexAttributes(oldID, type, mAttrID);
        for (int i=0; v!=null && i<v.size(); i++){

            DElemAttribute attr = (DElemAttribute)v.get(i);
            String attrID = attr.getID();

		    // get the attribute fields
            Vector fields = searchEngine.getAttrFields(attrID);
            if (fields==null || fields.size()==0)
                continue;

            Statement stmt = null;
            try{
            	stmt = conn.createStatement();
			    // get the attribute rows
			    Vector valueRows = attr.getRows();
	            for (int j=0; valueRows!=null && j<valueRows.size(); j++){
	
	                Hashtable rowHash = (Hashtable)valueRows.get(j);
	                String rowPos = (String)rowHash.get("position");
	                rowPos = rowPos==null ? "0" : rowPos;
	
	                // insert a new row
	                if (newType!=null)
	                    type=newType;
	                String rowID =
	                "md5('" + newID + type + attrID + rowPos + "')";
	
	                SQLGenerator gen = new SQLGenerator();
					gen.setTable("COMPLEX_ATTR_ROW");
					gen.setField("PARENT_ID", newID);
					gen.setField("PARENT_TYPE", type);
					gen.setField("M_COMPLEX_ATTR_ID", attrID);
	                gen.setFieldExpr("ROW_ID", rowID);
	                gen.setFieldExpr("POSITION", rowPos);
	                
					// JH131103 - here we need to know if the attribute is linked to
					// an harevsted one
					String harvAttrID = (String)rowHash.get("harv_attr_id");				
	                if (harvAttrID!=null)
						gen.setField("HARV_ATTR_ID", harvAttrID);
	                
					stmt.executeUpdate(gen.insertStatement());
					if (harvAttrID!=null)
						continue;
	
	                // get the value of each field in the given row
	                int insertedFields = 0;
	                for (int t=0; rowID!=null && t<fields.size(); t++){
	                    Hashtable fieldHash = (Hashtable)fields.get(t);
					    String fieldID    = (String)fieldHash.get("id");
					    String fieldValue = (String)rowHash.get(fieldID);
	
					    // insert the field
					    if (fieldID!=null && fieldValue!=null){
					        gen.clear();
					        gen.setTable("COMPLEX_ATTR_FIELD");
					        gen.setFieldExpr("ROW_ID", rowID);
					        gen.setField("M_COMPLEX_ATTR_FIELD_ID", fieldID);
					        gen.setField("VALUE", fieldValue);
					        stmt.executeUpdate(gen.insertStatement());
					        insertedFields++;
					    }
	                }
	                
	                // if no fields were actually inserted, delete the row
	                if (insertedFields==0)
	                    stmt.executeUpdate("delete from COMPLEX_ATTR_ROW " +
					                               "where ROW_ID=" + rowID);
	            }
            }
            catch (SQLException e){
            	e.printStackTrace(System.out);
            	throw e;
            }
            finally{
            	try{
            		if (stmt!=null) stmt.close();
            	}
            	catch (SQLException e){}
            }
        }
    }
    
    public void copyAttribute(String newID, String oldID, String newType, String oldType, String mAttrID)
                                                    throws SQLException {
        SQLGenerator gen = new SQLGenerator();
        gen.setTable("ATTRIBUTE");
        gen.setField("DATAELEM_ID", newID);
        gen.setField("PARENT_TYPE", newType);
        copy(gen, "M_ATTRIBUTE_ID=" + mAttrID + " and DATAELEM_ID=" + oldID + " and PARENT_TYPE='" + oldType + "'");
    }
    /**
    *
    */
    private String getLastInsertID() throws SQLException {
        
        String qry = "SELECT LAST_INSERT_ID()";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(qry);        
        rs.clearWarnings();
        
        String id = null;
        if (rs.next())
            id = rs.getString(1);
            
        stmt.close();
        return id;
    }
}

