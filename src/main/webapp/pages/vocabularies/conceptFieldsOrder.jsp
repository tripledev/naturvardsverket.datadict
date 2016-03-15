<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Vocabulary concept fields order">

    <stripes:layout-component name="head">
        <script type="text/javascript">
        // <![CDATA[
        // ]]>
        </script>
    </stripes:layout-component>

    <stripes:layout-component name="contents">

            <div id="drop-operations">
                <h2>Operations:</h2>
                <ul>
                    <li><stripes:link id="searchLnk" href="#">Search vocabularies</stripes:link></li>
                    <li><stripes:link id="searchConceptLnk" href="#">Search concepts</stripes:link></li>
                </ul>
            </div>

        <h1>Modify vocabulary concept fields order</h1>

        <display:table name="${actionBean.orderElements}" class="sortable" id="orderElement" sort="list" requestURI="${actionBean.urlBinding}">

            <display:setProperty name="basic.msg.empty_list" value="No order elements found." />
            <display:setProperty name="paging.banner.item_name" value="order element" />
            <display:setProperty name="paging.banner.items_name" value="order elements" />

            <display:column title="Fixed attr name">
                <c:out value="${orderElement.fixedAttrName}" />
            </display:column>
            <display:column title="Bound element ID">
                <c:out value="${orderElement.boundElemId}" />
            </display:column>
        </display:table>

    </stripes:layout-component>
</stripes:layout-render>
