<%@page contentType="text/html" import="java.util.*,com.tee.xmlserver.*"%>

<%@ include file="history.jsp" %>

<%

	XDBApplication.getInstance(getServletContext());
	
	String page_id = request.getParameter("page");

	if (page_id==null || page_id.length()==0)
		page_id = "0";

	String page_name=null;

	if (page_id.equals("1")){
		page_name = "Functions";
	}
	else if (page_id.equals("2")){
		page_name = "Concepts";
	}
	else if (page_id.equals("3")){
		page_name = "Login mode";
	}
            		
%>

<html>
<head>
    <title>Data Dictionary</title>
    <META CONTENT="text/html; CHARSET=ISO-8859-1" HTTP-EQUIV="Content-Type">
    <link type="text/css" rel="stylesheet" href="eionet.css">
    <script language="JavaScript" src='script.js'></script>
</head>
<body>
    <%@ include file="header.htm" %>
    <table border="0" cellspacing="0" cellpadding="0">
        <tr valign="top">
            <td nowrap="true" width="130">
                <p><center>
                    <%@ include file="menu.jsp" %>
                </center></P>
            </TD>
            <TD>
               	<% if (page_name == null){%>
	                <jsp:include page="location.jsp" flush='true'/>
           		<%} else{ %>
	                <jsp:include page="location.jsp" flush='true'>
            			<jsp:param name="name" value="<%=page_name%>"/>
            			<jsp:param name="back" value="true"/>
		            </jsp:include>
	            <% } %>

				<div style="margin-left:30">
				<%
				if (page_id.equals("0")){
				%>
							<p>
								<font class="head00">Content of the Data Dictionary </font><br></br>
								The Data Dictionary is a central service for storing technical specifications of the data requested in reporting obligations. The Data Dictionary provides descriptions of data file structures, such as definition of datasets, tables, data elements, nomenclature, allowable values and other technical requirements. <br>
							</p>
							<p>
								You can view and get information about existing dataset definitions, tables within datasets and their data elements (attributes/fields). <br>
								<ul>
									<li><a href="datasets.jsp?SearchType=SEARCH">Datasets</a></li>
									<li><a href="search_results_tbl.jsp?SearchType=SEARCH">Tables within datasets</a></li>
									<li><a href="search.jsp">Data elements</a></li>
								</ul>
							</p>
							<p>
								For each of these categories different services are or will be offered.
								<ul>
									<li>View on web. Provides you with overview lists or details of specific dataset or elements.</li>
									<li>Search and listing. Sorting and search is possible, either using name, keywords or other identification or content.</li>
									<li>Download specification documents and fact sheets. (to come)</li>
									<li>Download parameter files. (to come)</li>
									<li>Editing (only for selected personnel). Additional functions will be added after login.</li>
								</ul>
							</p>
							<p>
								Links to further information about
								<ul>
									<li><a href="index.jsp?page=1">Data Dictionary - functions, services and users</a></li>
									<li><a href="index.jsp?page=2">Concepts &amp; terms - datasets, tables, data elements, code lists</a></li>
									<li><a href="index.jsp?page=3">Data Dictionary - administrative tools - login mode</a>
								</ul>
							</p>
				<%
				}
				else if (page_id.equals("1")){
				%>
							<p>
								<br/>
								<font class="head00">Data Dictionary - functions, services and users</font><br></br>
								The Data Dictionary serves some main functions<br>
							</p>
							<p>
								<ul>
									<li>provide countries with <b>detailed</b> specifications of what to produce and report, to be looked up at web services of by provision of fact sheet documents for download.</li>
									<li>provide <b>parameters</b> necessary as input in <b>technical quality control</b> and validation of the reported data.</li>
									<li>provides a reference for <b>users</b> of the EEA and others using data following the specifications.</li>
									<li>provide a reference for <b>harmonisation processes</b> at the European level, the existing specifications to be reused when reporting obligations get revised or new ones are to be defined.</li>
									<li>Data Dictionary does not only contain spec ifications of data to be reported from the countries, but also other data needed flowing from other sources to be used in <b>indicator</b> development.</li>
									<li>If agreed with the EEA, countries can also store own definitions about national or internal reporting obligations.</li>
								</ul>
								<br>
								Reporting obligations are usually long-lasting. The definitions found in the Data Dictionary, therefore, are relatively stable. Revision and definitions of new dataset is handled by persons who have obtained administrating rights and have logged in by pressing the <a href="javascript:login()">Login</a> button. 
							</p>
				<%
				}
				else if (page_id.equals("2")){
				%>
							<p>
								<font class="head00">Concepts &amp; terms - datasets, tables, data elements </font><br></br>
								<b>&nbsp;&nbsp;Datasets</b><br>
							</p>
							<p>
								A collection of data exchanged between applications or humans. In Reportnet and Data Dictionary's context a dataset is a collection of tables containing the reported data. Often the "tables" will actually recede to a single table only. Usually datasets come as MSAccess databases or MSExcel files. They are subject to certain data flows and obliged to be reported by Reportnet players according to legislation.
							</p>
						<p><b>&nbsp;&nbsp;Tables</b></p>
							<p>
								A table in Data Dictionary's context is a table in dataset. It can be either a data table or a lookup table for how to interpret the data. A lookup table can be for example made for holding country codes or whichever other code lists.
								Columns in a table stand for data elements, rows for their values.
							</p>
						<p><b>&nbsp;&nbsp;Data elements</b></p>
							<p>
								Data elements are the different attributes or kinds of information linked up to entity. In a tabular structure the data elements are
							</p>
						<p style="margin-left:30"><img src="images/delem_description.gif"></p>
							<p>
								There can be different kinds of data elements
								<ul>
									<li><b>Data element with quantitative values</b>: The most commonly used kind, where any answer can be loaded if within the value domain. Examples: data elements "Longitude" or pH,  which allows any measured number to be used, or data element "Sitename" allowing any text describing a site.</li>
									<li><b>Data element with fixed values</b>: A data element where a predefined code list or other fixed values are the only accepted values. Examples: Station size with fixed values like Small, Large, etc.)</li>
								</ul>
							</p>
							<p>
								Important items when defining a data element is
								<ul>
									<li>Data type: if the content of the data element should be text, integer, Boolean or other types. Also specifies if decimals are to be used, and how many decimals are accepted.</li>
									<li>Sizemax: How large the field could be as a maximum.</li>
									<li>Value domain. In many cases you can define the value domain, e.g. only values from 0-100 is accepted for percentage values.</li>
									<li>Allowable values: If a pre-coded list is to be used, the allowable values will correspond to this list.</li>
									<li>Multiplicity: How many answers you allow for each case/object.</li>
								</ul>
							</p>
							<p>
								There is a long series of other attributes also being used to define the data element, among others the keywords being used to describe it, institution responsible for the data definition etc. For a full list of the attributes used, see <a href="attributes.jsp">attributes list</a>.
							</p>
				<%
				}
				else if (page_id.equals("3")){
				%>
							<p>
								<br/>
								<font class="head00">Data Dictionary - administrative tools - login mode</font><br></br>
								<b>Adding or revising the content</b><br>
							</p>
							<p>
								Revision and definitions of new datasets is handled by persons who have obtained
								administration rights and have logged in by pressing the <a href="javascript:login()">Login</a> button.
								<ul>
									<li>
										<b>Revision</b> of data definitions: Once you log in, you will be provided
										with additional functions on the left-hand pane, enabling you to add new data
										definitions or revise content. This you can do manually, by writing into
										the forms on the web pages.
									</li>
									<li>
										You can <b>import data</b> definitions directly in XML format. EEA has
										developed an MSAccess database for preparation of the data definitions,
										which you can then generate into XML format which in turn can then be
										imported into Data Dictionary database.
									</li>
									<li>
										Each data definition can be <b>represented in XML Schema</b> format
										and you can see (and save) them when pressing the relevant link at the bottom
										of definition views.
									</li>
								</ul>
							</p>
							<p>
								<b>Adding or revising the definition structure </b>
							</p>
							<p>
								Each dataset, table and data element is defined by a set of attributes.
								A lot of them correspond to <a target="_blank"
								href="http://www.diffuse.org/meta.html#ISO11179">
								ISO 11179 standard</a> for describing data elements. 
								Data Dictionary attribute set will be relatively stable. 
								However, the system is flexible and an administrator can dynamically
								<b>add/remove attributes</b> from/to the system. 
								To search for attributes, administrator can use the <b>Attributes</b>
								button on the left-hand pane.
							</p>
				<%
				}
				%>
				
				</div>
            </TD>
        </TR>
    </table>
</body>
</html>
