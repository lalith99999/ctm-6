<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*, com.ctm.model.*" %>
<%
  String username = (String) session.getAttribute("username");
  String role = (String) session.getAttribute("role");
  if (username == null || role == null || !"viewer".equalsIgnoreCase(role)) {
      response.sendRedirect("index.jsp"); return;
  }
  response.setHeader("Cache-Control","no-cache, no-store, must-revalidate");
  response.setHeader("Pragma","no-cache");
  response.setDateHeader("Expires",0);

  Tournament tournament = (Tournament) request.getAttribute("tournament");
  List<Map<String,Object>> todayMatches = (List<Map<String,Object>>) request.getAttribute("todayMatches");
  List<Map<String,Object>> scheduledMatches = (List<Map<String,Object>>) request.getAttribute("scheduledMatches");
  List<TeamStanding> points = (List<TeamStanding>) request.getAttribute("pointsTable");
  Map<String, List<String>> teamsPlayers = (Map<String, List<String>>) request.getAttribute("teamsPlayers");
%>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title><%= tournament.getName() %> — Tournament Details</title>
  <link rel="stylesheet" href="resources/css/tournament_details.css">
</head>
<body>
  <div class="top">
    <div class="brand">🏏 <%= tournament.getName() %> — <%= tournament.getFormat() %></div>
    <div>
      <a class="back" href="tournament_home.jsp">← All Tournaments</a>
      &nbsp;
      <a class="logout" href="logout">Logout</a>
    </div>
  </div>

  <div class="content">
    <!-- LEFT: Matches + Points -->
    <div>
      <div class="panel">
        <h2>Today’s Live / Finished Matches</h2>
        <% if (todayMatches == null || todayMatches.isEmpty()) { %>
          <p>No live or finished matches today.</p>
        <% } else {
             for (Map<String,Object> m : todayMatches) {
               String status = String.valueOf(m.get("status"));
               String aTeam = String.valueOf(m.get("teamA"));
               String bTeam = String.valueOf(m.get("teamB"));
               String scoreA = m.get("aRuns") + "/" + m.get("aWkts");
               String scoreB = m.get("bRuns") + "/" + m.get("bWkts");
        %>
          <div class="match">
            <div><b><%= aTeam %></b> vs <b><%= bTeam %></b> — <span class="status <%= status %>"><%= status %></span></div>
            <div>Score: <%= scoreA %>  |  <%= scoreB %></div>
            <div><%= m.get("venue") %>  —  <%= m.get("datetime") %></div>
          </div>
        <% } } %>
      </div>

      <div class="panel" style="margin-top:18px;">
        <h2>Scheduled Matches</h2>
        <% if (scheduledMatches == null || scheduledMatches.isEmpty()) { %>
          <p>No upcoming matches scheduled.</p>
        <% } else {
             for (Map<String,Object> m : scheduledMatches) { %>
          <div class="match">
            <div><b><%= m.get("teamA") %></b> vs <b><%= m.get("teamB") %></b></div>
            <div><%= m.get("datetime") %> — <%= m.get("venue") %></div>
          </div>
        <% } } %>
      </div>

      <div class="panel" style="margin-top:18px;">
        <h2>Points Table</h2>
        <% if (points == null || points.isEmpty()) { %>
          <p>No points data available.</p>
        <% } else { %>
          <table>
            <tr><th>Team</th><th>Played</th><th>Points</th><th>NRR</th></tr>
            <% for (TeamStanding s : points) { %>
              <tr>
                <td><%= s.getName() %></td>
                <td><%= s.getPlayed() %></td>
                <td><%= s.getPoints() %></td>
                <td><%= String.format("%.2f", s.getNrr()) %></td>
              </tr>
            <% } %>
          </table>
        <% } %>
      </div>
    </div>

    <!-- RIGHT: Teams & Players -->
    <div class="panel">
      <h2>Teams & Players</h2>
      <% if (teamsPlayers == null || teamsPlayers.isEmpty()) { %>
        <p>No team or player data available.</p>
      <% } else {
           for (Map.Entry<String, List<String>> e : teamsPlayers.entrySet()) { %>
        <div class="teamBox">
          <div style="font-weight:800; margin-bottom:6px;">🏏 <%= e.getKey() %></div>
          <div class="players">
            <ul style="margin:0; padding-left:16px;">
              <% for (String p : e.getValue()) { %>
                <li><%= p %></li>
              <% } %>
            </ul>
          </div>
        </div>
      <% } } %>
    </div>
  </div>

  <script>
    // Prevent cached navigation
    window.history.replaceState && window.history.replaceState(null, "", window.location.href);
    window.onpopstate = function(){ window.location.replace("tournament_home.jsp"); };
  </script>
</body>
</html>
