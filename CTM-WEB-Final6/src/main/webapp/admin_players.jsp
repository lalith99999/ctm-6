<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*, com.ctm.model.Player, com.ctm.model.PlayerType, com.ctm.model.Team" %>
<%
  String role = (String) session.getAttribute("role");
  if (role == null || !"admin".equalsIgnoreCase(role)) { response.sendRedirect("index.jsp"); return; }
  response.setHeader("Cache-Control","no-cache,no-store,must-revalidate");
  response.setHeader("Pragma","no-cache");
  response.setDateHeader("Expires",0);

  Team team = (Team) request.getAttribute("team");
  List<Player> players = (List<Player>) request.getAttribute("players");
  String msg = request.getParameter("msg");
  String err = request.getParameter("err");
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Manage Players - <%= team.getName() %></title>
<link rel="stylesheet" href="resources/css/admin_players.css">
<script>
function validateForm(form){
  const name = form.name.value.trim();
  const jersey = form.jerseyNo.value.trim();
  if(!/^[A-Za-z\s]+$/.test(name)){ alert("Name must contain only alphabets."); return false; }
  if(!/^[0-9]+$/.test(jersey) || jersey < 1 || jersey > 999){ alert("Invalid jersey number."); return false; }
  return true;
}
if(window.history.replaceState){ window.history.replaceState(null,null,window.location.href); }
</script>
</head>
<body>
<div class="top">
  <div class="brand">👕 Manage Players - <%= team.getName() %> (<%= team.getCity() %>)</div>
  <div>
    <a href="admteams" class="link">Back to Teams</a>
    <a href="logout" class="link">Logout</a>
  </div>
</div>

<div class="wrap">
  <% if (msg!=null) { %><div class="msg">✅ <%= msg.replace("+"," ") %></div><% } %>
  <% if (err!=null) { %><div class="msg" style="color:#c0392b;">⚠️ <%= err.replace("+"," ") %></div><% } %>

  <h2>Add Player</h2>
  <form method="post" action="admplayers" onsubmit="return validateForm(this)">
    <input type="hidden" name="action" value="create">
    <input type="hidden" name="teamId" value="<%= team.getId() %>">
    <input name="jerseyNo" placeholder="Jersey No (1-999)" required>
    <input name="name" placeholder="Player Name" required>
    <select name="type" required>
      <% for (PlayerType t : PlayerType.values()) { %>
        <option value="<%= t.name() %>"><%= t.name().replace('_',' ') %></option>
      <% } %>
    </select>
    <button class="btn btn-primary">Add</button>
  </form>

  <h2 style="margin-top:22px;">Team Players</h2>
  <table>
    <tr><th>#</th><th>Name</th><th>Role</th><th>Actions</th></tr>
    <% if (players==null || players.isEmpty()) { %>
      <tr><td colspan="4" style="text-align:center;">No players yet.</td></tr>
    <% } else { for (Player p : players) { %>
      <tr>
        <td><%= p.getJerseyNumber() %></td>
        <td><%= p.getName() %></td>
        <td><%= p.getType() %></td>
        <td>
          <form method="post" action="admplayers" onsubmit="return validateForm(this)">
            <input type="hidden" name="action" value="update">
            <input type="hidden" name="teamId" value="<%= team.getId() %>">
            <input type="hidden" name="jerseyNo" value="<%= p.getJerseyNumber() %>">
            <input name="name" value="<%= p.getName() %>" required>
            <select name="type" required>
              <% for (PlayerType t : PlayerType.values()) { %>
                <option value="<%= t.name() %>" <%= t==p.getType()?"selected":"" %>><%= t.name().replace('_',' ') %></option>
              <% } %>
            </select>
            <button class="btn btn-primary">Save</button>
          </form>
          <form method="post" action="admplayers"
                onsubmit="return confirm('Delete this player?');" style="margin-top:5px;">
            <input type="hidden" name="action" value="delete">
            <input type="hidden" name="teamId" value="<%= team.getId() %>">
            <input type="hidden" name="jerseyNo" value="<%= p.getJerseyNumber() %>">
            <button class="btn btn-danger">Delete</button>
          </form>
        </td>
      </tr>
    <% } } %>
  </table>
</div>
</body>
</html>
