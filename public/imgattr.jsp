<%@page contentType="text/html;charset=UTF-8" import="eionet.meta.*,java.sql.*,java.util.*,com.tee.xmlserver.*,eionet.util.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%
	request.setCharacterEncoding("UTF-8");
	
	XDBApplication.getInstance(getServletContext());
	AppUserIF user = SecurityUtil.getUser(request);
	
	session.setAttribute("imgattr_qrystr", request.getQueryString());
%>

<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head>
	<%@ include file="headerinfo.txt" %>
	<title>Data Dictionary</title>
	<script language="javascript" src='script.js' type="text/javascript"></script>
	<script language="javascript" type="text/javascript">
	// <![CDATA[

	function submitForm(mode){
		
		if (mode == 'remove'){
			document.forms["Upload"].elements["mode"].value = "remove";
			document.forms["Upload"].encoding = "application/x-www-form-urlencoded";
			document.forms["Upload"].submit();
			return;
		}

		var radio
		var o;

		for (var i=0; i<document.forms["Upload"].elements.length; i++){
			o = document.forms["Upload"].elements[i];
			if (o.name == "fileORurl"){
				if (o.checked == true){
					radio = o.value;
					//break;
				}
			}
		}
		
		var url = document.forms["Upload"].elements["url_input"].value;
		var file = document.forms["Upload"].elements["file_input"].value;
		var ok = true;

		if (radio == "url"){
			if (url == ""){
				alert("URL is not specified, there is nothing to add!");
				ok = false;
			}
		}
		if (radio == "file"){
			if (file == ""){
				alert("File location is not specified, there is nothing to add!");
				ok = false;
			}
		}

		if (ok == true){
			var trailer = "?fileORurl=" + radio + "&amp;url_input=" + url + "&amp;file_input=" + file;
			trailer = trailer + "&amp;obj_id=" + document.forms["Upload"].elements["obj_id"].value;
			
			var oType = document.forms["Upload"].elements["obj_type"];
			if (oType != null)
				trailer = trailer + "&amp;obj_type=" + oType.value;
				
			var oAttrID = document.forms["Upload"].elements["attr_id"];
			if (oAttrID != null)
				trailer = trailer + "&amp;attr_id=" + oAttrID.value;
				
			document.forms["Upload"].action = document.forms["Upload"].action + trailer;
			document.forms["Upload"].submit();
		}
	}
	
	// ]]>
	</script>
</head>

<%

ServletContext ctx = getServletContext();			
String appName = ctx.getInitParameter("application-name");

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
}						

String objID = request.getParameter("obj_id");
if (objID == null || objID.length()==0){ %>
	<b>Object ID is missing!</b> <%
	return;
}

String objType = request.getParameter("obj_type");
if (objType==null || objType.length()==0){ %>
	<b>Object type is missing!</b> <%
	return;
}

String attrID = request.getParameter("attr_id");
if (attrID==null || attrID.length()==0){ %>
	<b>Attribute ID is missing!</b> <%
	return;
}

String objName = request.getParameter("obj_name");
if (objName==null || objName.length()==0)
	objName = " ? ";

String titleLink = "";
String titleType = "";
// set the title type and link
if (objType.equals("E")){
	titleType = " element";
	titleLink = "data_element.jsp?mode=view&amp;delem_id=" + objID;
}
else if (objType.equals("T")){
	titleType = " table";
	titleLink = "dstable.jsp?mode=view&amp;table_id=" + objID;
}
else if (objType.equals("DS")){%>
	<b>No images allowed for datasets! Instead use their data model feature.</b> <%
	return;
}

String attrName = request.getParameter("attr_name");
if (attrName==null || attrName.length()==0)
	attrName = " ? ";

Connection conn = null;
XDBApplication xdbapp = XDBApplication.getInstance(getServletContext());
DBPoolIF pool = xdbapp.getDBPool();

try { // start the whole page try block

conn = pool.getConnection();
DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);

