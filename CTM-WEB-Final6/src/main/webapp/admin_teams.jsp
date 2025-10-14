<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*, com.ctm.model.Team, com.ctm.model.Cities" %>
<%
  String role = (String) session.getAttribute("role");
  if (role == null || !"admin".equalsIgnoreCase(role)) { response.sendRedirect("index.jsp"); return; }
  response.setHeader("Cache-Control","no-cache,no-store,must-revalidate");
  response.setHeader("Pragma","no-cache");
  response.setDateHeader("Expires",0);

  List<Team> teams = (List<Team>) request.getAttribute("teams");
  String msg = request.getParameter("msg");
  String err = request.getParameter("err");
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Manage Teams</title>
<link rel="stylesheet" href="resources/css/admin_teams.css">
<script>
function validateName(form){
  const name=form.name.value.trim();
  if(!/^[A-Za-z\s]+$/.test(name)){
    alert("Team name must contain only alphabets.");
    return false;
  }
  return true;
}

// Prevent back-resubmission
if (window.history.replaceState) {
  window.history.replaceState(null,null,window.location.href);
}
</script>
</head>
<body>
<div class="top">
  <div class="brand">⚙️ Manage Teams</div>
  <div>
    <a href="adminmain.jsp" class="link">Home</a>
    <a href="logout" class="link">Logout</a>
  </div>
</div>

<div class="wrap">
  <% if (msg!=null) { %><div class="msg">✅ <%= msg.replace("+"," ") %></div><% } %>
  <% if (err!=null) { %><div class="msg" style="color:#c0392b;">⚠️ <%= err.replace("+"," ") %></div><% } %>

  <h2>Create Team</h2>
  <form method="post" action="admteams" onsubmit="return validateName(this)">
    <input type="hidden" name="action" value="create">
    <input name="name" placeholder="Team Name" required>
    <select name="city" required>
      <option value="">--Select City--</option>
      <% for (Cities c : Cities.values()) { %>
        <option value="<%= c.name() %>"><%= c.name().replace('_',' ') %></option>
      <% } %>
    </select>
    <button class="btn btn-primary">Create</button>
  </form>

  <h2 style="margin-top:22px;">Existing Teams</h2>
  <table>
    <tr><th>ID</th><th>Name</th><th>City</th><th>Actions</th></tr>
    <% if (teams==null || teams.isEmpty()) { %>
      <tr><td colspan="4" style="text-align:center;">No teams added yet.</td></tr>
    <% } else { for (Team t : teams) { %>
      <tr>
        <td><%= t.getId() %></td>
        <td><%= t.getName() %></td>
        <td><%= t.getCity() %></td>
        <td>
          <form method="post" action="admteams" onsubmit="return validateName(this)">
            <input type="hidden" name="action" value="update">
            <input type="hidden" name="id" value="<%= t.getId() %>">
            <input name="name" value="<%= t.getName() %>" required>
            <select name="city" required>
              <% for (Cities c : Cities.values()) { %>
                <option value="<%= c.name() %>" <%= c.name().equalsIgnoreCase(t.getCity())?"selected":"" %>>
                  <%= c.name().replace('_',' ') %>
                </option>
              <% } %>
            </select>
            <button class="btn btn-primary">Save</button>
          </form>
          <form method="post" action="admteams"
                onsubmit="return confirm('Delete this team?');" style="margin-top:5px;">
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
