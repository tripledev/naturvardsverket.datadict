<%@page contentType="text/html;charset=UTF-8" import="eionet.meta.*,java.sql.*,java.util.*,java.io.*,eionet.util.sql.ConnectionUtil,eionet.util.*,eionet.web.action.*,net.sourceforge.stripes.action.UrlBinding"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<%
    response.setHeader("Pragma", "No-cache");
    response.setHeader("Cache-Control", "no-cache,no-store,max-age=0");
    response.setHeader("Expires", Util.getExpiresDateString());

    request.setCharacterEncoding("UTF-8");
    session.setAttribute("imgattr_qrystr", request.getQueryString());

    ServletContext ctx = getServletContext();
    DDUser user = SecurityUtil.getUser(request);

    // POST request not allowed for anybody who hasn't logged in
    if (request.getMethod().equals("POST") && user==null){
        request.setAttribute("DD_ERR_MSG", "You have no permission to POST data!");
        request.getRequestDispatcher("error.jsp?class=popup").forward(request, response);
        return;
    }

    // get vital request parameters
    String objID = request.getParameter("obj_id");
    if (objID == null || objID.length()==0){
        request.setAttribute("DD_ERR_MSG", "Missing request parameter: obj_id");
        request.getRequestDispatcher("error.jsp?class=popup").forward(request, response);
        return;
    }
    String objType = request.getParameter("obj_type");
    if (objType==null || objType.length()==0){
        request.setAttribute("DD_ERR_MSG", "Missing request parameter: obj_type");
        request.getRequestDispatcher("error.jsp?class=popup").forward(request, response);
        return;
    }
    String attrID = request.getParameter("attr_id");
    if (attrID==null || attrID.length()==0){
        request.setAttribute("DD_ERR_MSG", "Missing request parameter: attr_id");
        request.getRequestDispatcher("error.jsp?class=popup").forward(request, response);
        return;
    }
    String objName = request.getParameter("obj_name");
    String attrName = request.getParameter("attr_name");

    String titleLink = "";
    String titleType = "";
    if (objType.equals("E")){
        titleType = " element";
        titleLink = request.getContextPath() + "/dataelements/" + objID + "/edit";
    }
    else if (objType.equals("T")){
        titleType = " table";
        titleLink = request.getContextPath() + "/tables/" + objID;
    }
    else if (objType.equals("DS")){
        request.setAttribute("DD_ERR_MSG", "Images not allowed for datasets. Use data model instead.");
        request.getRequestDispatcher("error.jsp?class=popup").forward(request, response);
        return;
    }
    else if (objType.equals(DElemAttribute.ParentType.SCHEMA_SET.toString())){
        titleType = " schema set";
        titleLink = request.getContextPath() + SchemaSetActionBean.class.getAnnotation(UrlBinding.class).value() + "?edit=&schemaSet.id=" + objID;
    }

    Connection conn = null;

    // the whole page's try block
    try {
        conn = ConnectionUtil.getConnection();
        DDSearchEngine searchEngine = new DDSearchEngine(conn, "");
        Vector attrs = searchEngine.getSimpleAttributes(objID, objType);
%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
    <%@ include file="headerinfo.jsp" %>
    <title>Data Dictionary</title>
    <script type="text/javascript">
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
            var trailer = "?fileORurl=" + radio + "&url_input=" + url + "&file_input=" + file;
            trailer = trailer + "&obj_id=" + document.forms["Upload"].elements["obj_id"].value;

            var oType = document.forms["Upload"].elements["obj_type"];
            if (oType != null)
                trailer = trailer + "&obj_type=" + oType.value;

            var oAttrID = document.forms["Upload"].elements["attr_id"];
            if (oAttrID != null)
                trailer = trailer + "&attr_id=" + oAttrID.value;

            document.forms["Upload"].action = document.forms["Upload"].action + trailer;
            document.forms["Upload"].submit();
        }
    }

    // ]]>
    </script>
