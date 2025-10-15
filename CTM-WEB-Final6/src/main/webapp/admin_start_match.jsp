<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*, com.ctm.model.Tournament" %>
<%
  String role=(String)session.getAttribute("role");
  if(role==null||!"admin".equalsIgnoreCase(role)){response.sendRedirect("index.jsp");return;}
  response.setHeader("Cache-Control","no-cache,no-store,must-revalidate");
  response.setHeader("Pragma","no-cache");
  response.setDateHeader("Expires",0);

  String mode=(String)request.getAttribute("mode");
  if(mode==null) mode="tournaments";
  String msg=(String)request.getAttribute("msg");
  String err=(String)request.getAttribute("err");
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Start Match</title>
<link rel="stylesheet" href="resources/css/admin_start_match.css">
</head>
<body>
<div class="top">
  <div class="brand">🎯 Start Match</div>
  <div><a href="adminmain.jsp" class="link">Home</a><a href="logout" class="link">Logout</a></div>
</div>

<div class="wrap">
  <% if(msg!=null){ %><div class="banner success"><%= msg %></div><% } %>
  <% if(err!=null){ %><div class="banner error"><%= err %></div><% } %>

  <% if("tournaments".equals(mode)){
       List<Tournament> tours=(List<Tournament>)request.getAttribute("tournaments");
       Map<Long,Integer> todayCounts=(Map<Long,Integer>)request.getAttribute("todayCounts");
  %>
    <h2>Tournaments with matches today</h2>
    <% if(tours==null||tours.isEmpty()){ %>
      <p class="empty">No tournaments available.</p>
    <% } else { %>
      <table>
        <tr><th>ID</th><th>Name</th><th>Scheduled Today</th><th>Action</th></tr>
        <% for(Tournament t:tours){ %>
          <tr>
            <td><%=t.getId()%></td>
            <td><%=t.getName()%></td>
            <td><span class="chip <%= todayCounts.getOrDefault(t.getId(),0)>0?"chip-ok":"chip-bad" %>"><%=todayCounts.getOrDefault(t.getId(),0)%></span></td>
            <td><a class="primary" href="startmatch?tid=<%=t.getId()%>">View Matches</a></td>
          </tr>
        <% } %>
      </table>
    <% } %>
  <% } else if("matches".equals(mode)) {
       List<Map<String,Object>> matches=(List<Map<String,Object>>)request.getAttribute("matches");
       com.ctm.model.Tournament t=(com.ctm.model.Tournament)request.getAttribute("tournament");
  %>
    <h2>Today's Matches — <%= t!=null ? t.getName() : "" %></h2>
    <% if(matches==null||matches.isEmpty()){ %>
      <p class="empty">No scheduled matches for today.</p>
      <div class="actions"><a class="link" href="startmatch">← Back to tournaments</a></div>
    <% } else { %>
      <table>
        <tr><th>ID</th><th>Match</th><th>Venue</th><th>Date</th><th>Action</th></tr>
        <% for(Map<String,Object> m:matches){ %>
          <tr>
            <td><%=m.get("id")%></td>
            <td><%=m.get("aName")%> vs <%=m.get("bName")%></td>
            <td><%=m.get("venue")%></td>
            <td><%=m.get("datetime")%></td>
            <td>
              <form method="post" action="startmatch">
                <input type="hidden" name="matchId" value="<%=m.get("id")%>">
                <input type="hidden" name="tid" value="<%= t!=null ? t.getId() : "" %>">
                <button class="primary" type="submit">Conduct Toss &amp; Start</button>
              </form>
            </td>
          </tr>
        <% } %>
      </table>
      <div class="actions"><a class="link" href="startmatch">← Back to tournaments</a></div>
    <% } %>
  <% } %>
</div>

<script>
if(window.history.replaceState) window.history.replaceState(null,null,window.location.href);
</script>
</body>
</html>
