<%@page contentType="text/html;charset=UTF-8"%>

<%@ include file="/pages/common/taglibs.jsp"%>

<%@page import="net.sourceforge.stripes.action.ActionBean"%>


<stripes:layout-render name="/pages/common/template.jsp" pageTitle="Vocabularies">

    <stripes:layout-component name="contents">
        <div id="drop-operations">
            <h2>Operations:</h2>
            <ul>
                <li><stripes:link id="searchLnk" href="#">Search again</stripes:link></li>
                <li><stripes:link href="/vocabularies" event="form">Back to vocabularies</stripes:link></li>
            </ul>
        </div>

        <h1>Vocabularies</h1>

        <p class="advise-msg">
            Note: Unauthenticated users can only see vocabularies in <em>Released</em> and <em>Public Draft</em> statuses.
        </p>

        <display:table name="${actionBean.vocabularyResult.list}" class="sortable" id="item" requestURI="/vocabularies/search"  pagesize="20">
            <display:column title="Vocabulary Set" sortable="true" sortProperty="folderName">
                ${item.folderName}
            </display:column>
            <display:column title="Vocabulary" sortable="true" sortProperty="label">
                <c:choose>
                    <c:when test="${item.workingCopy}">
                        <c:choose>
                            <c:when test="${actionBean.userName eq item.workingUser}">
                                <stripes:link href="/vocabulary/${item.folderName}/${item.identifier}/view">
                                    <c:out value="${item.label}"/>
                                    <stripes:param name="vocabularyFolder.workingCopy" value="${item.workingCopy}" />
                                </stripes:link>
                                <span title="Your working copy" class="checkedout">*</span>
                            </c:when>
                            <c:otherwise>
                                <c:out value="${item.label}" /> <span title="Checked out by ${item.workingUser}" class="checkedout">*</span>
                            </c:otherwise>
                        </c:choose>
                    </c:when>

                    <c:when test="${not item.draftStatus || actionBean.userLoggedIn}">
                        <stripes:link href="/vocabulary/${item.folderName}/${item.identifier}/view"><c:out value="${item.label}" /></stripes:link>
                    </c:when>
                    <c:otherwise>
                        <c:out value="${item.label}" />
                    </c:otherwise>
                </c:choose>
            </display:column>
            <display:column title="Status" sortable="true" sortProperty="regStatus">
                ${item.regStatus.label}
            </display:column>
        </display:table>
    <jsp:include page="searchVocabulariesInc.jsp" />
    </stripes:layout-component>

</stripes:layout-render>

