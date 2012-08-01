<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<%@page import="net.sourceforge.stripes.action.ActionBean"%>

<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Search data elements">

    <stripes:layout-component name="head">
        <script type="text/javascript">
        // <![CDATA[

        function deleteAttribute(id) {
            var input = document.createElement("input");
            input.setAttribute("type", "hidden");
            input.setAttribute("name", "delAttr");
            input.setAttribute("value", id);

            document.getElementById("searchForm").appendChild(input);
            document.getElementById("searchForm").submit();
        }

        // ]]>
        </script>
    </stripes:layout-component>

    <stripes:layout-component name="contents">
    <h1>Search data elements</h1>

    <c:if test="${actionBean.permissionToAdd}">
        <div id="drop-operations">
            <h2>Operations:</h2>
            <ul>
                <li>
                    <stripes:link href="/dataelements/add/">
                        <stripes:param name="common" value="true" />
                        New common element
                    </stripes:link>
                </li>
            </ul>
        </div>
    </c:if>

    <c:url value="/images/info_icon.gif" var="infoIcon" />
    <c:url value="/images/button_remove.gif" var="removeIcon" />

    <stripes:form id="searchForm" beanclass="eionet.web.action.SearchDataElementsActionBean" method="get">
        <table width="auto" cellspacing="0" style="clear:right">
            <tr valign="top">
                <td align="right" style="padding-right:10">
                    <strong>RegistrationStatus</strong>
                </td>
                <td>
                    <a href="help.jsp?screen=dataset&area=regstatus" onclick="pop(this.href);return false;">
                        <img style="border:0" src="${infoIcon}" alt="Help" width="16" height="16"/>
                    </a>
                </td>
                <td colspan="2">
                    <stripes:select name="filter.regStatus" class="small">
                        <stripes:option value="" label="All" />
                        <stripes:options-collection collection="${actionBean.regStatuses}"/>
                    </stripes:select>
                </td>
            </tr>
            <tr valign="top">
                <td align="right" style="padding-right:10">
                    <b>Dataset</b>
                </td>
                <td>
                    <a href="help.jsp?screen=search_element&amp;area=dataset" onclick="pop(this.href);return false;">
                        <img style="border:0" src="${infoIcon}" alt="Help" width="16" height="16"/>
                    </a>
                </td>
                <td colspan="2">
                    <stripes:select name="filter.dataSet" class="small">
                        <stripes:option value="" label="All" />
                        <stripes:options-collection collection="${actionBean.dataSets}" value="identifier" label="shortName" />
                    </stripes:select>
                </td>
            </tr>
            <tr valign="top">
                <td align="right" style="padding-right:10">
                    <b>Type</b>
                </td>
                <td>
                    <a href="help.jsp?screen=element&amp;area=type" onclick="pop(this.href);return false;">
                        <img style="border:0" src="${infoIcon}" alt="Help" width="16" height="16"/>
                    </a>
                </td>
                <td colspan="2">
                    <stripes:select name="filter.type" class="small">
                        <stripes:option value="" label="All" />
                        <stripes:option value="CH1" label="Data element with fixed values (codes)" />
                        <stripes:option value="CH2" label="Data element with quantitative values (e.g. measurements)" />
                    </stripes:select>
                </td>
            </tr>
            <tr valign="top">
                <td align="right" style="padding-right:10">
                    <b>Short name</b>
                </td>
                <td>
                    <a href="help.jsp?screen=dataset&amp;area=short_name" onclick="pop(this.href);return false;">
                        <img style="border:0" src="${infoIcon}" alt="Help" width="16" height="16"/>
                    </a>
                </td>
                <td colspan="2">
                    <stripes:text name="filter.shortName" class="smalltext" size="59" />
                </td>
            </tr>
            <tr valign="top">
                <td align="right" style="padding-right:10">
                    <b>Identifier</b>
                </td>
                <td>
                    <a href="help.jsp?screen=dataset&amp;area=identifier" onclick="pop(this.href);return false;">
                        <img style="border:0" src="${infoIcon}" alt="Help" width="16" height="16"/>
                    </a>
                </td>
                <td colspan="2">
                    <stripes:text name="filter.identifier" class="smalltext" size="59" />
                </td>
            </tr>
            <c:forEach items="${actionBean.filter.attributes}" var="attr" varStatus="row">
                <tr valign="top">
                    <td align="right" style="padding-right:10">
                        <b><c:out value="${attr.shortName}" /></b>
                    </td>
                    <td>
                        <a href="help.jsp?attrid=${attr.id}&amp;attrtype=SIMPLE" onclick="pop(this.href);return false;">
                            <img style="border:0" src="${infoIcon}" alt="Help" width="16" height="16"/>
                        </a>
                    </td>
                    <td colspan="2">
                        <stripes:hidden name="filter.attributes[${row.index}].id" />
                        <stripes:hidden name="filter.attributes[${row.index}].name" />
                        <stripes:hidden name="filter.attributes[${row.index}].shortName" />
                        <stripes:text name="filter.attributes[${row.index}].value" class="smalltext" size="59" />
                    </td>
                </tr>
            </c:forEach>
            <c:forEach items="${actionBean.addedAttributes}" var="attr" varStatus="row">
                <tr valign="top">
                    <td align="right" style="padding-right:10">
                        <b><c:out value="${attr.shortName}" /></b>
                    </td>
                    <td>
                        <a href="help.jsp?attrid=${attr.id}&amp;attrtype=SIMPLE" onclick="pop(this.href);return false;">
                            <img style="border:0" src="${infoIcon}" alt="Help" width="16" height="16"/>
                        </a>
                    </td>
                    <td colspan="2">
                        <stripes:hidden name="addedAttributes[${row.index}].id" />
                        <stripes:hidden name="addedAttributes[${row.index}].name" />
                        <stripes:hidden name="addedAttributes[${row.index}].shortName" />
                        <stripes:text name="addedAttributes[${row.index}].value" class="smalltext" size="59" />
                        <img src="${removeIcon}" border="0" onclick="deleteAttribute(${attr.id})" />
                    </td>
                </tr>
            </c:forEach>
            <tr valign="top">
                <td colspan="2">&nbsp;</td>
                <td colspan="2">
                    <stripes:radio name="filter.elementType" value="${actionBean.filter.nonCommonElementType}" checked="${actionBean.filter.nonCommonElementType}" /> Non-common elements
                    <stripes:radio name="filter.elementType" value="${actionBean.filter.commonElementType}" /> Common elements
                </td>
            </tr>
            <tr valign="top">
                <td colspan="2">&nbsp;</td>
                <td colspan="2">
                    <stripes:checkbox name="filter.includeHistoricVersions" /> Include historic versions
                </td>
            </tr>
            <tr>
                <td colspan="2">&nbsp;</td>
                <td colspan="2">
                    Add criteria:
                    <stripes:select name="addAttr" class="small" onchange="this.form.submit();" >
                        <stripes:option value="0" label="" />
                        <stripes:options-collection collection="${actionBean.addableAttributes}" label="shortName" value="id"/>
                    </stripes:select>
                </td>
            </tr>
            <tr>
                <td colspan="2">&nbsp;</td>
                <td colspan="2"><br /><stripes:submit name="search" value="Search"/></td>
            </tr>
        </table>
    </stripes:form>

    </stripes:layout-component>

</stripes:layout-render>