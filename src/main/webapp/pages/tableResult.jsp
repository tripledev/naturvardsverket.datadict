<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<%@page import="net.sourceforge.stripes.action.ActionBean"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Tables">

    <stripes:layout-component name="contents">
        <div id="drop-operations">
            <h2>Operations:</h2>
            <ul>
                <li><stripes:link href="/tableSearch.action" event="form">Search</stripes:link></li>
            </ul>
        </div>

        <h1>Senaste versioner av tabeller, oavsett status (draft etc.)</h1>

        <p class="advise-msg">
            Viktigt: Tabeller fr&aring;n dataset som inte har 
            <em>Recorded</em> eller <em>Released</em> status &auml;r otillg&auml;ngliga f&ouml;r anonyma anv&auml;ndare.
        </p>

        <display:table name="${actionBean.dataSetTables}" class="sortable" id="item" sort="list"
            decorator="eionet.web.decorators.TableResultDecorator" requestURI="/tableSearch.action">
            <display:column title="Full name" sortable="true">
                <c:choose>
                    <c:when test="${item.statusReleased || actionBean.userLoggedIn}">
                        <stripes:link href="/tables/${item.id}"><c:out value="${item.name}" /></stripes:link>
                    </c:when>
                    <c:otherwise>
                        <c:out value="${item.name}" />
                    </c:otherwise>
                </c:choose>
            </display:column>
            <display:column property="shortName" title="Short name" sortable="true" />
            <display:column property="dataSetName" title="Dataset" sortable="true" />
            <display:column title="Dataset status" sortable="true">
                <c:url var="imgSrc" value="/images/${item.statusImage}" />
                <img src="${imgSrc}" border="0" title="${item.dataSetStatus}" />
            </display:column>
        </display:table>
    </stripes:layout-component>

</stripes:layout-render>
