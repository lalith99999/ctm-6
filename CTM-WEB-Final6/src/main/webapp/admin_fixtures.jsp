<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*, com.ctm.model.Match, com.ctm.model.Tournament" %>
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
<style>
  body {font-family: Poppins, sans-serif; background: #0b5345; color: #fff; margin:0;}
  .top {display:flex; justify-content:space-between; align-items:center; background:#09493c; padding:14px 20px;}
  .brand {font-weight:700;}
  .logout {color:#fff; text-decoration:none; margin-left:10px;}
  .wrap {max-width:900px; margin:30px auto; padding:20px;}
  .msg {padding:12px; border-radius:10px; margin-bottom:15px;}
  .ok {background:#1e8449; color:#d4efdf;}
  .err {background:#922b21; color:#f9ebea;}
  table {width:100%; border-collapse:collapse; margin-top:20px;}
  th, td {padding:10px; border-bottom:1px solid #145a32; text-align:left;}
  th {background:#117a65;}
  tr:hover {background:#0e6655;}
  .btn {padding:8px 16px; border:none; border-radius:8px; background:#f1c40f; color:#000; font-weight:600;}
</style>
</head>
<body>
<div class="top">
  <div class="brand">🏏 Generate Fixtures</div>
  <div><a href="adminmain.jsp" class="logout">Home</a><a href="logout" class="logout">Logout</a></div>
</div>

<div class="wrap">
  <% if (msg != null) { %><div class="msg ok"><%= msg %></div><% } %>
  <% if (err != null) { %><div class="msg err"><%= err %></div><% } %>

  <% if ("result".equals(mode)) {
       Tournament t = (Tournament) request.getAttribute("tournament");
       List<Match> matches = (List<Match>) request.getAttribute("matches");
  %>
    <h2><%= t.getName() %> Fixtures</h2>
    <% if (matches == null || matches.isEmpty()) { %>
      <p>No matches found.</p>
    <% } else { %>
      <table>
        <tr><th>#</th><th>Team A</th><th>Team B</th><th>Venue</th><th>Date</th></tr>
        <% int i = 1; for (Match m : matches) { %>
          <tr>
            <td><%= i++ %></td>
            <td><%= m.getTeam1Name() %></td>
            <td><%= m.getTeam2Name() %></td>
            <td><%= m.getVenue() %></td>
            <td><%= m.getDateTime() %></td>
          </tr>
        <% } %>
      </table>
    <% } %>

  <% } else if ("confirm".equals(mode)) {
       Tournament t = (Tournament) request.getAttribute("tournament");
       Integer enrolled = (Integer) request.getAttribute("enrolledCount");
       Boolean squadsOk = (Boolean) request.getAttribute("squadsOk");
  %>
    <h2><%= t.getName() %> — Fixture Generation</h2>
    <p>Teams Enrolled: <b><%= enrolled %></b> | 11/11 Squads: <b><%= (squadsOk ? "Yes" : "No") %></b></p>
    <form method="get" action="fixturesgen">
      <input type="hidden" name="tid" value="<%= t.getId() %>">
      <input type="hidden" name="action" value="generate">
      <label>Select Venue:</label>
      <select name="venue" required>
        <option value="">-- Choose Stadium --</option>
        <% for (com.ctm.model.Stadium s : com.ctm.model.Stadium.values()) { %>
          <option value="<%= s.getFullName() %>"><%= s.getFullName() %></option>
        <% } %>
      </select>
      <button class="btn" type="submit">Generate Fixtures</button>
    </form>

  <% } else { %>
    <h2>All Tournaments</h2>
    <p>Select one to generate fixtures.</p>
  <% } %>
</div>
</body>
</html>
