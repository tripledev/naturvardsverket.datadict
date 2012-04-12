<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<%@page import="net.sourceforge.stripes.action.ActionBean"%>

<stripes:layout-render name="/pages/common/template.jsp"
    pageTitle="Schema sets">

    <stripes:layout-component name="contents">

        <h1>Schema sets</h1>

        <div id="drop-operations">
            <h2>Operations:</h2>
            <ul>
                <li><a href="${pageContext.request.contextPath}/schemaSet.action?add=">Add schema set</a></li>
                <li><a href="${pageContext.request.contextPath}/searchSchemaSets.action">Search schema sets</a></li>
            </ul>
        </div>

        <c:if test="${empty actionBean.schemaSets}">
            <div>
                No schema sets found. Note that unauthenticated users can only see schema sets with released status.
            </div>
        </c:if>

        <stripes:form action="/schemaSets.action" method="post">
            <ul class="menu">
                <c:forEach var="item" items="${actionBean.schemaSets}">
                    <li>
                    <c:if test="${actionBean.deletePermission}">
                        <stripes:checkbox name="selected" value="${item.id}" />
                    </c:if>
                    <stripes:link href="/schemaSet.action" class="link-folder">
                        <stripes:param name="schemaSet.id" value="${item.id}" />
                        <c:out value="${item.identifier}" />
                    </stripes:link>
                    </li>
                </c:forEach>
            </ul>
            <br />
            <c:if test="${actionBean.deletePermission && not empty actionBean.schemaSets}">
                <stripes:submit name="delete" value="Delete" />
            </c:if>

        </stripes:form>

    </stripes:layout-component>

</stripes:layout-render>