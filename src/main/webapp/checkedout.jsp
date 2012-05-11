<%@page contentType="text/html;charset=UTF-8" import="java.util.*,java.sql.*,eionet.meta.*,eionet.util.*,eionet.util.sql.ConnectionUtil,eionet.meta.dao.domain.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<%!
ServletContext ctx = null;
boolean userHasWorkingCopies = false;
Vector datasets=null;
Vector commonElements=null;
List<SchemaSet> schemaSets=null;
List<Schema> schemas=null;
%>

<%@ include file="/pages/common/taglibs.jsp"%>
<%@ include file="history.jsp" %>

<%
    response.setHeader("Pragma", "No-cache");
    response.setHeader("Cache-Control", "no-cache,no-store,max-age=0");
    response.setHeader("Expires", Util.getExpiresDateString());

    request.setCharacterEncoding("UTF-8");

    DDUser user = SecurityUtil.getUser(request);

    if (user==null){
        response.sendRedirect("index.jsp");
        return;
    }

    ctx = getServletContext();

    Connection conn = null;

    // try-catch block of the whole page
    try {

        conn = ConnectionUtil.getConnection();

        DDSearchEngine searchEngine = new DDSearchEngine(conn, "", ctx);
        searchEngine.setUser(user);
        userHasWorkingCopies = searchEngine.hasUserWorkingCopies();

        if (userHasWorkingCopies){
            datasets = searchEngine.getDatasets(null, null, null, null, true);
            commonElements = searchEngine.getCommonElements(null, null, null, null, true, "=");
            schemaSets = searchEngine.getSchemaSetWorkingCopies();
            schemas = searchEngine.getSchemaWorkingCopies();
        }

%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
    <%@ include file="headerinfo.jsp" %>
    <title>Data Dictionary - Datasets checked out</title>
</head>
<body>
<div id="container">
<jsp:include page="nlocation.jsp" flush="true">
    <jsp:param name="name" value="Checked out"/>
</jsp:include>
<%@ include file="nmenu.jsp" %>

<div id="workarea">
    <h1>Your checkouts</h1>

<%
        if (userHasWorkingCopies){
%>
    <p class="advise-msg">You have checked out the following objects:</p>
    <table class="datatable">
        <tbody>
            <%
            int d=0;
            // DATASETS
            if (datasets!=null && datasets.size()>0){
                for (int i=0; i<datasets.size(); i++){

                    Dataset dataset = (Dataset)datasets.get(i);

                    String ds_id = dataset.getID();
                    String dsVersion = dataset.getVersion()==null ? "" : dataset.getVersion();
                    String ds_name = Util.processForDisplay(dataset.getShortName());
                    if (ds_name == null) ds_name = "unknown";
                    if (ds_name.length() == 0) ds_name = "empty";
                    String dsFullName=dataset.getName();
                    if (dsFullName == null) dsFullName = ds_name;
                    if (dsFullName.length() == 0) dsFullName = ds_name;
                    if (dsFullName.length()>60)
                        dsFullName = dsFullName.substring(0,60) + " ...";
                        d++;
                    %>

                    <tr>
                        <td>
                            Dataset:
                            <a href="<%=request.getContextPath()%>/datasets/<%=ds_id%>">
                                <%=Util.processForDisplay(dsFullName)%>
                            </a>
                        </td>
                    </tr>
                <%
                }
            } else {
                %>
                    <tr><td>You have no datasets checked out</td></tr>
                <%
            }
            // COMMON ELEMENTS
            if (commonElements!=null && commonElements.size()>0){
                for (int i=0; i<commonElements.size(); i++){
                    DataElement dataElement = (DataElement)commonElements.get(i);
                    String delem_id = dataElement.getID();
                    String delem_name = dataElement.getShortName();
                    if (delem_name == null) delem_name = "unknown";
                    if (delem_name.length() == 0) delem_name = "empty";
                    String delem_type = dataElement.getType();
                    if (delem_type == null) delem_type = "unknown";

                    String displayType = "unknown";
                    if (delem_type.equals("CH1")){
                        displayType = "Fixed values";
                    }
                    else if (delem_type.equals("CH2")){
                        displayType = "Quantitative";
                    }

                    d++;
                    %>
                    <tr>
                        <td colspan="2">
                            Common element:&nbsp;
                            <a href="<%=request.getContextPath()%>/dataelements/<%=delem_id%>">
                                <%=Util.processForDisplay(delem_name)%>
                            </a>
                        (<%=displayType%>)
                        </td>
                    </tr>
                    <%
                }
            } else {
                %>
                    <tr><td>You have no common elements checked out</td></tr>
                <%
            }

            // SCHEMA SETS
            if (schemaSets!=null && !schemaSets.isEmpty()){
                for (int i=0; i<schemaSets.size(); i++){

                    SchemaSet schemaSet = schemaSets.get(i);
                    %>
                    <tr>
                        <td>
                            Schema set:
                            <stripes:link beanclass="eionet.web.action.SchemaSetActionBean"><c:out value="<%=schemaSet.getIdentifier()%>"/>
                                <stripes:param name="schemaSet.id" value="<%=schemaSet.getId()%>"/>
                            </stripes:link>
                        </td>
                    </tr>
                <%
                }
            } else {
                %>
                    <tr><td>You have no schema sets checked out!</td></tr>
                <%
            }

            // SCHEMAS
            if (schemas!=null && !schemas.isEmpty()){
                for (int i=0; i<schemas.size(); i++){

                    Schema schema = schemas.get(i);
                    %>
                    <tr>
                        <td>
                            Schema:
                            <stripes:link beanclass="eionet.web.action.SchemaActionBean"><c:out value="<%=schema.getFileName()%>"/>
                                <stripes:param name="schema.id" value="<%=schema.getId()%>"/>
                            </stripes:link>
                        </td>
                    </tr>
                <%
                }
            } else {
                %>
                    <tr><td>You have no schemas checked out!</td></tr>
                <%
            }
            %>
        </tbody>
    </table>
                <%
        } else {
            %>
    <p class="advise-msg">You have <em>no objects</em> checked out</p>
            <%
        }
            %>
</div> <!-- workarea -->
</div> <!-- container -->
<%@ include file="footer.jsp" %>
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
