<%@page import="java.util.*,eionet.util.SecurityUtil,eionet.util.Props,eionet.util.PropsIF,eionet.meta.DDUser,eionet.meta.LoginServlet"%>
<%
ServletContext ctx = getServletContext();
String appName = ctx.getInitParameter("application-name");
%>
<div id="toolribbon">
    <div id="lefttools">
        <%@ include file="topleftlinks.txt" %>
    </div>
    <div id="righttools">
        <%
        DDUser _user = SecurityUtil.getUser(request);
        if (_user!=null){
            %>
            <a id="logoutlink" href="<%=request.getContextPath()%>/logout.jsp" title="Logout">Logga ut (<%=_user.getUserName()%>)</a><%
        }
        else{
            %>
            <a id="loginlink" href="<%=SecurityUtil.getLoginURL(request)%>" title="Login">Logga in</a><%
        }

        String helpScreen = request.getParameter("helpscreen");
        if (helpScreen!=null){
            %>
            <a id="pagehelplink" title="Get help on this page" href="<%=request.getContextPath()%>/help.jsp?screen=<%=helpScreen%>&amp;area=pagehelp" onclick="pop(this.href);return false;"><span>Hj&auml;lp</span></a><%
        }
        %>
        <a id="printlink" title="Print this page" href="javascript:this.print();"><span>Print</span></a>
        <a id="fullscreenlink" href="javascript:toggleFullScreenMode()" title="Switch to/from full screen mode"><span>Switch to/from full screen mode</span></a>
        <a id="acronymlink" href="http://www.miljodatasamverkan.se/datadict/about.action" title="Look up acronyms"><span>F&ouml;rkortningar</span></a>
        <form action="http://google.com/search" method="get">
          <div id="freesrchform">
            <label for="freesrchfld">S&ouml;k</label>
            <input type="text" id="freesrchfld" name="q"
             onfocus="if(this.value=='S&ouml;k p&aring; sidan')this.value='';"
             onblur="if(this.value=='')this.value='S&ouml;k p&aring; sidan';"
             value="S&ouml;k p&aring; sidan"/>
             <input type="hidden" name="sitesearch" value="<%=Props.getProperty(PropsIF.JSP_URL_PREFIX)%>" />
            <input id="freesrchbtn" type="image" src="<%=request.getContextPath()%>/images/button_go.gif" alt="Go"/>
          </div>
        </form>
    </div>
</div> <!-- toolribbon -->

<div id="pagehead">
    <%@ include file="pagehead.jsp" %>
</div> <!-- pagehead -->


<div id="menuribbon">
    <%@ include file="dropdownmenus.txt" %>
</div>
<div class="breadcrumbtrail">
    <div class="breadcrumbhead">Du är här:</div>
    <div class="breadcrumbitem eionetaccronym"><a href="http://www.miljodatasamverkan.se">Miljödatasamverkan</a></div>
    <%
    String contextName = request.getParameter("context_name");
    String contextPath = request.getParameter("context_path");
    if (contextPath==null)
        contextPath = "";
    String lastItemName = request.getParameter("name");
    if (lastItemName!=null && contextName==null){
        %>
        <div class="breadcrumbitem"><a href="<%=request.getContextPath()%>/index.jsp"><%=appName %></a></div>
        <div class="breadcrumbitemlast"><%=lastItemName%></div><%
    }
    else if (lastItemName==null && contextName!=null){
        %>
        <div class="breadcrumbitem"><a href="<%=request.getContextPath()%>/index.jsp"><%=appName %></a></div>
        <div class="breadcrumbitemlast"><%=contextName%></div><%
    }
    else if (lastItemName!=null && contextName!=null){
        %>
        <div class="breadcrumbitem"><a href="<%=request.getContextPath()%>/index.jsp"><%=appName %></a></div>
        <div class="breadcrumbitem"><a href="<%=contextPath%>"><%=contextName%></a></div>
        <div class="breadcrumbitemlast"><%=lastItemName%></div><%
    }
    else if (lastItemName==null && contextName==null){
        %>
        <div class="breadcrumbitemlast"><%=appName %></div><%
    }
    %>
    <div class="breadcrumbtail">
    </div>
</div>
