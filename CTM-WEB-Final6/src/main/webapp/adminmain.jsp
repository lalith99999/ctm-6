<%@ page contentType="text/html; charset=UTF-8" %>
<%
  String username = (String) session.getAttribute("username");
  String role = (String) session.getAttribute("role");
  if (username == null || role == null || !"admin".equalsIgnoreCase(role)) {
      response.sendRedirect("index.jsp"); return;
  }
  response.setHeader("Cache-Control","no-cache,no-store,must-revalidate");
  response.setHeader("Pragma","no-cache");
  response.setDateHeader("Expires",0);
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Admin Dashboard</title>
<link rel="stylesheet" href="resources/css/adminmain.css">
<script>
window.history.replaceState && window.history.replaceState(null, "", window.location.href);
window.onpopstate = function(){ window.location.replace('adminmain.jsp'); };
</script>
</head>
<body>
<div class="top">
  <div class="brand">🛠️ Admin Panel — Welcome <b><%= username %></b></div>
  <div><a class="logout" href="adminmain.jsp">Home</a> <a class="logout" href="logout">Logout</a></div>
</div>

<div class="wrap">
  <div class="panel">
    <h2>Dashboard</h2>
    <div class="actions">
      <a class="action-btn" href="admteams">📋 Manage Teams</a>
      <a class="action-btn" href="admtournaments">🏆 Manage Tournaments</a>
      <a class="action-btn" href="enroll">➕ Enroll Teams</a>
      <a class="action-btn" href="fixturesgen">📅 Generate Fixtures</a>
      <a class="action-btn" href="startmatch">🎯 Start Match</a>
      <a class="action-btn" href="updateinnings">📝 Update Innings</a>
    </div>
  </div>
</div>
</body>
</html>
