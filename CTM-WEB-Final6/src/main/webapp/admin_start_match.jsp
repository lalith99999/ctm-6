<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*, com.ctm.model.Tournament" %>
<%
  String role=(String)session.getAttribute("role");
  if(role==null||!"admin".equalsIgnoreCase(role)){response.sendRedirect("index.jsp");return;}
  response.setHeader("Cache-Control","no-cache,no-store,must-revalidate");
  response.setHeader("Pragma","no-cache");
  response.setDateHeader("Expires",0);

  String mode=(String)request.getAttribute("mode");
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
  <div><a href="adminmain.jsp" class="logout">Home</a><a href="logout" class="logout">Logout</a></div>
</div>

<div class="wrap">
<% if("tournaments".equals(mode)){ 
     List<Tournament> tours=(List<Tournament>)request.getAttribute("tournaments");
     Map<Long,Integer> todayCounts=(Map<Long,Integer>)request.getAttribute("todayCounts");
%>
  <h2>Select Tournament</h2>
  <table>
    <tr><th>ID</th><th>Name</th><th>Scheduled Today</th><th>Action</th></tr>
    <% for(Tournament t:tours){ %>
      <tr>
        <td><%=t.getId()%></td><td><%=t.getName()%></td>
        <td><%=todayCounts.getOrDefault(t.getId(),0)%></td>
        <td><a class="link" href="startmatch?tid=<%=t.getId()%>">View Matches</a></td>
      </tr>
    <% } %>
  </table>
<% } else if("matches".equals(mode)){ 
     List<Map<String,Object>> matches=(List<Map<String,Object>>)request.getAttribute("matches");
%>
  <h2>Today's Matches</h2>
  <table>
    <tr><th>ID</th><th>Teams</th><th>Venue</th><th>Toss</th></tr>
    <% for(Map<String,Object> m:matches){ %>
      <tr>
        <td><%=m.get("id")%></td>
        <td><%=m.get("aName")%> vs <%=m.get("bName")%></td>
        <td><%=m.get("venue")%></td>
        <td>
          <form method="post" action="startmatch">
            <input type="hidden" name="matchId" value="<%=m.get("id")%>">
            <select name="tossWinnerId" required>
              <option value="<%=m.get("aId")%>"><%=m.get("aName")%></option>
              <option value="<%=m.get("bId")%>"><%=m.get("bName")%></option>
            </select>
            <select name="tossDecision" required>
              <option value="BAT">BAT</option>
              <option value="BOWL">BOWL</option>
            </select>
            <button>Start</button>
          </form>
        </td>
      </tr>
    <% } %>
  </table>
<% } %>
</div>

<script>
if(window.history.replaceState) window.history.replaceState(null,null,window.location.href);
</script>
</body>
</html>