Vector attrs = searchEngine.getSimpleAttributes(objID, objType);

String _type = null;
if (objType.equals("E"))
	_type="elm";
else if (objType.equals("DS"))
	_type="dst";
else if (objType.equals("T"))
	_type="tbl";

String disabled = "disabled";
if (user!=null && searchEngine.isWorkingCopy(objID, _type))
	disabled = "";
			
%>

<body class="popup">

<div class="popuphead">
	<h1>Data Dictionary</h1>
	<hr/>
	<div align="right">
		<form name="close" action="javascript:window.close()">
			<input type="submit" class="smallbutton" value="Close"/>
		</form>
	</div>
</div>

<form name="Upload" action="ImgUpload" method="post" enctype="multipart/form-data">

	<h1>
			<%=Util.replaceTags(attrName)%> of <a href="<%=Util.replaceTags(titleLink, true)%>"><font color="#006666"><%=Util.replaceTags(objName, true)%></font></a> <%=Util.replaceTags(titleType)%>
	</h1>
		
		
	<table width="auto" cellspacing="0">
		<tr>
			<td align="left" style="padding-right:5">
				<input type="radio" name="fileORurl" value="file" checked="checked"/>&#160;File:</td>
			<td align="left">
				<input type="file" class="smalltext" name="file_input" size="40"/>
			</td>
		</tr>
		<tr>
			<td align="left" style="padding-right:5">
				<input type="radio" class="smalltext" name="fileORurl" value="url"/>&#160;URL:
			</td>
			<td align="left">
				<input type="text" class="smalltext" name="url_input" size="52"/>
			</td>
		</tr>
		<tr>
			<td></td>
			<td align="left">
				<input name="SUBMIT" type="button" <%=disabled%> class="mediumbuttonb" value="Add" onclick="submitForm('upload')" onkeypress="submitForm('upload')"/>&#160;&#160;
				<input name="REMOVE" type="button" <%=disabled%> class="mediumbuttonb" value="Remove selected" onclick="submitForm('remove')" onkeypress="submitForm('remove')"/>
			</td>
		</tr>
		<tr><td colspan="2">&nbsp;</td></tr>
	</table>
	
	<table width="auto" cellspacing="0">
		
		<%							
		boolean found = false;
		int displayed = 0;
		for (int i=0; attrs!=null && i<attrs.size(); i++){	
			DElemAttribute attr = (DElemAttribute)attrs.get(i);
			if (attr.getID().equals(attrID)){									
				Vector values = attr.getValues();
				for (int j=0; values!=null && j<values.size(); j++){
					found = true;
					String value = (String)values.get(j);
					if (displayed==0){ %>
						<tr><td colspan="2">Please note that you can only add images of JPG, GIF and PNG!</td></tr> <%
					}
					%>
					<tr>
						<td valign="top">
							<input type="checkbox" name="file_name" value="<%=Util.replaceTags(value, true)%>"/>
						</td>
						<td align="left">
							<img src="visuals/<%=Util.replaceTags(value, true)%>"/>
						</td>
					</tr>
					<tr height="10"><td colspan="2">&#160;</td></tr> <%
					displayed++;
				}
			}
		}
		
		if (!found){%>
			<tr>
				<td align="left" colspan="2">
					<b>
						No images found! You can add by using the form above.<br/>
						Please note that you can only add images of JPG, GIF or PNG.<br/>
						If you're not authorized or the object is not a working copy,<br/>
						the form is disabled.
					</b>
				</td>
			</tr><%
		}
		
		%>
		
	</table>
	
	<input type="hidden" name="obj_id" value="<%=objID%>"/>
	<input type="hidden" name="obj_type" value="<%=objType%>"/>
	<input type="hidden" name="attr_id" value="<%=attrID%>"/>
	
	<input type="hidden" name="mode" value="add"/>
	
	<input type="hidden" name="redir_url" value="imgattr.jsp?<%=Util.replaceTags(request.getQueryString(), true)%>"/>
	
</form>
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
