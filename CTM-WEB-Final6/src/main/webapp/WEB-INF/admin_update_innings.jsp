<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*, com.ctm.model.Match, com.ctm.model.MatchStatus" %>
<%
  String role = (String) session.getAttribute("role");
  if (role == null || !"admin".equalsIgnoreCase(role)) { response.sendRedirect("/index.jsp"); return; }
  response.setHeader("Cache-Control","no-cache, no-store, must-revalidate");
  response.setHeader("Pragma","no-cache");
  response.setDateHeader("Expires",0);

  String mode = (String) request.getAttribute("mode");
  if (mode == null) mode = "tournaments";
  String msg = (String) request.getAttribute("msg");
  String err = (String) request.getAttribute("err");
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Update Innings</title>
<link rel="stylesheet" href="../resources/css/admin_update_innings_internal.css">
</head>
<body>
<div class="top">
  <div class="brand">🏏 Update Innings</div>
  <div><a class="link" href="adminmain.jsp">Home</a> &nbsp; <a class="link" href="logout">Logout</a></div>
</div>

<div class="wrap">
  <% if (msg != null) { %><div class="banner success"><%= msg %></div><% } %>
  <% if (err != null) { %><div class="banner error"><%= err %></div><% } %>

  <% if ("tournaments".equals(mode)) { %>
    <h2>Select a Tournament</h2>
    <p>Only tournaments with LIVE matches appear here.</p>
    <%
      List<Map<String,Object>> tournaments = (List<Map<String,Object>>) request.getAttribute("tournaments");
      if (tournaments == null || tournaments.isEmpty()) {
    %>
      <p class="empty">No live matches right now.</p>
    <%
      } else {
    %>
      <table>
        <tr><th>ID</th><th>Name</th><th>Live Matches</th><th>Action</th></tr>
        <% for (Map<String,Object> row : tournaments) { %>
          <tr>
            <td><%= row.get("id") %></td>
            <td><%= row.get("name") %></td>
            <td><span class="chip chip-ok"><%= row.get("count") %></span></td>
            <td><a class="primary" href="updateinnings?action=matches&tid=<%= row.get("id") %>">Open</a></td>
          </tr>
        <% } %>
      </table>
    <% } %>

  <% } else if ("matches".equals(mode)) { %>
    <h2>Live Matches</h2>
    <%
      List<Match> liveMatches = (List<Match>) request.getAttribute("liveMatches");
      com.ctm.model.Tournament tour = (com.ctm.model.Tournament) request.getAttribute("tournament");
      if (tour != null) {
    %>
      <p class="subtitle">Tournament: <b><%= tour.getName() %></b></p>
    <% }
      if (liveMatches == null || liveMatches.isEmpty()) {
    %>
      <p class="empty">No LIVE matches to update.</p>
      <div class="actions"><a class="link" href="updateinnings">← Back to tournaments</a></div>
    <% } else { %>
      <table>
        <tr><th>ID</th><th>Match</th><th>Date</th><th>Venue</th><th>Action</th></tr>
        <% for (Match m : liveMatches) { %>
          <tr>
            <td><%= m.getMatchId() %></td>
            <td><%= m.getTeam1Name() %> vs <%= m.getTeam2Name() %></td>
            <td><%= m.getDateTime() != null ? m.getDateTime().toLocalDate() : "-" %></td>
            <td><%= m.getVenue() %></td>
            <td><a class="primary" href="updateinnings?action=form&matchId=<%= m.getMatchId() %>">Update Innings</a></td>
          </tr>
        <% } %>
      </table>
      <div class="actions"><a class="link" href="updateinnings">← Back to tournaments</a></div>
    <% } %>

  <% } else if ("form".equals(mode)) {
       Match match = (Match) request.getAttribute("match");
       com.ctm.model.Tournament tour = (com.ctm.model.Tournament) request.getAttribute("tournament");
       Long firstTeamId = (Long) request.getAttribute("firstTeamId");
       Integer target = (Integer) request.getAttribute("target");
       boolean firstLocked = match.getFirstInningsTeamId() != null;
       boolean matchFinished = match.getStatus() == MatchStatus.FINISHED;
       String firstTeamName = firstTeamId == null ? "TBD" : (firstTeamId == match.getTeam1Id() ? match.getTeam1Name() : match.getTeam2Name());
       Long secondTeamId = null;
       if (firstTeamId != null) secondTeamId = (firstTeamId.equals(match.getTeam1Id()) ? match.getTeam2Id() : match.getTeam1Id());
       String secondTeamName = secondTeamId == null ? "TBD" : (secondTeamId.equals(match.getTeam1Id()) ? match.getTeam1Name() : match.getTeam2Name());
    %>
    <h2>Match #<%= match.getMatchId() %> — <%= match.getTeam1Name() %> vs <%= match.getTeam2Name() %></h2>
    <% if (tour != null) { %><p class="subtitle">Tournament: <b><%= tour.getName() %></b></p><% } %>
    <div class="grid">
      <div class="card">
        <h3>First Innings — <%= firstTeamName %></h3>
        <p class="note">Once locked, the first innings cannot be edited.</p>
        <p class="summary">Score: <b><%= firstTeamId != null && firstTeamId == match.getTeam1Id() ? match.getARuns() : match.getBRuns() %></b> / <b><%= firstTeamId != null && firstTeamId == match.getTeam1Id() ? match.getAWkts() : match.getBWkts() %></b> in <b><%= firstTeamId != null && firstTeamId == match.getTeam1Id() ? match.getAOvers() : match.getBOvers() %></b> overs</p>
        <form method="post" action="updateinnings" class="form" <%= firstLocked ? "data-disabled=1" : "" %>>
          <input type="hidden" name="phase" value="first">
          <input type="hidden" name="matchId" value="<%= match.getMatchId() %>">
          <input type="hidden" name="tid" value="<%= match.getTournamentId() %>">
          <input type="hidden" name="battingTeamId" value="<%= firstTeamId != null ? firstTeamId : match.getTeam1Id() %>">
          <label>Runs <input type="number" name="runs" min="0" value="<%= firstTeamId != null && firstTeamId == match.getTeam1Id() ? match.getARuns() : match.getBRuns() %>" <%= firstLocked ? "readonly" : "" %>></label>
          <label>Wickets <input type="number" name="wickets" min="0" max="10" value="<%= firstTeamId != null && firstTeamId == match.getTeam1Id() ? match.getAWkts() : match.getBWkts() %>" <%= firstLocked ? "readonly" : "" %>></label>
          <label>Overs <input type="text" name="overs" value="<%= firstTeamId != null && firstTeamId == match.getTeam1Id() ? match.getAOvers() : match.getBOvers() %>" <%= firstLocked ? "readonly" : "" %>></label>
          <label>Extras <input type="number" name="extras" min="0" value="<%= firstTeamId != null && firstTeamId == match.getTeam1Id() ? match.getAExtras() : match.getBExtras() %>" <%= firstLocked ? "readonly" : "" %>></label>
          <button class="primary" type="submit" <%= firstLocked ? "disabled" : "" %>>Lock First Innings</button>
        </form>
      </div>

      <div class="card">
        <h3>Second Innings — <%= secondTeamName %></h3>
        <% if (target != null) { %><p class="note">Target: <b><%= target %></b> runs</p><% } %>
        <p class="summary">Score: <b><%= secondTeamId != null && secondTeamId == match.getTeam1Id() ? match.getARuns() : match.getBRuns() %></b> / <b><%= secondTeamId != null && secondTeamId == match.getTeam1Id() ? match.getAWkts() : match.getBWkts() %></b> in <b><%= secondTeamId != null && secondTeamId == match.getTeam1Id() ? match.getAOvers() : match.getBOvers() %></b> overs</p>
        <form method="post" action="updateinnings" class="form" <%= (!firstLocked || matchFinished) ? "data-disabled=1" : "" %>>
          <input type="hidden" name="phase" value="second">
          <input type="hidden" name="matchId" value="<%= match.getMatchId() %>">
          <input type="hidden" name="tid" value="<%= match.getTournamentId() %>">
          <label>Runs <input type="number" name="runs" min="0" value="<%= secondTeamId != null && secondTeamId == match.getTeam1Id() ? match.getARuns() : match.getBRuns() %>" <%= (!firstLocked || matchFinished) ? "readonly" : "" %>></label>
          <label>Wickets <input type="number" name="wickets" min="0" max="10" value="<%= secondTeamId != null && secondTeamId == match.getTeam1Id() ? match.getAWkts() : match.getBWkts() %>" <%= (!firstLocked || matchFinished) ? "readonly" : "" %>></label>
          <label>Overs <input type="text" name="overs" value="<%= secondTeamId != null && secondTeamId == match.getTeam1Id() ? match.getAOvers() : match.getBOvers() %>" <%= (!firstLocked || matchFinished) ? "readonly" : "" %>></label>
          <label>Extras <input type="number" name="extras" min="0" value="<%= secondTeamId != null && secondTeamId == match.getTeam1Id() ? match.getAExtras() : match.getBExtras() %>" <%= (!firstLocked || matchFinished) ? "readonly" : "" %>></label>
          <button class="primary" type="submit" <%= (!firstLocked || matchFinished) ? "disabled" : "" %>>Finalize Match</button>
        </form>
      </div>
    </div>
    <div class="actions"><a class="link" href="updateinnings?action=matches&tid=<%= match.getTournamentId() %>">← Back to matches</a></div>
  <% } %>
</div>
</body>
</html>
