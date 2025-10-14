<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*, com.ctm.model.Match" %>
<%
  String role = (String) session.getAttribute("role");
  if (role == null || !"admin".equalsIgnoreCase(role)) { response.sendRedirect("index.jsp"); return; }
  response.setHeader("Cache-Control","no-cache,no-store,must-revalidate");
  response.setHeader("Pragma","no-cache");
  response.setDateHeader("Expires",0);

  List<Match> live = (List<Match>) request.getAttribute("liveMatches");
  String msg = request.getParameter("msg");
  String err = request.getParameter("err");
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Update Innings</title>
<link rel="stylesheet" href="resources/css/admin_tournaments.css">
<style>.btn{padding:10px 14px;border:none;border-radius:8px;cursor:pointer;font-weight:600;}</style>
<script>
function validateTotals(){
  const runs=+document.getElementById('total').value;
  const overs=+document.getElementById('overs').value;
  const wkts=+document.getElementById('wickets').value;
  if(wkts<10 && overs!=20){alert('If not all out, 20 overs must be completed.');return false;}
  return true;
}
</script>
</head>
<body>
<div class="top">
  <div class="brand">⚙️ Update Innings</div>
  <div><a href="adminmain.jsp" class="logout">Home</a><a href="logout" class="logout">Logout</a></div>
</div>

<div class="wrap">
  <% if(msg!=null){ %><div class="msg">✅ <%= msg.replace("+"," ") %></div><% } %>
  <% if(err!=null){ %><div class="msg">❌ <%= err.replace("+"," ") %></div><% } %>

  <h2>Live Matches</h2>
  <% if(live==null||live.isEmpty()){ %>
    <p>No live matches.</p>
  <% } else { %>
    <form method="post" action="updateinnings">
      <label>Select Match:</label>
      <select name="matchId" required>
        <% for(Match m:live){ %>
          <option value="<%= m.getMatchId() %>">
            <%= m.getTeam1Name() %> vs <%= m.getTeam2Name() %>
          </option>
        <% } %>
      </select>
      <button class="btn" name="step" value="choose">Update Scores</button>
    </form>
  <% } %>

  <% if(request.getAttribute("selectedMatchId")!=null){ %>
    <div class="panel">
      <h3>Enter Innings Totals</h3>
      <form method="post" action="updateinnings" onsubmit="return validateTotals()">
        <input type="hidden" name="matchId" value="<%= request.getAttribute("selectedMatchId") %>">
        <label><input type="radio" name="step" value="saveFirst" checked> First Innings</label>
        <label><input type="radio" name="step" value="saveSecond"> Second Innings</label><br><br>
        <input id="total" name="total" type="number" placeholder="Total Runs" min="0" required>
        <input id="overs" name="overs" type="number" placeholder="Overs (0–20)" min="0" max="20" required>
        <input id="wickets" name="wickets" type="number" placeholder="Wickets (0–10)" min="0" max="10" required>
        <br><br><button class="btn">Save</button>
      </form>
    </div>
  <% } %>
</div>
</body>
</html>
