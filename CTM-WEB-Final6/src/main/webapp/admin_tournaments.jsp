<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*, com.ctm.model.Tournament" %>
<%
  String role = (String) session.getAttribute("role");
  if (role == null || !"admin".equalsIgnoreCase(role)) { response.sendRedirect("index.jsp"); return; }
  response.setHeader("Cache-Control","no-cache,no-store,must-revalidate");
  response.setHeader("Pragma","no-cache");
  response.setDateHeader("Expires",0);

  List<Tournament> tournaments = (List<Tournament>) request.getAttribute("tournaments");
  String msg = request.getParameter("msg");
  String err = request.getParameter("err");
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Manage Tournaments</title>
<link rel="stylesheet" href="resources/css/admin_tournaments.css">
<script>
function validateName(form){
  const name = form.name.value.trim();
  if(!/^[A-Za-z0-9\s]+$/.test(name)){
    alert("Tournament name must contain only alphabets or numbers.");
    return false;
  }
  return true;
}

// Prevent re-submission on back/refresh
if (window.history.replaceState) {
  window.history.replaceState(null, null, window.location.href);
}
</script>
</head>
<body>
<div class="top">
  <div class="brand">🏆 Manage Tournaments</div>
  <div><a href="adminmain.jsp" class="link">Home</a> &nbsp; <a href="logout" class="link">Logout</a></div>
</div>

<div class="wrap">
  <% if (msg != null) { %><div class="msg">✅ <%= msg.replace("+"," ") %></div><% } %>
  <% if (err != null) { %><div class="msg" style="color:#c0392b;">⚠️ <%= err.replace("+"," ") %></div><% } %>

  <h2>Create Tournament</h2>
  <form method="post" action="admtournaments" onsubmit="return validateName(this)">
    <input type="hidden" name="action" value="create">
    <input name="name" placeholder="Tournament Name" required>
    <button class="btn btn-primary">Create</button>
  </form>

  <h2 style="margin-top:22px;">Existing Tournaments</h2>
  <table>
    <tr><th>ID</th><th>Name</th><th>Format</th><th>Actions</th></tr>
    <% if (tournaments == null || tournaments.isEmpty()) { %>
      <tr><td colspan="4" style="text-align:center;">No tournaments yet.</td></tr>
    <% } else { for (Tournament t : tournaments) { %>
      <tr>
        <td><%= t.getId() %></td>
        <td><%= t.getName() %></td>
        <td><%= t.getFormat() %></td>
        <td>
          <form method="post" action="admtournaments" onsubmit="return validateName(this)">
            <input type="hidden" name="action" value="update">
            <input type="hidden" name="id" value="<%= t.getId() %>">
            <input name="name" value="<%= t.getName() %>" required>
            <button class="btn btn-primary">Save</button>
          </form>
          <form method="post" action="admtournaments"
                onsubmit="return confirm('Delete this tournament?');" style="margin-top:5px;">
            <input type="hidden" name="action" value="delete">
            <input type="hidden" name="id" value="<%= t.getId() %>">
            <button class="btn btn-danger">Delete</button>
          </form>
        </td>
      </tr>
    <% } } %>
  </table>
</div>
</body>
</html>