</head>
<body>

<div id="container">
<jsp:include page="nlocation.jsp" flush="true">
        <jsp:param name="name" value="Image attribute"/>
        <jsp:param name="helpscreen" value="img_attr"/>
    </jsp:include>
<%@ include file="nmenu.jsp" %>

<div id="workarea" style="clear:right;">
<div id="operations">
    <ul>
        <li><a href="<%=Util.processForDisplay(titleLink, true)%>">&lt; back to the <%=titleType%></a></li>
    </ul>
</div>
<form id="Upload" action="ImgUpload" method="post" enctype="multipart/form-data">

    <h1>
        <%=Util.processForDisplay(attrName)%> of <a href="<%=Util.processForDisplay(titleLink, true)%>"><%=Util.processForDisplay(objName, true)%></a> <%=Util.processForDisplay(titleType)%>
    </h1>

    <table cellspacing="0" style="width:auto;margin-top:10px">
        <tr>
            <td align="left" style="padding-right:5">
                <input type="radio" name="fileORurl" value="file" checked="checked"/>&nbsp;File:
            </td>
            <td align="left">
                <input type="file" class="smalltext" name="file_input" size="40"/>
            </td>
        </tr>
        <tr>
            <td align="left" style="padding-right:5">
                <input type="radio" class="smalltext" name="fileORurl" value="url"/>&nbsp;URL:
            </td>
            <td align="left">
                <input type="text" class="smalltext" name="url_input" size="52"/>
            </td>
        </tr>
        <tr>
            <td></td>
            <td align="left">
                <input name="SUBMIT" type="button" class="mediumbuttonb" value="Add" onclick="submitForm('upload')" onkeypress="submitForm('upload')"/>&nbsp;&nbsp;
                <input name="REMOVE" type="button" class="mediumbuttonb" value="Remove selected" onclick="submitForm('remove')" onkeypress="submitForm('remove')"/>
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
                            <input type="checkbox" name="file_name" value="<%=Util.processForDisplay(value, true)%>"/>
                        </td>
                        <td align="left" style="border:1px solid black;padding:5px">
                            <img src="visuals/<%=Util.processForDisplay(value, true)%>" alt="The registered image file could not be found. Use the checkbox and 'Remove selected' button to remove it and then upload again."/>
                        </td>
                    </tr>
                    <tr height="10"><td colspan="2">&nbsp;</td></tr> <%
                    displayed++;
                }
            }
        }

        if (!found){%>
            <tr>
                <td align="left" colspan="2">
                    <b>
                        No images found! You can add by using the form above.<br/>
                        Please note that you can only add images of JPG, GIF or PNG.
                    </b>
                </td>
            </tr><%
        }

        %>

    </table>

    <div style="display:none">
        <input type="hidden" name="obj_id" value="<%=objID%>"/>
        <input type="hidden" name="obj_type" value="<%=objType%>"/>
        <input type="hidden" name="attr_id" value="<%=attrID%>"/>

        <input type="hidden" name="mode" value="add"/>

        <input type="hidden" name="redir_url" value="imgattr.jsp?<%=Util.processForDisplay(request.getQueryString(), true)%>"/>
    </div>
</form>
</div> <!-- workarea -->
</div> <!-- container -->
<%@ include file="footer.jsp" %>

</body>
</html>

<%
// end the whole page try block
}
catch (Exception e){
    if (response.isCommitted())
        e.printStackTrace(System.out);
    else{
        String msg = e.getMessage();
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        e.printStackTrace(new PrintStream(bytesOut));
        String trace = bytesOut.toString(response.getCharacterEncoding());
        request.setAttribute("DD_ERR_MSG", msg);
        request.setAttribute("DD_ERR_TRC", trace);
        request.getRequestDispatcher("error.jsp?class=popup").forward(request, response);
        return;
    }
}
finally {
    try { if (conn!=null) conn.close();
    } catch (SQLException e) {}
}
%>