<%@page contentType="text/html" import="java.io.*,java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.*,com.tee.xmlserver.*"%>

<%!private Vector elems=null;%>
<%!private ServletContext ctx=null;%>

<%@ include file="history.jsp" %>

<%

response.setHeader("Pragma", "no-cache");
response.setHeader("Cache-Control", "no-cache");
response.setDateHeader("Expires", 0);

XDBApplication.getInstance(getServletContext());
AppUserIF user = SecurityUtil.getUser(request);
if (request.getMethod().equals("POST")){
	if (user == null){
		%>
			<html>
			<body>
				<h1>Error</h1><b>Not authorized to post any data!</b>
			</body>
			</html>
		<%
		return;
	}
}

//check if element id is specified
String delemID = request.getParameter("delem_id");
if (delemID == null || delemID.length()==0){ %>
	<b>Data element ID is missing!</b> <%
	return;
}

String delemName = request.getParameter("delem_name");
if (delemName == null || delemName.length()==0) delemName = "unknown";

String disabled = user == null ? "disabled" : "";

ctx = getServletContext();

//handle the POST

if (request.getMethod().equals("POST")){
	
	Connection userConn = null;
	
	try{
		userConn = user.getConnection();
		FKHandler handler = new FKHandler(userConn, request, ctx);
		
		try{
			handler.execute();
		}
		catch (Exception e){
			e.printStackTrace(new PrintStream(response.getOutputStream()));
			return;
		}
	}
	finally{
		try { if (userConn!=null) userConn.close();
		} catch (SQLException e) {}
	}
	
	// build reload URL

	response.sendRedirect(currentUrl);
	return;

}

//handle the GET

String appName = ctx.getInitParameter("application-name");

Connection conn = null;
XDBApplication xdbapp = XDBApplication.getInstance(getServletContext());
DBPoolIF pool = xdbapp.getDBPool();

try { // start the whole page try block

conn = pool.getConnection();
DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);

elems = searchEngine.getFKRelationsElm(delemID);

StringBuffer collect_elems=new StringBuffer();

if (disabled.equals("")){
	boolean isWorkingCopy = searchEngine.isWorkingCopy(delemID, "elm");
	if (!isWorkingCopy) disabled = "disabled";
}

disabled="";
	
%>

<html>
<head>
	<title>Meta</title>
	<META HTTP-EQUIV="Content-Type" CONTENT="text/html"/>
	<link href="eionet.css" rel="stylesheet" type="text/css"/>
</head>

<script language="JavaScript" src='script.js'></script>

<script language="JavaScript">
		function submitForm(mode){
			
			if (mode=="delete"){
				var b = confirm("This will delete all the foreign key relations you have selected. Click OK, if you want to continue. Otherwise click Cancel.");
				if (b==false) return;
			}
			
			document.forms["form1"].elements["mode"].value = mode;
			document.forms["form1"].submit();
		}
		
		function openAdd(url){
			
			<%
			String selDS = request.getParameter("ds_id");
			if (selDS!=null){%>
				if (url != null) url = url + "&dataset=" + <%=selDS%>;<%
			}
			%>
			
			var selected = document.forms["form1"].collect_elems.value;
			if (url != null) url = url + "&selected=" + selected;
			wAdd = window.open(url,"Search","height=500,width=700,status=yes,toolbar=no,scrollbars=yes,resizable=yes,menubar=no,location=yes");
			if (window.focus) {wAdd.focus()}
		}
		
		function pickElem(id, name){
			document.forms["form1"].b_id.value=id;
			document.forms["form1"].mode.value="add";
			submitForm('add');
			
			return false;
		}
		
</script>
	
<body marginheight ="0" marginwidth="0" leftmargin="0" topmargin="0">
<%@ include file="header.htm" %>
<table border="0">
    <tr valign="top">
        <td nowrap="true" width="125">
            <p><center>
                <%@ include file="menu.jsp" %>
            </center></P>
        </TD>
        <TD>
            <jsp:include page="location.jsp" flush='true'>
                <jsp:param name="name" value="Foreign keys"/>
                <jsp:param name="back" value="true"/>
            </jsp:include>
            
