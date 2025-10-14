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
  List<Tournament> list = (List<Tournament>) request.getAttribute("tournaments");
%>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>All Tournaments</title>
  <link rel="stylesheet" href="resources/css/viewer_tournament.css">
</head>
<body>
  <div class="top">
    <div class="brand">🏏 All Tournaments</div>
    <a class="logout" href="logout">Logout</a>
  </div>

  <div class="wrap">
    <a class="back" href="usermain.jsp">← Back</a>
    <h2>Available Tournaments</h2>
    <%
      if (list == null || list.isEmpty()) {
    %><p>No tournaments found.</p><%
      } else {
    %>
    <div class="grid">
      <% for (Tournament t : list) { %>
<a class="card" href="tournamentdetails?tid=<%= t.getId() %>">
          <div style="font-size:18px;font-weight:800"><%= t.getName() %></div>
          <div style="margin-top:6px;opacity:.85">Format: <%= t.getFormat() %></div>
        </a>
      <% } %>
    </div>
    <% } %>
</div>


</body>
</html>
