package eionet.meta.exports.xls;

import javax.servlet.http.*;
import javax.servlet.*;

import java.io.*;
import java.sql.*;
import java.util.HashSet;

import eionet.meta.exports.*;
import eionet.meta.exports.schema.*;
import eionet.util.*;
import eionet.util.sql.ConnectionUtil;
import eionet.meta.DDSearchEngine;
import com.tee.uit.security.*;

public class XlsServlet extends HttpServlet {
	
	private HashSet validObjTypes = null;
	
	public void init() throws ServletException{
		validObjTypes = new HashSet();
		validObjTypes.add("dst");
		validObjTypes.add("tbl");
	}
    
    protected void service(HttpServletRequest req, HttpServletResponse res)
                                throws ServletException, IOException {

		res.setContentType("application/vnd.ms-excel");
		
		OutputStream os = null;
        Connection conn = null;
        
        try{
        	
            String id = req.getParameter("obj_id");
	        if (Util.voidStr(id))
	            throw new Exception("Missing object id!");
	        
			String type = req.getParameter("obj_type");
			if (Util.voidStr(type) || !validObjTypes.contains(type))
				throw new Exception("Missing object type or object type invalid!");
	        
	        ServletContext ctx = getServletContext();
			String cachePath = Props.getProperty(PropsIF.DOC_PATH);

            // get the DB connection
			conn = ConnectionUtil.getConnection();
                
	        DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);
            os = res.getOutputStream();
            
            XlsIF xls = null;
            if (type.equals("dst")){
            	xls = new DstXls(searchEngine, os);
				((CachableIF)xls).setCachePath(cachePath);
            }
            else{
            	xls = new TblXls(searchEngine, os);
				((CachableIF)xls).setCachePath(cachePath);
            }
            
			xls.create(id);
			StringBuffer buf = new StringBuffer("attachment; filename=\"").
			append(xls.getName()).append("\"");
			res.setHeader("Content-Disposition", buf.toString());
			
			xls.write();
	        os.flush();
	        os.close();
	    }
	    catch (Exception e){
	        e.printStackTrace(System.out);
			throw new ServletException(e.toString());
	    }
		finally{
			try{
				if (os != null) os.close();
				if (conn != null) conn.close();
			}
			catch(Exception ee){}
		}
    }
}
