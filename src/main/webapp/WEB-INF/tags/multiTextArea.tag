<%@ include file="/pages/common/taglibs.jsp"%>
<%@ attribute name="uniqueId" required="true" %>
<%@ attribute name="attributeId" required="true" %>
<%@ attribute name="fieldName" required="true" %>
<%@ attribute name="attributes" required="true" type="java.util.ArrayList" %>
<%@ attribute name="fieldRows" required="false" %>
<%@ attribute name="fieldCols" required="false" %>
<%@ attribute name="fieldClass" required="false" %>

<%--
    Input tag for text attribute which supports multiple values.
    For the delete button to work, removeField(elementId) javaScript function must be included.
    Parameters:
        - attributes must be List of eionet.meta.dao.domain.VocabularyConceptAttribute objects.
        - attributeId numeric id of the attribute definition (in M_ATTRIBUTE table).
        - fieldName must be the Stripes bean property name (actionBean.vocabularyConcept.testAttribute)
        - uniqueId id that is used as suffix for ids of different html elements
        - fieldRows, fieldCols and fieldClass are optional with default values "50", "3" and "smalltext"
 --%>

<c:if test="${empty fieldRows}">
    <c:set var="fieldRows" value="3" />
</c:if>
<c:if test="${empty fieldCols}">
    <c:set var="fieldCols" value="40" />
</c:if>
<c:if test="${empty fieldClass}">
    <c:set var="fieldClass" value="smalltext" />
</c:if>

<c:url var="addIcon" value="/images/button_plus.gif" />
<c:url var="delIcon" value="/images/button_remove.gif" />

<script type="text/javascript">
// <![CDATA[
( function($) {
    $(document).ready(function() {
        var currentSize = ${fn:length(attributes)};

        $("#multiAdd${uniqueId}").live("click", function(event){
            var newValue = $("#newField${uniqueId}").val();
            var newField = "<span id='multySpan${uniqueId}-" + currentSize + "'>";
            newField += "<input type='hidden' name='${fieldName}[" + currentSize + "].attributeId' value='${attributeId}' />";
            newField += "<textarea class='${fieldClass}' size='${fieldSize}' rows='${fieldRows}' cols='${fieldCols}' name='${fieldName}[" + currentSize + "].value'>" + newValue + "</textarea>";
            newField += " <a href='#' onclick='removeField(\"multySpan${uniqueId}-" + currentSize + "\")'><img style='border:0' src='${delIcon}' alt='Remove' /></a><br></span>";
            var multiDivHtml = $("#multiDiv${uniqueId}").html();
            multiDivHtml += newField;
            $("#multiDiv${uniqueId}").html(multiDivHtml);
            $("#newField${uniqueId}").val('');
            currentSize++;
            event.preventDefault();
        });

    });
} ) ( jQuery );
// ]]>
</script>

<div id="multiDiv${uniqueId}">
    <textarea id="newField${uniqueId}" class="${fieldClass}" rows="${fieldRows}" cols="${fieldCols}"></textarea>
    <a href='#' id="multiAdd${uniqueId}"><img style='border:0' src='${addIcon}' alt='Add' /></a><br>

    <c:forEach var="attr" items="${attributes}" varStatus="innerLoop">
        <c:if test="${attr.id != 0}">
        <span id="multySpan${uniqueId}-${innerLoop.index}">
            <input type="hidden" name="${fieldName}[${innerLoop.index}].id" value="${attr.id}" />
            <textarea name="${fieldName}[${innerLoop.index}].value" class="${fieldClass}" rows="${fieldRows}" cols="${fieldCols}">${attr.value}</textarea>
            <a href='#' onclick="removeField('multySpan${uniqueId}-${innerLoop.index}')"><img style='border:0' src='${delIcon}' alt='Remove' /></a><br>
        </span>
        </c:if>
    </c:forEach>
</div>