<%@page contentType="text/html;charset=UTF-8" import="java.util.*,java.sql.*,java.io.*,eionet.meta.*,com.tee.xmlserver.*,com.tee.uit.help.Helps,eionet.util.Util"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@ include file="history.jsp" %>

<%
request.setCharacterEncoding("UTF-8");

Connection conn = null;
DBPoolIF pool = null;
ServletContext ctx = getServletContext();
XDBApplication xdbapp = XDBApplication.getInstance(getServletContext());

String page_id = request.getParameter("page");
if (page_id==null || page_id.length()==0)
	page_id = "0";

String page_name="";	
if (page_id.equals("1"))
	page_name = "Functions";
else if (page_id.equals("2"))
	page_name = "Concepts";
else if (page_id.equals("3"))
	page_name = "Login mode";

try{
	pool = xdbapp.getDBPool();	
	conn = pool.getConnection();
	
	AppUserIF user = SecurityUtil.getUser(request);
	DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);	
	searchEngine.setUser(user);
	
	HashSet filterStatuses = new HashSet();
	filterStatuses.add("Released");
	Vector releasedDatasets = searchEngine.getDatasets(null, null, null, null, null, false, filterStatuses);
	request.setAttribute("rlsd_datasets", releasedDatasets);
}
catch (Exception e){
	
	request.setAttribute("DD_ERR_MSG", e.toString());
	
	ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();							
	e.printStackTrace(new PrintStream(bytesOut));
	String trace = bytesOut.toString(response.getCharacterEncoding());
	if (trace!=null)
		request.setAttribute("DD_ERR_TRC", trace);
}
finally{
	try{
		if (conn!=null) conn.close();
	}
	catch (SQLException e){}
}

	
%>
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head>
	<%@ include file="headerinfo.jsp" %>
	<title>Data Dictionary</title>
</head>
<body>
<div id="container">
    <jsp:include page="nlocation.jsp" flush="false">
		<jsp:param name="name" value="<%=page_name%>"/>
    </jsp:include>
    <%@ include file="nmenu.jsp" %>
<div id="workarea">

				
					<%
					
					// exceptionous part
					String errMsg = (String)request.getAttribute("DD_ERR_MSG");
					if (errMsg!=null){
						String errTrc = (String)request.getAttribute("DD_ERR_TRC");
						%>
						<b>DD encountered the following error:</b><br/>
						<%=errMsg%>
						<%
						if (errTrc!=null){
							%>
							<form name="errtrc" action="http://">
								<input type="hidden" name="errtrc" value="<%=errTrc%>"/>
							</form>
							<%
						}
					}
					// no exceptions
					else{
						%>
					
								<div id="outerframe">												
									<div style="margin-bottom:10px">
					                    <jsp:include page="released_datasets.jsp" flush="true" />
									</div>					                	
									<table>
										<tr>
											<td style="vertical-align:top"><%=Helps.get("front_page", "documentation")%></td>
					                  		<td><%=Helps.get("front_page", "support")%></td>
					                	</tr>
					                	<tr>
					                  		<td><%=Helps.get("front_page", "news")%></td>
		                  					<td>&nbsp;</td>
										</tr>
									</table>
						</div>						
						<%
					} // end of excpetions if/else
					%>
								
</div> <!-- workarea -->
</div> <!-- container -->
<jsp:include page="footer.jsp" flush="true" />
</body>
</html>
