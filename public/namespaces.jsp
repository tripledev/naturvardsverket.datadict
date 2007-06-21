<%@page contentType="text/html;charset=UTF-8" import="java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.*,com.tee.xmlserver.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@ include file="history.jsp" %>

<%
	request.setCharacterEncoding("UTF-8");
	
	ServletContext ctx = getServletContext();
	String appName = ctx.getInitParameter("application-name");
	
	String urlPath = ctx.getInitParameter("basens-path");
	if (urlPath == null) urlPath = "";
	
	Connection conn = null;
	XDBApplication xdbapp = XDBApplication.getInstance(getServletContext());
	DBPoolIF pool = xdbapp.getDBPool();
	
	try { // start the whole page try block
							
	conn = pool.getConnection();
	
	AppUserIF user = SecurityUtil.getUser(request);
	
	if (request.getMethod().equals("POST")){
		
		if (user == null){
	      			%>
	      				<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
	      				<body>
	      					<h1>Error</h1><b>Not authorized to post any data!</b>
	      				</body>
	      				</html>
	      			<%
	      			return;
      			}
		
      	Connection userConn = null;
		try{
			userConn = user.getConnection();
			NamespaceHandler handler =
						new NamespaceHandler(userConn, request, ctx, "delete");
					
			handler.execute();
		}
		finally{
			try { if (userConn!=null) userConn.close();
			} catch (SQLException e) {}
		}
		
		String redirUrl = request.getParameter("searchUrl");
		if (redirUrl != null && redirUrl.length()!=0){
			ctx.log("redir= " + redirUrl);
			response.sendRedirect(redirUrl);
		}
	}	
	
	DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);
	
	Vector namespaces = searchEngine.getNamespaces();	
%>

<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head>
	<%@ include file="headerinfo.jsp" %>
    <title>Data Dictionary</title>
    <script language="javascript" src='script.js' type="text/javascript"></script>
    <script language="javascript" type="text/javascript">
		// <![CDATA[
		function submitForm(){
			
			var b = confirm("This will delete all the namespaces you have selected. Click OK, if you want to continue. Otherwise click Cancel.");
			if (b==false) return;
					
			var o = document.forms["form1"].searchUrl;
			if (o!=null)
				o.value=document.location.href;
			
			document.forms["form1"].submit();
		}
		
		function goTo(mode){
			if (mode == "add"){
				document.location.assign('namespace.jsp?mode=add');
			}
		}
		// ]]>
    </script>
</head>
<body>
<jsp:include page="nlocation.jsp" flush="true">
	<jsp:param name="name" value="Namespaces"/>
	
</jsp:include>
<%@ include file="nmenu.jsp" %>
<div id="workarea">
	<h1>Namespaces</h1>
			<%
            
            if (namespaces == null || namespaces.size()==0){
	            %>
	            <b>No namespaces were found!</b></div></body></html>
	            <%
	            return;
            }
            %>
            
			<form id="form1" method="post" action="namespaces.jsp">
			
				<p>
					To view or modify a namespace, click its Full name. To add a new namespace, click 'Add'.
				</p>
		
		<table width="auto" cellspacing="0">
		
			<tr>
				<!--td></td-->
				<td align="left" colspan="2" style="padding-bottom:5">
					<% if (user != null){
						%>
						<input type="button" class="smallbutton" value="Add" onclick="goTo('add')"/>
						<%
					}
					else{
						%>
						<input type="button" class="smallbutton" value="Add" disabled/>
						<%
					}
					%>
				</td>
			</tr>
		
			<tr>
				<!--td align="right" style="padding-right:10">
					<% if (user != null){
						%>
						<input type="button" value="Delete" class="smallbutton" onclick="submitForm()"/>
						<%
					}
					else{
						%>
						<input type="button" value="Delete" class="smallbutton" disabled/>
						<%
					}
					%>
				</td-->
				<th align="left" style="padding-left:5;padding-right:10">Full name</th>
				<th align="left" style="padding-right:10">Description</th>
			</tr>
			
			<%
			
			for (int i=0; i<namespaces.size(); i++){
				
				Namespace namespace = (Namespace)namespaces.get(i);
				
				String ns_id = namespace.getID();
				String ns_name = namespace.getFullName();
				//String ns_short_name = namespace.getShortName();
				if (ns_name == null) ns_name = "unknown";
				if (ns_name.length() == 0) ns_name = "empty";
				
				String descr = namespace.getDescription();
				if (descr == null) descr = "";
				
				/*String ns_url = namespace.getUrl();
				if (ns_url.startsWith("/")) ns_url = urlPath + ns_url;
				
				String displayUrl = ns_url;
				if (ns_url.length()>100){
					displayUrl = ns_url.substring(0,100) + " ...";
				}*/
				
				String displayDescr = descr;
				if (descr.length()>100){
					displayDescr = descr.substring(0,100) + " ...";
				}
				
				String displayName = ns_name;
				if (ns_name.length()>20){
					displayName = ns_name.substring(0,20) + " ...";
				}
				
				%>
				
				<tr>
					<!--td align="right" style="padding-right:10">
						<%
						String tableID = namespace.getTable();
						String dsID = namespace.getDataset();
						if (ns_id.equals("1") || tableID != null || dsID!=null){
							%>
							<input type="checkbox" style="height:13;width:13" disabled/>
							<%
						}
						else{
							%>
							<input type="checkbox" style="height:13;width:13" name="ns_id" value="<%=ns_id%>"/>
							<%
						}
						%>
					</td-->
					<td align="left" style="padding-left:5;padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
						<a href="namespace.jsp?ns_id=<%=ns_id%>&amp;mode=view">
						<%=Util.replaceTags(displayName)%></a>
					</td>
					<td align="left" style="padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
						<%=Util.replaceTags(displayDescr)%>
					</td>
				</tr>
				
				<%
			}
			%>
			
		</table>
		
		<input type="hidden" name="searchUrl" value=""/>
		
		</form>
</div>
</body>
</html>

<%
// end the whole page try block
}
finally {
	try { if (conn!=null) conn.close();
	} catch (SQLException e) {}
}
%>