<div style="margin-left:30">
			
<form name="form1" method="POST" action="foreign_keys.jsp">

	<table width="500" cellspacing="0" cellpadding="0">

		
		<tr valign="bottom">
			<td>
				<font class="head00">Foreign keys associated with
				<span class="title2"><%=Util.replaceTags(delemName)%></span>.
			</td>
		</tr>
		
		<tr height="5"><td></td></tr>
		
		<tr><td style="border-top-color:#008B8B;border-top-style:solid;border-top-width:1pt;">&#160;</td></tr>
		
		<tr>
			<td>
				This is a list of elements associated as foreign keys with
				<font color="#006666"><%=Util.replaceTags(delemName)%></font>.
				Click on element name to view its definition, click on Cardinality
				to view or edit the fk relation.
			</td>
		</tr>
		
		<tr height="5"><td>&#160;</td></tr>
		
	</table>
	
	<table width="auto" cellspacing="0" cellpadding="0">
	
		<tr style="padding-bottom:2" >
			<td></td>
			<td colspan="3">
				<input type="button" <%=disabled%>
					class="smallbutton"
					value="Add"
					onclick="openAdd('search.jsp?fk=true&ctx=popup')"/>
			</td>
		</tr>

		<tr>
			<td align="right" style="padding-right:10">
				<input type="button" <%=disabled%> value="Remove" class="smallbutton" onclick="submitForm('delete')"/>
			</td>				
			<th align="left" style="padding-left:5;padding-right:10">Element</th>
			<th align="left" style="padding-right:10">Table</th>
			<th align="left" style="padding-right:10">Cardinality</th>
		</tr>
			
		<%
		
		collect_elems.append(delemID + "|");
		for (int i=0; elems!=null && i<elems.size(); i++){
			
			Hashtable fkRel  = (Hashtable)elems.get(i);
			String fkElmID   = (String)fkRel.get("elm_id");
			String fkElmName = (String)fkRel.get("elm_name");
			String fkTblName = (String)fkRel.get("tbl_name");
			String fkRelID   = (String)fkRel.get("rel_id");
			
			String aCardin   = (String)fkRel.get("a_cardin");
			String bCardin   = (String)fkRel.get("b_cardin");
			String relDefin   = (String)fkRel.get("definition");
			String cardinality = aCardin + " to " + bCardin;
			
			if (fkElmID==null || fkElmID.length()==0)
				continue;
			
			collect_elems.append(fkElmID + "|");
			
			%>
			<tr>
				<td align="right" style="padding-right:10">
					<input type="checkbox" name="rel_id" value="<%=fkRelID%>"/>
				</td>
				<td align="left" style="padding-left:5;padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
					<a href="data_element.jsp?delem_id=<%=fkElmID%>&mode=view"><%=fkElmName%></a>
				</td>
				<td align="left" style="padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
					<%=fkTblName%>
				</td>
				<td align="left" style="padding-right:10" <% if (i % 2 != 0) %> bgcolor="#D3D3D3" <%;%>>
					<a  title="<%=relDefin%>"
						href="fk_relation.jsp?rel_id=<%=fkRelID%>&mode=view"><%=cardinality%></a>
				</td>
			</tr>
			<%
		}
		
		%>

	</table>
	
	<input type="hidden" name="mode" value="delete"/>
	<input type="hidden" name="delem_id" value="<%=delemID%>"/>
	<input type="hidden" name="delem_name" value="<%=delemName%>"/>
	
	<input type="hidden" name="a_id" value="<%=delemID%>"/>
	<input type="hidden" name="b_id" value=""/>
	
	<input type="hidden" name="collect_elems" value="<%=collect_elems.toString()%>"></input>
		
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