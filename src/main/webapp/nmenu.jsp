<%@page import="eionet.util.SecurityUtil,eionet.meta.DDUser"%>

<div id="leftcolumn" class="localnav">
    <ul>
        <li><a href="<%=request.getContextPath()%>/documentation">Hjälp och dokumentation</a></li>
        <li><a href="<%=request.getContextPath()%>/datasets.jsp">Dataset</a></li>
        <li><a href="<%=request.getContextPath()%>/tableSearch.action">Tabeller</a></li>
        <li><a href="<%=request.getContextPath()%>/searchelements">Dataelement</a></li>
        <%
        DDUser _user = SecurityUtil.getUser(request);
        if (_user!=null){
            %>
            <li><a href="<%=request.getContextPath()%>/checkedout.jsp">Dina utcheckningar</a></li>
            <li><a href="<%=request.getContextPath()%>/attributes.jsp">Attribut</a></li><%
        }

        if (SecurityUtil.userHasPerm(request, "/import", "x")){ %>
            <li><a href="<%=request.getContextPath()%>/import.jsp">Importera dataset</a></li><%
        }
        if (SecurityUtil.userHasPerm(request, "/cleanup", "x")){ %>
            <li><a href="<%=request.getContextPath()%>/clean.jsp">Rensa</a></li> <%
        }
        if (_user!=null){ %>
            <li><a href="<%=request.getContextPath()%>/subscribe.jsp">Prenumerera (RSS)</a></li><%
        }
        if (SecurityUtil.userHasPerm(request, "/schemasets", "v") || SecurityUtil.userHasPerm(request, "/schemas", "v")){ %>
          <li><a href="<%=request.getContextPath()%>/schemasets/browse/">Scheman</a></li> <%
        }
        %>
        <li><a href="<%=request.getContextPath()%>/vocabularies">Kodlistor</a></li>
        <li><a href="<%=request.getContextPath()%>/services/list">Tjänster</a></li>
        <li><a href="<%=request.getContextPath()%>/namespaces">Namnrymd</a></li>
    </ul>
</div>
