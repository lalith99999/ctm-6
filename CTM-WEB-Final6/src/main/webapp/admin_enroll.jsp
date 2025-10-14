<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*, com.ctm.model.Team, com.ctm.model.TeamStanding, com.ctm.model.Tournament" %>
<%
  String role = (String) session.getAttribute("role");
  if (role == null || !"admin".equalsIgnoreCase(role)) { response.sendRedirect("index.jsp"); return; }
  response.setHeader("Cache-Control","no-cache,no-store,must-revalidate");
  response.setHeader("Pragma","no-cache");
  response.setDateHeader("Expires",0);

  List<Tournament> tournaments = (List<Tournament>) request.getAttribute("tournaments");
  Set<Long> lockedIds = (Set<Long>) request.getAttribute("lockedIds");
  Tournament selected = (Tournament) request.getAttribute("selectedTournament");
  Boolean locked = (Boolean) request.getAttribute("locked");
  List<TeamStanding> enrolled = (List<TeamStanding>) request.getAttribute("enrolled");
  List<Team> available = (List<Team>) request.getAttribute("available");

  String msg = (String) request.getAttribute("msg");
  String err = (String) request.getAttribute("err");
%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Enroll Teams</title>
<link rel="stylesheet" href="resources/css/admin_enroll.css">
<style>
.locked {color:#e74c3c;font-weight:600;}
</style>
</head>
<body>
<div class="top">
  <div class="brand">🏏 Enroll Teams</div>
  <div class="nav">
    <a href="adminmain.jsp">Home</a> | <a href="logout">Logout</a>
  </div>
</div>

<div class="wrap">
  <% if (msg != null) { %><div class="toast ok"><%= msg %></div><% } %>
  <% if (err != null) { %><div class="toast err"><%= err %></div><% } %>

  <h2>Select Tournament</h2>
  <div class="list">
  <% if (tournaments == null || tournaments.isEmpty()) { %>
      <p>No tournaments found.</p>
  <% } else {
      for (Tournament t : tournaments) {
        boolean isLocked = lockedIds != null && lockedIds.contains(t.getId());
  %>
        <div class="row">
          <b><%= t.getName() %></b> (<%= t.getFormat() %>)
          <% if (isLocked) { %><span class="locked">[Fixtures Generated]</span><% } %>
          <a href="enroll?tid=<%= t.getId() %>" class="pill">Open</a>
        </div>
  <%  } } %>
  </div>

  <% if (selected != null) { %>
    <hr><h2>Manage Teams — <%= selected.getName() %></h2>
    <% if (locked != null && locked) { %>
      <div class="banner lock">🔒 Fixtures generated — changes disabled.</div>
    <% } %>

    <div class="cols">
      <div class="card">
        <h3>Available Teams</h3>
        <table>
          <tr><th>ID</th><th>Name</th><th>City</th><th></th></tr>
          <% if (available==null || available.isEmpty()) { %>
            <tr><td colspan="4" class="empty">No more teams to enroll.</td></tr>
          <% } else { for (Team t : available) { %>
            <tr>
              <td><%= t.getId() %></td>
              <td><%= t.getName() %></td>
              <td><%= t.getCity() %></td>
              <td>
                <% if (locked) { %>
                  <span class="pill disabled">Add</span>
                <% } else { %>
                  <a class="pill add" href="enroll?action=add&tid=<%= selected.getId() %>&teamId=<%= t.getId() %>">Add</a>
                <% } %>
              </td>
            </tr>
          <% } } %>
        </table>
      </div>

      <div class="card">
        <h3>Enrolled Teams</h3>
        <table>
          <tr><th>ID</th><th>Name</th><th>City</th><th>Points</th><th>Played</th><th></th></tr>
          <% if (enrolled==null || enrolled.isEmpty()) { %>
            <tr><td colspan="6" class="empty">No teams enrolled yet.</td></tr>
          <% } else { for (TeamStanding ts : enrolled) { %>
            <tr>
              <td><%= ts.getTeamId() %></td>
              <td><%= ts.getName() %></td>
              <td><%= ts.getCity() %></td>
              <td><%= ts.getPoints() %></td>
              <td><%= ts.getPlayed() %></td>
              <td>
                <% if (locked) { %>
                  <span class="pill disabled">Remove</span>
                <% } else { %>
                  <a class="pill del" href="enroll?action=remove&tid=<%= selected.getId() %>&teamId=<%= ts.getTeamId() %>"
                    onclick="return confirm('Remove this team?');">Remove</a>
                <% } %>
              </td>
            </tr>
          <% } } %>
        </table>
      </div>
    </div>
  <% } %>
</div>
</body>
</html>
