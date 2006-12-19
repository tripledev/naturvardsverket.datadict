<%@page contentType="text/html;charset=UTF-8" import="java.util.*,java.sql.*,eionet.meta.*,eionet.meta.savers.*,eionet.util.Util,com.tee.xmlserver.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%!static int iPageLen=0;%>

<%@ include file="history.jsp" %>

<%
	System.out.println("----");
	
	request.setCharacterEncoding("UTF-8");
	ServletContext ctx = getServletContext();
	String appName = ctx.getInitParameter("application-name");

	Connection conn = null;
	XDBApplication xdbapp = XDBApplication.getInstance(getServletContext());
	DBPoolIF pool = xdbapp.getDBPool();

	try { // start the whole page try block

	conn = pool.getConnection();

	AppUserIF user = SecurityUtil.getUser(request);
	if (user==null || !user.isAuthentic()){ %>
		<b>Not allowed!</b><%
		return;
	}

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
			AttributeHandler handler = new AttributeHandler(userConn, request, ctx, "delete");
			handler.setUser(user);

			handler.execute();

			String redirUrl = request.getParameter("searchUrl");
			if (redirUrl != null && redirUrl.length()!=0){
				ctx.log("redir= " + redirUrl);
				response.sendRedirect(redirUrl);
			}
		}
		finally{
			try { if (userConn!=null) userConn.close();
			} catch (SQLException e) {}
		}
	}

	DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);

	Vector attributes = searchEngine.getDElemAttributes(null, DElemAttribute.TYPE_SIMPLE, DDSearchEngine.ORDER_BY_M_ATTR_DISP_ORDER);
	Vector complexAttributes = searchEngine.getDElemAttributes(null, DElemAttribute.TYPE_COMPLEX, DDSearchEngine.ORDER_BY_M_ATTR_DISP_ORDER);
	for (int i=0; complexAttributes!=null && i<complexAttributes.size(); i++)
		attributes.add(complexAttributes.get(i));


	int iCurrPage=0;
    try {
	    iCurrPage=Integer.parseInt(request.getParameter("page_number"));
    }
    catch(Exception e){
        iCurrPage=0;
    }
    if (iCurrPage<0)
        iCurrPage=0;

    String mode = request.getParameter("mode");
%>

<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head>
	<%@ include file="headerinfo.jsp" %>
  <title>Data Dictionary - Attributes</title>
  <script type="text/javascript">
  // <![CDATA[
		function setLocation(){
			var o = document.forms["form1"].searchUrl;
			if (o!=null)
				o.value=document.location.href;
		}

		function goTo(mode){
			if (mode == "add"){
				document.location.assign('delem_attribute.jsp?mode=add');
			}
		}
		// ]]>
    </script>
</head>
<body>
	<jsp:include page="nlocation.jsp" flush='true'>
		<jsp:param name="name" value="Attributes"/>
		<jsp:param name="back" value="true"/>
	</jsp:include>
<%@ include file="nmenu.jsp" %>
<div id="workarea">
			<%

            if (attributes == null || attributes.size()==0){
	            %>
	            <b>No attributes were found!</b></div></td></tr></table></body></html>
	            <%
	            return;
            }
            %>

			<form id="form1" method="post" action="attributes.jsp">

				<div id="operations">
					<ul>
						<li class="help"><a target="_blank" href="help.jsp?screen=attributes&amp;area=pagehelp" onclick="pop(this.href);return false;">Page help</a></li>
				<%
				if (user != null && mode==null){
					boolean addPrm = SecurityUtil.hasPerm(user.getUserName(), "/attributes", "i");
					if (addPrm){
						%>
						<li><a href="javascript:goTo('add')">Add attribute</a></li>
						<%
					}
				}

				%>

					</ul>
				</div>

		<h1>Attributes</h1>
		<p>
			This is a list of all definition attributes used in Data Dictionary.
			Every attribute is uniquely identifed by its short name. Click page help
			and question marks in column headers to to find out more.
			To view <% if (user != null && mode==null){ %> or modify <%}%> an attribute's
			definition, click its short name.
			<% if (false && user != null && mode==null){ %>
				To add a new attribute, click the 'Add' button on top of the list.
				The left-most column enables you to delete selected attributes.
			<%}%>
		</p>

		<table class="datatable">
			<col style="width:30%"/>
			<col style="width:14%"/>
			<col style="width:14%"/>
			<col style="width:14%"/>
			<col style="width:14%"/>
			<col style="width:14%"/>
			<tr>
				<th scope="col" class="scope-col">
								Short name
				</th>
				<th scope="col" class="scope-col">
								Type
				</th>
				<th scope="col" class="scope-col">
								Datasets
				</th>
				<th scope="col" class="scope-col">
								Tables
				</th>
				<th scope="col" class="scope-col">
								Data elements with fixed values
				</th>
				<th scope="col" class="scope-col">
								Data elements with quantitative values
				</th>
			</tr>

			<%
			// show all
			if (iPageLen==0)
				iPageLen = attributes.size();

	        int iBeginNode=iCurrPage*iPageLen;
		    int iEndNode=(iCurrPage+1)*iPageLen;
			if (iEndNode>=attributes.size())
				iEndNode=attributes.size();
	        //for (int i=iBeginNode;i<iEndNode;i++) {
			for (int i=0; i<attributes.size(); i++){

				DElemAttribute attribute = (DElemAttribute)attributes.get(i);

				String attr_id = attribute.getID();
				String attr_name = attribute.getShortName();
				if (attr_name == null) attr_name = "unknown";
				if (attr_name.length() == 0) attr_name = "empty";
				String attr_oblig = attribute.getObligation();

				String attrType = attribute.getType();

				String displayOblig = "Mandatory";
				if (attr_oblig.equals("M")){
					displayOblig = "Mandatory";
				}
				else if (attr_oblig.equals("O")){
					displayOblig = "Optional";
				}
				else if (attr_oblig.equals("C")){
					displayOblig = "Conditional";
				}

				String attrTypeDisp = "Simple";
				%>

				<tr <% if (i % 2 != 0) %> class="zebradark" <%;%>>
					<%
					if (attrType.equals(DElemAttribute.TYPE_COMPLEX))
						attrTypeDisp = "Complex";
					%>
					<td>
						<a href="delem_attribute.jsp?attr_id=<%=attr_id%>&amp;type=<%=attrType%>&amp;mode=view">
						<%=Util.replaceTags(attr_name)%></a>
					</td>
					<td><%=Util.replaceTags(attrTypeDisp)%></td>
					<td class="center">
						<% if (attribute.displayFor("DST")){ %><img src="images/ok.gif" alt="Yes"/><%}%>
					</td>
					<td class="center">
						<% if (attribute.displayFor("TBL")){ %><img src="images/ok.gif" alt="Yes"/><%}%>
					</td>
					<td class="center">
						<% if (attribute.displayFor("CH1")){ %><img src="images/ok.gif" alt="Yes"/><%}%>
					</td>
					<td class="center">
						<% if (attribute.displayFor("CH2")){ %><img src="images/ok.gif" alt="Yes"/><%}%>
					</td>
				</tr>

				<%
			}
			%>

		</table>

		<input type="hidden" name="searchUrl" value=""/>
		</form>
			</div> <!-- workarea -->
      <jsp:include page="footer.jsp" flush="true"/>
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
