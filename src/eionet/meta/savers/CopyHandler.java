package eionet.meta.savers;

import java.sql.*;
import java.util.*;

import eionet.meta.*;
import javax.servlet.*;
import com.tee.util.*;
import com.tee.xmlserver.AppUserIF;
import com.tee.uit.security.*;

public class CopyHandler extends Object {

  /**
   * Constructor
   */
    private Connection conn = null;
    private DDSearchEngine searchEngine = null;
    private ServletContext ctx = null;
    
    private AppUserIF user = null;

    public CopyHandler(Connection conn, ServletContext ctx, DDSearchEngine searchEngine) {
        this.conn = conn;
        this.ctx = ctx;
        this.searchEngine = searchEngine;
    }
    public CopyHandler(Connection conn, ServletContext ctx) {
        this.conn = conn;
        this.ctx = ctx;
        searchEngine = new DDSearchEngine(conn);
    }
    public CopyHandler(Connection conn) {
        this.conn = conn;
        searchEngine = new DDSearchEngine(conn);
    }
    
    public void setUser(AppUserIF user){
        this.user = user;
    }

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
     * not be selected from the original and they will be mitted from the
     * final insert query (i.e. they will be auto_generated by the DB).
     *
     * @return  id of the working copy.
     * @exception   SQLException
     */
    public String copy(SQLGenerator dstGen, String srcConstraint,
                        boolean includeDstGenFields) throws SQLException{

        if (dstGen==null) return null;
        srcConstraint = srcConstraint==null ? "" : " where " + srcConstraint;

        String q = "select * from " + dstGen.getTableName() + srcConstraint;

        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(q);
        ResultSetMetaData rsmd = rs.getMetaData();
        int colCount = rsmd.getColumnCount();

        while (rs.next()){
            SQLGenerator gen = (SQLGenerator)dstGen.clone();
            for (int i=1; i<=colCount; i++){
                String colName = rsmd.getColumnName(i);
                String colValue = rs.getString(i);
                if ((dstGen.getFieldValue(colName))==null){
                    if (colValue!=null)
                        gen.setField(colName, colValue);
                }
                else if (!includeDstGenFields){
                    if(dstGen.getFieldValue(colName).equals(""))
                        gen.removeField(colName);
                }
            }

            log(gen.insertStatement());
            stmt.executeUpdate(gen.insertStatement());
        }

        if (includeDstGenFields)
            return null;
        else
            return searchEngine.getLastInsertID();
    }

    public String copy(String srcElemID, boolean tbl2elem)
        throws Exception{

        return copyElem(srcElemID, true);
    }
    
	public String copyElem(String srcElemID, boolean tbl2elem)
													throws Exception{
		return copyElem(srcElemID, false, tbl2elem); 
	}
    
