<%@page import="com.tee.uit.security.AccessController, com.tee.uit.security.AccessControlListIF, eionet.util.SecurityUtil,com.tee.xmlserver.AppUserIF"%>

<div id="globalnav">
	<h2>Contents</h2>
		<ul>
			<li><a href="datasets.jsp?SearchType=SEARCH">Datasets</a></li>
			<li><a href="search_results_tbl.jsp?SearchType=SEARCH">Tables</a></li>
			<li><a href="search.jsp">Data elements</a></li>
		</ul>

	<%
		AppUserIF _user = SecurityUtil.getUser(request);
		if (_user!=null && _user.isAuthentic()) {
			%>
	<h2>Logged in as<br/><%=_user.getUserName()%></h2>
    <ul>
			<li><a href="logout.jsp">Logout</a></li>
			<%
		}
		else{
			%>
	<h2>Not logged in</h2>
    <ul>
			<li><a href="javascript:login()">Login</a></li>
			<%
		}
		if (_user!=null && _user.isAuthentic()) { %>
  	
		<li><a href="attributes.jsp">Attributes</a></li>
		
		<%
			if (SecurityUtil.hasPerm(_user.getUserName(), "/import", "x")){ %>
					<a href="import.jsp">Import datasets</a> <%
			}

			if (SecurityUtil.hasPerm(_user.getUserName(), "/cleanup", "x")){ %>
				<a href="clean.jsp">Cleanup</a> <%
			}
		}
		%>
	</ul>

	<h2>Reportnet services</h2>
	<ul>
        <li><a href="http://cr.eionet.eu.int">Content Registry</a></li>
		<li><a href="http://rod.eionet.eu.int/" title="Reporting Obligations">ROD Obligations</a></li>
		<li><a href="http://cdr.eionet.eu.int/" title="Central Data Repository">CDR Repository</a></li>
		<li><a href="http://dd.eionet.eu.int/">Data Dictionary</a></li>
	</ul>
</div>
