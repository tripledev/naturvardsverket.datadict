<%@page import="java.util.*"%>

<%
Vector links = (Vector)request.getAttribute("quicklinks");
if (links!=null && links.size()>0){
    %>
        <div id="quickbar" style="clear:right">
                        <b>Quick links</b><%
                        
                        for (int i=0; i<links.size(); i++){
                            String link = (String)links.get(i);
                            int j = link.indexOf("|");
                            String name = link.substring(j+1).trim();
                            String dispName = link.substring(0,j).trim();
                            %>
                            &nbsp;|&nbsp;<a href="#<%=name%>"><%=dispName%></a><%
                        }
                        %>
        </div><%
}
%>
