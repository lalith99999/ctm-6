<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*, com.ctm.model.Match, com.ctm.model.Player, com.ctm.model.Tournament" %>
<%
  String role = (String) session.getAttribute("role");
  if (role == null || !"admin".equalsIgnoreCase(role)) { response.sendRedirect("index.jsp"); return; }
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
  <% if (msg != null && !msg.isEmpty()) { %><div class="msg">✅ <%= msg.replace("+"," ") %></div><% } %>
  <% if (err != null && !err.isEmpty()) { %><div class="msg">❌ <%= err.replace("+"," ") %></div><% } %>

<% if ("tournaments".equals(mode)) { %>
  <h2>Select a Tournament (LIVE matches only)</h2>
  <%
    List<Map<String,Object>> tournaments = (List<Map<String,Object>>) request.getAttribute("tournaments");
    if (tournaments == null || tournaments.isEmpty()) {
  %>
    <p>No tournaments with LIVE matches right now.</p>
  <% } else {
       for (Map<String,Object> t : tournaments) {
  %>
    <div class="row">
      <div><b><%= t.get("name") %></b> <span class="note">(ID: <%= t.get("id") %>)</span></div>
      <div><a class="pill" href="updateinnings?action=matches&tid=<%= t.get("id") %>">Open</a></div>
    </div>
  <% }} %>

<% } else if ("matches".equals(mode)) { %>
  <%
    com.ctm.model.Tournament tour = (com.ctm.model.Tournament) request.getAttribute("tournament");
    List<Map<String,Object>> liveMatches = (List<Map<String,Object>>) request.getAttribute("liveMatches");
  %>
  <h2>LIVE Matches — <%= tour.getName() %></h2>
  <table>
    <tr><th>ID</th><th>Match</th><th>Date</th><th>Venue</th><th>Action</th></tr>
    <% if (liveMatches == null || liveMatches.isEmpty()) { %>
      <tr><td colspan="5">No LIVE matches in this tournament.</td></tr>
    <% } else { for (Map<String,Object> m : liveMatches) { %>
      <tr>
        <td><%= m.get("id") %></td>
        <td><%= m.get("aName") %> vs <%= m.get("bName") %></td>
        <td><%= m.get("datetime") %></td>
        <td><%= m.get("venue") %></td>
        <td><a class="pill" href="updateinnings?action=form&matchId=<%= m.get("id") %>">Update Innings</a></td>
      </tr>
    <% } } %>
  </table>
  <div style="margin-top:10px;"><a class="link" href="updateinnings">← Back to tournaments</a></div>

<% } else if ("form".equals(mode)) { %>
  <%
    Match match = (Match) request.getAttribute("match");
    List<Player> teamAPlayers = (List<Player>) request.getAttribute("teamAPlayers");
    List<Player> teamBPlayers = (List<Player>) request.getAttribute("teamBPlayers");
    long battingTeamId = (Long) request.getAttribute("battingTeamId");
    long bowlingTeamId = (Long) request.getAttribute("bowlingTeamId");

    Map<String,Object> draft = (Map<String,Object>) request.getAttribute("draft");
    Integer dTotalRuns = draft!=null ? (Integer) draft.get("totalRuns") : null;
    Integer dTotalWkts = draft!=null ? (Integer) draft.get("totalWkts") : null;
    Double dOvers     = draft!=null ? (Double)  draft.get("overs")     : null;
  %>

  <h2>Match #<%= match.getId() %> — Update Innings</h2>
  <div class="grid">
    <div class="card">
      <h3>Batting Team</h3>
      <form action="updateinnings" method="get">
        <input type="hidden" name="action" value="form">
        <input type="hidden" name="matchId" value="<%= match.getId() %>">
        <label>
          <input type="radio" name="battingTeamId" value="<%= match.getTeamAId() %>" <%= (battingTeamId==match.getTeamAId()?"checked":"") %> >
          Team A
        </label>
        &nbsp;&nbsp;
        <label>
          <input type="radio" name="battingTeamId" value="<%= match.getTeamBId() %>" <%= (battingTeamId==match.getTeamBId()?"checked":"") %> >
          Team B
        </label>
        &nbsp;&nbsp;
        <button class="btn" type="submit">Switch</button>
      </form>
      <p class="note">Choose the batting side, then enter totals & player-wise details below.</p>
    </div>

    <div class="card">
      <h3>Team Totals</h3>
      <form action="updateinnings" method="get" id="saveForm">
        <input type="hidden" name="action" value="save">
        <input type="hidden" name="matchId" value="<%= match.getId() %>">
        <input type="hidden" name="battingTeamId" value="<%= battingTeamId %>">
        <div class="row" style="gap:8px;justify-content:flex-start">
          <div>Runs: <input type="number" name="totalRuns" min="0" value="<%= dTotalRuns!=null? dTotalRuns : 0 %>"></div>
          <div>Wickets: <input type="number" name="totalWkts" min="0" max="10" value="<%= dTotalWkts!=null? dTotalWkts : 0 %>"></div>
          <div>Overs: <input type="text" name="overs" value="<%= dOvers!=null? dOvers : 0 %>"></div>
        </div>

        <div class="grid" style="margin-top:12px">
          <div class="card">
            <h3>Batsmen (Batting Team)</h3>
            <table>
              <tr><th>#</th><th>Name</th><th>Runs</th><th>Balls</th><th>Out?</th></tr>
              <%
                List<Player> bats = (battingTeamId==match.getTeamAId()) ? teamAPlayers : teamBPlayers;
                for (Player p : bats) {
              %>
                <tr>
                  <td><%= p.getJerseyNumber() %><input type="hidden" name="bats_jersey" value="<%= p.getJerseyNumber() %>"></td>
                  <td style="text-align:left"><%= p.getName() %></td>
                  <td><input type="number" name="bats_runs" min="0" value="0"></td>
                  <td><input type="number" name="bats_balls" min="0" value="0"></td>
                  <td><input type="checkbox" name="bats_out"></td>
                </tr>
              <% } %>
            </table>
            <p class="note">Sum of batsman runs must equal team total.</p>
          </div>

          <div class="card">
            <h3>Bowlers (Bowling Team)</h3>
            <table>
              <tr><th>#</th><th>Name</th><th>Runs Conceded</th><th>Wickets</th></tr>
              <%
                List<Player> bowls = (bowlingTeamId==match.getTeamAId()) ? teamAPlayers : teamBPlayers;
                for (Player p : bowls) {
              %>
                <tr>
                  <td><%= p.getJerseyNumber() %><input type="hidden" name="bowl_jersey" value="<%= p.getJerseyNumber() %>"></td>
                  <td style="text-align:left"><%= p.getName() %></td>
                  <td><input type="number" name="bowl_runs" min="0" value="0"></td>
                  <td><input type="number" name="bowl_wkts" min="0" max="10" value="0"></td>
                </tr>
              <% } %>
            </table>
            <p class="note">Sum of bowlers’ wickets must equal team wickets.</p>
          </div>
        </div>

        <div style="margin-top:12px">
          <button class="btn btn-primary" type="submit">Save Innings</button>
          &nbsp; <a class="link" href="updateinnings?action=matches&tid=<%= match.getTournamentId() %>">Cancel</a>
        </div>
      </form>
    </div>
  </div>
<% } %>
</div>

</body>
</html>
