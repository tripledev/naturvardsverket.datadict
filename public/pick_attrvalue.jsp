<%@page contentType="text/html;charset=UTF-8" import="java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,com.tee.xmlserver.*,eionet.util.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<%
	request.setCharacterEncoding("UTF-8");
	
	ServletContext ctx = getServletContext();			
	String appName = ctx.getInitParameter("application-name");

	
	String type = request.getParameter("type");
	String attr_id = request.getParameter("attr_id");
	if (type == null) type = "?";

	if (attr_id == null || attr_id.length()==0) { %>
		<b>Attribute id paramater is missing!</b>
		<%
		return;
		
	}

	Connection conn = null;
	XDBApplication xdbapp = XDBApplication.getInstance(getServletContext());
	DBPoolIF pool = xdbapp.getDBPool();

	try { // start the whole page try block
		
	conn = pool.getConnection();
	DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);
	
	Vector attrValues=null;

	Vector v = searchEngine.getDElemAttributes(attr_id,type);
	DElemAttribute attribute = (v==null || v.size()==0) ? null : (DElemAttribute)v.get(0);
	String attrName = attribute.getName();

	Vector attrFields = searchEngine.getAttrFields(attr_id);

	if (type.equals(DElemAttribute.TYPE_COMPLEX))
		 attrValues = searchEngine.getComplexAttributeValues(attr_id);	
	else
		 attrValues = searchEngine.getSimpleAttributeValues(attr_id);	

%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
	<head>
		<%@ include file="headerinfo.jsp" %>
		<title>Meta</title>
		<script type="text/javascript">
		// <![CDATA[

			function closeme(){
				window.close()
			}
			function selectComplex(idx){
				var field_ids = document.forms["form1"].elements["field_ids"];
				if (opener && !opener.closed) {
					if (field_ids.length>0){
						for (var i=0; i<field_ids.length;i++){
							
							field_id=document.forms["form1"].elements["field_ids"][i].value;
							if (opener.document.forms["form1"].elements["field_"+field_id])
								opener.document.forms["form1"].elements["field_"+field_id].value=document.forms["form1"].elements["field_"+field_id][idx+1].value;
						}
					}
				} else {
					alert("You have closed the main window.\n\nNo action will be taken on the choices in this dialog box.")
				}
				closeme()
			}
			function selectSimple(val){
			//EK this function will finished if somebody want to use it
				if (opener && !opener.closed) {
						alert(val);
				} else {
					alert("You have closed the main window.\n\nNo action will be taken on the choices in this dialog box.")
				}
				closeme()
			}
		// ]]>
		</script>
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
	<h5>Select (<%=Util.replaceTags(attrName)%>) value:</h5>
	<form id="form1" action="">
	<table class="datatable">
		<tr>
			<%
			if (type.equals(DElemAttribute.TYPE_COMPLEX)){
				for (int t=0; t<attrFields.size(); t++){
					Hashtable hash = (Hashtable)attrFields.get(t);
					String name = (String)hash.get("name");
					String f_id = (String)hash.get("id");
						%>
						<th align="left" style="padding-right:10">&nbsp;<%=Util.replaceTags(name)%>
							<input type="hidden" name="field_ids" value="<%=f_id%>"/>
							<input type="hidden" name="field_<%=f_id%>" value=" "/>
						</th>
						<%
				}
			}
			else{
				%>
				<th align="left" style="padding-right:10">&nbsp;Value</th>
				<%
			}
		%>
		</tr>
		<%
		if (attrValues!= null){
		if (attrValues.size()>0){
			for (int j=0; attrValues!=null && j<attrValues.size();j++){
				String trStyle = (j%2 != 0) ? "style=\"background-color:#D3D3D3\"" : "";
				if (type.equals(DElemAttribute.TYPE_COMPLEX)){
					Hashtable rowHash = (Hashtable)attrValues.get(j);
					%>
					<tr <%=trStyle%>>
					<%
			
					for (int t=0; t<attrFields.size(); t++){
						Hashtable hash = (Hashtable)attrFields.get(t);
						String fieldID = (String)hash.get("id");
						String fieldValue = fieldID==null ? null : (String)rowHash.get(fieldID);
						
						if (fieldValue == null) fieldValue = "";
							%>
							<td style="padding-right:10">&nbsp;
								<%
								if (t==0){
									%>
									<a href="javascript:selectComplex(<%=j%>)"><%=Util.replaceTags(fieldValue, true)%></a><%
								}
								else{ %>
									<%=Util.replaceTags(fieldValue)%><%
								}
								%>
								<input type="hidden" name="field_<%=fieldID%>" value="<%=Util.replaceTags(fieldValue, true)%>"/>
							</td>
							<%
					}
				
					%>
					</tr>				
					<%
				}
				else{
					String value = (String)attrValues.get(j);
					%>
					<tr><td <% if (j % 2 != 0) %> bgcolor="#D3D3D3" <%;%> align="left" style="padding-right:10">&nbsp;
						<a href="javascript:selectSimple('<%=value%>')"><%=Util.replaceTags(value, true)%></a>
						</td></tr>
					<%
				}
			}
		}}
		%>	
		<tr><td>&nbsp;</td></tr>

	</table>
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
