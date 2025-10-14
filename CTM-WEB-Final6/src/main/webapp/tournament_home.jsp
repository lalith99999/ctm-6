<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*, com.ctm.model.Tournament" %>
<%
  String username = (String) session.getAttribute("username");
  String role = (String) session.getAttribute("role");
  if (username == null || role == null || !"viewer".equalsIgnoreCase(role)) {
      response.sendRedirect("index.jsp"); return;
  }
  response.setHeader("Cache-Control","no-cache, no-store, must-revalidate");
  response.setHeader("Pragma","no-cache");
  response.setDateHeader("Expires",0);

  List<Tournament> tournaments = (List<Tournament>) request.getAttribute("tournaments");
%>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>All Tournaments</title>
  <link rel="stylesheet" href="resources/css/tournament_home.css">
</head>
<body>
  <div class="top">
    <div class="brand">🏏 All Tournaments</div>
    <div>
      <a class="logout" href="usermain.jsp">Home</a>
      &nbsp;
      <a class="logout" href="logout">Logout</a>
    </div>
  </div>

  <div class="wrap">
    <h2>Available Tournaments</h2>
    <% if (tournaments == null || tournaments.isEmpty()) { %>
      <p>No tournaments available.</p>
    <% } else { %>
      <div class="grid">
        <% for (Tournament t : tournaments) { %>
          <a class="card" href="tournamentdetails?tId=<%= t.getId() %>">
            <div style="font-size:18px;font-weight:700;"><%= t.getName() %></div>
            <div style="margin-top:6px;opacity:.85;">Format: <%= t.getFormat() %></div>
          </a>
        <% } %>
      </div>
    <% } %>
  </div>

  <script>
    // Prevent navigation caching
    window.history.replaceState && window.history.replaceState(null, "", window.location.href);
    window.onpopstate = function(){ window.history.pushState(null, "", window.location.href); };
  </script>
</body>
</html>
