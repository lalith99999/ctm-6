<%@ page contentType="text/html; charset=UTF-8" %>
<%
  // Session guard
  String username = (String) session.getAttribute("username");
  String role = (String) session.getAttribute("role");
  if (username == null || role == null || !"viewer".equalsIgnoreCase(role)) {
      response.sendRedirect("index.jsp"); return;
  }
  // Disable caching
  response.setHeader("Cache-Control","no-cache, no-store, must-revalidate");
  response.setHeader("Pragma","no-cache");
  response.setDateHeader("Expires",0);
%>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>Viewer Dashboard</title>
  <link rel="stylesheet" href="resources/css/usermain.css">
</head>
<body>
  <div class="top">
    <div class="brand"> Hello <b><%= username %></b></div>
    <a class="logout" href="logout">Logout</a>
  </div>

  <div class="wrap">
    <h2>Welcome to Cricket Viewer Portal</h2>

</body>
</html>
