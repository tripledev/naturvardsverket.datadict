package eionet.meta.exports.xmlinst;

import java.io.*;
import java.util.*;
import java.sql.*;

import eionet.meta.*;
import eionet.util.Util;


public class TblXmlInst extends XmlInst {
	
	public TblXmlInst(DDSearchEngine searchEngine, PrintWriter writer){
		super(searchEngine, writer);
	}
	
	public void write(String tblID) throws Exception{

		if (Util.voidStr(tblID))
			throw new Exception("Table ID not specified!");
        
		// Get the table object.
		DsTable tbl = searchEngine.getDatasetTable(tblID);
		if (tbl == null) throw new Exception("Table not found!");
        
		// get data elements (this will set all the simple attributes of elements)
		tbl.setElements(searchEngine.getDataElements(null, null, null, null, tblID));
        
		write(tbl);
	}

	/**
	* Write a schema for a given object.
	*/
	private void write(DsTable tbl) throws Exception{

		// set the dataset namespace
		String nsID = tbl.getParentNs();
		if (!Util.voidStr(nsID)){
			Namespace ns = searchEngine.getNamespace(nsID);
			if (ns != null){
				addNamespace(ns);
				dstNsPrefix = getNamespacePrefix(ns);
				setSchemaLocation(getSchemaLocation(nsID, tbl.getID()));
			}
		}
		
		// set the table namespace
		nsID = tbl.getNamespace();
		if (!Util.voidStr(nsID)){
			Namespace ns = searchEngine.getNamespace(nsID);
			if (ns != null){
				addNamespace(ns);
				tblNsPrefix = getNamespacePrefix(ns);
			}
		}
		
		setDocElement(dstNsPrefix + ":" + tbl.getIdentifier());
		
		writeRows(tbl.getElements());
	}
	
	protected String getSchemaLocation(String nsID, String id){
		
		StringBuffer buf = new StringBuffer().
		append(appContext).append("namespace.jsp?ns_id=").append(nsID).append(" ").
		append(appContext).append("GetSchema?id=TBL").append(id);
		
		return buf.toString();
	}

	protected void setLeads(){
		leads = new Hashtable();
		leads.put("row", "\t");
		leads.put("elm", "\t\t");
	}
}
