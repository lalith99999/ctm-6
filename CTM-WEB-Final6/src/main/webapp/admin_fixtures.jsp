<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.time.format.DateTimeFormatter, java.util.*, com.ctm.model.Match, com.ctm.model.Tournament" %>
<%
  String role = (String) session.getAttribute("role");
  if (role == null || !"admin".equalsIgnoreCase(role)) { response.sendRedirect("index.jsp"); return; }

  String mode = (String) request.getAttribute("mode");
  if (mode == null) mode = "list";
  String msg = (String) request.getAttribute("msg");
  String err = (String) request.getAttribute("err");
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Generate Fixtures</title>
<link rel="stylesheet" href="resources/css/admin_fixtures.css">
</head>
<body>
<div class="top">
  <div class="brand">🏏 Generate Fixtures</div>
  <div><a href="adminmain.jsp" class="link">Home</a><a href="logout" class="link">Logout</a></div>
</div>

<div class="wrap">
  <% if (msg != null) { %><div class="banner success"><%= msg %></div><% } %>
  <% if (err != null) { %><div class="banner error"><%= err %></div><% } %>

  <% if ("result".equals(mode)) {
       Tournament t = (Tournament) request.getAttribute("tournament");
       List<Match> matches = (List<Match>) request.getAttribute("matches");
       DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy");
  %>
    <h2><%= t.getName() %> Fixtures</h2>
    <% if (matches == null || matches.isEmpty()) { %>
      <p class="empty">No fixtures were generated.</p>
    <% } else { %>
      <table>
        <tr><th>#</th><th>Team A</th><th>Team B</th><th>Venue</th><th>Date</th></tr>
        <% int i = 1; for (Match m : matches) { %>
          <tr>
            <td><%= i++ %></td>
            <td><%= m.getTeam1Name() %></td>
            <td><%= m.getTeam2Name() %></td>
            <td><%= m.getVenue() %></td>
            <td><%= m.getDateTime() == null ? "-" : fmt.format(m.getDateTime()) %></td>
          </tr>
        <% } %>
      </table>
    <% } %>
    <div class="actions"><a class="link" href="fixturesgen">← Back to tournaments</a></div>

  <% } else if ("confirm".equals(mode)) {
       Tournament t = (Tournament) request.getAttribute("tournament");
       Integer enrolled = (Integer) request.getAttribute("enrolledCount");
       Boolean squadsOk = (Boolean) request.getAttribute("squadsOk");
       boolean fixturesExist = ((Integer) request.getAttribute("already")) == 1;
  %>
    <div class="card">
      <h2><%= t.getName() %> — Fixture Generation</h2>
      <p>Teams enrolled: <b><%= enrolled %></b></p>
      <p>All teams registered with 11 players: <span class="chip <%= squadsOk ? "chip-ok" : "chip-bad" %>"><%= squadsOk ? "Ready" : "Pending" %></span></p>
      <p>Existing fixtures: <span class="chip <%= fixturesExist ? "chip-warn" : "chip-ok" %>"><%= fixturesExist ? "Exists" : "None" %></span></p>
      <form method="get" action="fixturesgen" class="form-inline">
        <input type="hidden" name="tid" value="<%= t.getId() %>">
        <input type="hidden" name="action" value="generate">
        <label for="venue">Select Venue</label>
        <select id="venue" name="venue" required>
          <option value="">-- Choose Stadium --</option>
          <% for (com.ctm.model.Stadium s : com.ctm.model.Stadium.values()) { %>
            <option value="<%= s.getFullName() %>"><%= s.getFullName() %></option>
          <% } %>
        </select>
        <button class="primary" type="submit" <%= (!squadsOk || enrolled < 3 || fixturesExist) ? "disabled" : "" %>>Generate Fixtures</button>
      </form>
      <a class="link" href="fixturesgen">← Back to tournaments</a>
    </div>

  <% } else {
       List<Map<String,Object>> stats = (List<Map<String,Object>>) request.getAttribute("tournamentStats");
  %>
    <h2>All Tournaments</h2>
    <p>Select a tournament to review eligibility and generate fixtures.</p>
    <% if (stats == null || stats.isEmpty()) { %>
      <p class="empty">No tournaments available.</p>
    <% } else { %>
      <table>
        <tr><th>ID</th><th>Name</th><th>Teams Enrolled</th><th>11 Players?</th><th>Fixtures?</th><th>Action</th></tr>
        <% for (Map<String,Object> row : stats) {
             Tournament t = (Tournament) row.get("tournament");
             int enrolled = (Integer) row.get("enrolled");
             boolean squadsOk = (Boolean) row.get("squadsOk");
             boolean fixtures = (Boolean) row.get("fixtures");
        %>
          <tr>
            <td><%= t.getId() %></td>
            <td><%= t.getName() %></td>
            <td><%= enrolled %></td>
            <td><span class="chip <%= squadsOk ? "chip-ok" : "chip-bad" %>"><%= squadsOk ? "Ready" : "Pending" %></span></td>
            <td><span class="chip <%= fixtures ? "chip-warn" : "chip-ok" %>"><%= fixtures ? "Exists" : "None" %></span></td>
            <td><a class="primary" href="fixturesgen?tid=<%= t.getId() %>">Review</a></td>
          </tr>
        <% } %>
      </table>
    <% } %>
  <% } %>
</div>
</body>
</html>
