package com.ctm.servlet;

import com.ctm.daoimpl.TournamentDaoImpl;
import com.ctm.model.TeamStanding;
import com.ctm.model.Tournament;
import com.ctm.util.DaoUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

@WebServlet("/tournamentdetails")
public class TournamentDetailsServlet extends HttpServlet {

  private final TournamentDaoImpl tdao = new TournamentDaoImpl();

  // SQLs used for read-only views
  private static final String SQL_MATCHES =
      "SELECT m.match_id, ta.name a_name, tb.name b_name, m.status, m.datetime, m.venue, " +
      "       m.a_runs, m.a_wkts, m.a_overs, m.b_runs, m.b_wkts, m.b_overs " +
      "FROM matches m " +
      "JOIN teams ta ON ta.team_id = m.team_a_id " +
      "JOIN teams tb ON tb.team_id = m.team_b_id " +
      "WHERE m.tournament_id=? ORDER BY m.match_id";

  private static final String SQL_PLAYERS_BY_TEAM =
      "SELECT p.jersey_no, p.name, " +
      "       NVL(SUM(CASE WHEN m.tournament_id=? THEN pp.runs END),0) runs, " +
      "       NVL(SUM(CASE WHEN m.tournament_id=? THEN pp.wickets END),0) wickets, " +
      "       NVL(COUNT(DISTINCT CASE WHEN m.tournament_id=? THEN pp.match_id END),0) matches " +
      "FROM team_players tp " +
      "JOIN players p ON p.jersey_no=tp.jersey_no " +
      "LEFT JOIN player_performance pp ON pp.jersey_no=p.jersey_no " +
      "LEFT JOIN matches m ON m.match_id=pp.match_id " +
      "WHERE tp.team_id=? " +
      "GROUP BY p.jersey_no, p.name " +
      "ORDER BY p.name";

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    // session guard
    HttpSession s = req.getSession(false);
    if (s == null || !"viewer".equalsIgnoreCase(String.valueOf(s.getAttribute("role")))) {
      resp.sendRedirect("index.jsp"); return;
    }

    long tid;
    try { tid = Long.parseLong(req.getParameter("tid")); }
    catch (Exception e) { resp.sendRedirect("tournaments"); return; }

    // tournament + standings via DAO
    Optional<Tournament> opt = tdao.findTournament(tid);
    if (opt.isEmpty()) { resp.sendRedirect("tournaments"); return; }
    Tournament tournament = opt.get();
    List<TeamStanding> standings = tdao.listTeamsInTournament(tid);

    // matches + team->players via helper methods
    List<Map<String,Object>> matches = loadMatches(tid);
    Map<String,List<Map<String,Object>>> teamPlayers = loadTeamPlayersByTournament(tid, standings);

    // to JSP
    req.setAttribute("tournament", tournament);
    req.setAttribute("standings", standings);
    req.setAttribute("matches", matches);
    req.setAttribute("teamPlayers", teamPlayers);
    req.getRequestDispatcher("tournament_details.jsp").forward(req, resp);
  }

  // ---- helpers ----
  private List<Map<String,Object>> loadMatches(long tid) {
    List<Map<String,Object>> list = new ArrayList<>();
    try (PreparedStatement ps = DaoUtil.getMyPreparedStatement(SQL_MATCHES)) {
      ps.setLong(1, tid);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          Map<String,Object> m = new HashMap<>();
          m.put("id", rs.getLong("match_id"));
          m.put("aName", rs.getString("a_name"));
          m.put("bName", rs.getString("b_name"));
          m.put("status", rs.getString("status"));
          m.put("datetime", rs.getString("datetime"));
          m.put("venue", rs.getString("venue"));
          m.put("aRuns", rs.getObject("a_runs"));   m.put("aWkts", rs.getObject("a_wkts"));   m.put("aOvers", rs.getObject("a_overs"));
          m.put("bRuns", rs.getObject("b_runs"));   m.put("bWkts", rs.getObject("b_wkts"));   m.put("bOvers", rs.getObject("b_overs"));
          list.add(m);
        }
      }
    } catch (Exception e) { e.printStackTrace(); }
    return list;
  }

  private Map<String,List<Map<String,Object>>> loadTeamPlayersByTournament(long tid, List<TeamStanding> standings) {
    Map<String,List<Map<String,Object>>> map = new LinkedHashMap<>();
    for (TeamStanding ts : standings) {
      List<Map<String,Object>> players = new ArrayList<>();
      try (PreparedStatement ps = DaoUtil.getMyPreparedStatement(SQL_PLAYERS_BY_TEAM)) {
        ps.setLong(1, tid); ps.setLong(2, tid); ps.setLong(3, tid); ps.setLong(4, ts.getTeamId());
        try (ResultSet rs = ps.executeQuery()) {
          while (rs.next()) {
            Map<String,Object> p = new HashMap<>();
            p.put("jerseyNo", rs.getLong("jersey_no"));
            p.put("name", rs.getString("name"));
            p.put("runs", rs.getInt("runs"));
            p.put("wickets", rs.getInt("wickets"));
            p.put("matches", rs.getInt("matches"));
            players.add(p);
          }
        }
      } catch (Exception e) { e.printStackTrace(); }
      map.put(ts.getName(), players);
    }
    return map;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    doGet(req, resp);
  }
}
