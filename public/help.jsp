<%@page contentType="text/html;charset=UTF-8" import="com.tee.uit.help.Helps, java.sql.*, eionet.meta.*, com.tee.xmlserver.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<%

request.setCharacterEncoding("UTF-8");

String helpText = "";

String screen = request.getParameter("screen");
String area   = request.getParameter("area");
String attrid = request.getParameter("attrid");
String attrshn = request.getParameter("attrshn");

if (attrid==null && attrshn==null){
	if (screen==null || area==null){ %>
		<b>Missing screen or area!</b> <%
		return;
	}
	
	String _helpText = Helps.get(screen, area);
	if (_helpText!=null)
		helpText = _helpText;		
}
else{
	
	String attrtype = request.getParameter("attrtype");
	if (attrtype==null){ %>
		<b>Missing attribute type!</b><%
		return;
	}
	
	Connection conn = null;
	XDBApplication xdbapp = XDBApplication.getInstance(getServletContext());
	DBPoolIF pool = xdbapp.getDBPool();
	try {
		conn = pool.getConnection();
		DDSearchEngine searchEngine = new DDSearchEngine(conn, "", getServletContext());
		if (attrid==null || attrid.length()==0)
			helpText = searchEngine.getAttrHelpByShortName(attrshn, attrtype);
		else
			helpText = searchEngine.getAttrHelp(attrid, attrtype);
		
		helpText = helpText==null ? "" : helpText;
	}
	catch (Exception e){ %>
		<b><%=e.toString()%></b><%
		return;
	}
	finally{
		try { if (conn!=null) conn.close(); } catch (SQLException e) {}
	}
}

%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
		<%@ include file="headerinfo.jsp" %>
    <title>Data Dictionary</title>
</head>
<body class="popup">
	<div id="pagehead">
	    <a href="/"><img src="images/eealogo.gif" alt="Logo" id="logo" /></a>
	    <div id="networktitle">Eionet</div>
	    <div id="sitetitle">Data Dictionary (DD)</div>
	    <div id="sitetagline">This service is part of Reportnet</div>    
	</div> <!-- pagehead -->
	<div id="operations" style="margin-top:10px">
		<ul>
			<li><a href="javascript:window.close();">Close</a></li>
		</ul>
	</div>
	<div id="workarea" style="clear:right">
		<%=helpText%>
	</div>
</body>
</html>