    public String copyElem(String srcElemID,
    					   boolean workingCopy,
    					   boolean tbl2elem) throws Exception{

        if (srcElemID==null) return null;
        
        SQLGenerator gen = new SQLGenerator();
        gen.setTable("DATAELEM");
        gen.setField("DATAELEM_ID", "");
        String newID = copy(gen, "DATAELEM_ID=" + srcElemID, false);
        
        if (newID==null)
            return null;

		// make it a working copy if needed
		if (workingCopy){
			gen.clear();
			gen.setTable("DATAELEM");
			gen.setField("WORKING_COPY", "Y");
			gen.setField("DATE", String.valueOf(System.currentTimeMillis()));
			conn.createStatement().executeUpdate(gen.updateStatement() +
											 " where DATAELEM_ID=" + newID);
		}
        
		// copy rows in TBL2ELEM, with lastInsertID, if needed
		if (tbl2elem){
			gen.clear();
			gen.setTable("TBL2ELEM");
			gen.setField("DATAELEM_ID", newID);
			copy(gen, "DATAELEM_ID=" + srcElemID);
		}
		
		// copy rows in ATTRIBUTE, with lastInsertID
		gen.clear();
		gen.setTable("ATTRIBUTE");
		gen.setField("DATAELEM_ID", newID);
		copy(gen, "DATAELEM_ID=" + srcElemID + " and PARENT_TYPE='E'");
		
        // copy rows in COMPLEX_ATTR_ROW, with lastInsertID
        copyComplexAttrs(newID, srcElemID, "E");
        
        // copy fixed values
		copyFxv(newID, srcElemID, "elem");
		
        // copy fk relations
		gen.clear();
		gen.setTable("FK_RELATION");
		gen.setField("REL_ID", "");
		gen.setField("A_ID", newID);
		copy(gen, "A_ID=" + srcElemID, false);
		gen.clear();
		gen.setTable("FK_RELATION");
		gen.setField("REL_ID", "");
		gen.setField("B_ID", newID);
		copy(gen, "B_ID=" + srcElemID);
		
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
	private void copyFxv(String newOwner, String oldOwner, String ownerType)
														throws SQLException{
		
		StringBuffer buf = new StringBuffer("select * from FXV where ").
		append("OWNER_ID=").append(oldOwner).append(" and OWNER_TYPE=").
		append(Util.strLiteral(ownerType));

		SQLGenerator gen = new SQLGenerator();
		gen.setTable("FXV");
		gen.setFieldExpr("OWNER_ID", newOwner);
		gen.setField("OWNER_TYPE", ownerType);
		
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(buf.toString());
		while (rs.next()){
			gen.setField("VALUE", rs.getString("VALUE"));
			gen.setField("IS_DEFAULT", rs.getString("IS_DEFAULT"));
			gen.setField("DEFINITION", rs.getString("DEFINITION"));
			gen.setField("SHORT_DESC", rs.getString("SHORT_DESC"));
			stmt.executeUpdate(gen.insertStatement());
		}
		
		stmt.close();
	}
    
    /**
    *
    */
    public String copyTbl(String tblID,
                          boolean workingCopy,
                          boolean elmRelations)throws Exception{

        if (tblID==null) return null;

        // get the table to copy
        DsTable dsTable = searchEngine.getDatasetTable(tblID);
        if (dsTable==null)
            throw new Exception("Could not find the table to copy!");

        // copy row in DS_TABLE table
        SQLGenerator gen = new SQLGenerator();
        gen.setTable("DS_TABLE");
        gen.setField("TABLE_ID", "");
        String newID = copy(gen, "TABLE_ID=" + tblID, false);
        if (newID==null)
            return null;

        Statement stmt = conn.createStatement();
        
        // make it a working copy if needed
        if (workingCopy){
            gen.clear();
            gen.setTable("DS_TABLE");
            gen.setField("WORKING_COPY", "Y");
            gen.setField("DATE", String.valueOf(System.currentTimeMillis()));
            stmt.executeUpdate(gen.updateStatement() +
                                                " where TABLE_ID=" + newID);
        }
        
        // enter a row into DST2TBL
        String dstID = dsTable.getDatasetID();
        if (dstID!=null){
            gen.clear();
            gen.setTable("DST2TBL");
            gen.setField("TABLE_ID", newID);
            gen.setField("DATASET_ID", dstID);
            stmt.executeUpdate(gen.insertStatement());
        }
        
        stmt.close();
        
        // copy simple attributes
        gen.clear();
        gen.setTable("ATTRIBUTE");
        gen.setField("DATAELEM_ID", newID);
        copy(gen, "DATAELEM_ID=" + tblID + " and PARENT_TYPE='T'");

        // copy complex attributes
        copyComplexAttrs(newID, tblID, "T");
        
        // copy rows in TBL2ELEM, with lastInsertID
        if (elmRelations){
            gen.clear();
            gen.setTable("TBL2ELEM");
            gen.setField("TABLE_ID", newID);
            copy(gen, "TABLE_ID=" + tblID);
        }
        
        return newID;
    }
    
    /**
    *
    */
    public String copyDst(String dstID,
                          boolean workingCopy,
                          boolean tableRelations,
                          boolean tables)throws Exception{

        if (dstID==null) return null;
        
        // copy row in DATASET table
        SQLGenerator gen = new SQLGenerator();        
        gen.setTable("DATASET");
        gen.setField("DATASET_ID", "");
        String newID = copy(gen, "DATASET_ID=" + dstID, false);
        
        if (newID==null) return null;

        // make it a working copy if needed
        if (workingCopy){
            gen.clear();
            gen.setTable("DATASET");
            gen.setField("WORKING_COPY", "Y");
            gen.setField("DATE", String.valueOf(System.currentTimeMillis()));
            conn.createStatement().executeUpdate(gen.updateStatement() +
                                                " where DATASET_ID=" + newID);
        }
        
        // copy simple attributes
        gen.clear();
        gen.setTable("ATTRIBUTE");
        gen.setField("DATAELEM_ID", newID);
        copy(gen, "DATAELEM_ID=" + dstID + " and PARENT_TYPE='DS'");

        // copy complex attributes
        copyComplexAttrs(newID, dstID, "DS");
        
        // copy rows in DST2TBL, with lastInsertID
        if (tableRelations){
            gen.clear();
            gen.setTable("DST2TBL");
            gen.setField("DATASET_ID", newID);
            copy(gen, "DATASET_ID=" + dstID);
        }
        
        // copy rod links
		gen.clear();
		gen.setTable("DST2ROD");
		gen.setField("DATASET_ID", newID);
		copy(gen, "DATASET_ID=" + dstID);
        
        // copy the tables as well, eventually with elements as well.
        // this is needed if copying an existing dataset for the creation
        // of a new one
        if (tables)
            copyDstTables(dstID);
        
        return newID;
    }
    
    /**
    *
    */
    public void copyDstTables(String dstID) throws Exception{
        // JH190803 - do me
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
                
                Statement stmt = conn.createStatement();
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
    
    private void log(String msg){
        if (ctx != null)
            ctx.log(msg);
    }
    
    public static void main(String[] args){
        
        try{
            Class.forName("org.gjt.mm.mysql.Driver");
            Connection conn = DriverManager.getConnection(
				"jdbc:mysql://195.250.186.16:3306/DataDict", "dduser", "xxx");
            
            CopyHandler copyHandler = new CopyHandler(conn);
            copyHandler.copyFxv("9999999", "9922", "elem");
       }
        catch (Exception e){
            System.out.println(e.toString());
        }
    }
}

