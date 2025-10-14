package com.ctm.servlet;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.ctm.daoimpl.ScheduleDaoRoundRobin;
import com.ctm.daoimpl.TeamDaoImpl;
import com.ctm.daoimpl.TournamentDaoImpl;
import com.ctm.model.Match;
import com.ctm.model.TeamStanding;
import com.ctm.model.Tournament;
import com.ctm.util.DaoUtil;

@WebServlet("/fixturesgen")
public class FixturesGenerateServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final TournamentDaoImpl tDao = new TournamentDaoImpl();
    private final TeamDaoImpl teamDao = new TeamDaoImpl();
    private final ScheduleDaoRoundRobin roundRobin = new ScheduleDaoRoundRobin();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession s = req.getSession(false);
        if (s == null || !"admin".equalsIgnoreCase((String) s.getAttribute("role"))) {
            resp.sendRedirect("index.jsp");
            return;
        }

        String action = req.getParameter("action");
        long tid = parseLong(req.getParameter("tid"));

        if (tid <= 0) {
            req.setAttribute("mode", "list");
            req.setAttribute("tournaments", tDao.listAllTournaments());
            req.getRequestDispatcher("admin_fixtures.jsp").forward(req, resp);
            return;
        }

        Tournament tour = tDao.findTournament(tid).orElse(null);
        if (tour == null) {
            resp.sendRedirect("fixturesgen");
            return;
        }

        boolean fixturesExist = tDao.fixturesExist(tid);

        // 🔹 Generate fixtures
        if ("generate".equalsIgnoreCase(action)) {
            List<TeamStanding> teams = tDao.listTeamsInTournament(tid);
            int count = (teams == null ? 0 : teams.size());
            boolean allHave11 = true;

            for (TeamStanding ts : teams) {
                int sz = teamDao.listPlayersOfTeam(ts.getTeamId()).size();
                if (sz != 11) {
                    allHave11 = false;
                    break;
                }
            }

            if (count < 3 || !allHave11 || fixturesExist) {
                req.setAttribute("err", "Fixture generation failed. Check conditions.");
                req.setAttribute("mode", "confirm");
                req.setAttribute("tournament", tour);
                req.setAttribute("enrolledCount", count);
                req.setAttribute("squadsOk", allHave11);
                req.getRequestDispatcher("admin_fixtures.jsp").forward(req, resp);
                return;
            }

            String venue = req.getParameter("venue");
            boolean ok = roundRobin.generateFixtures(tid, teams, venue);

            if (!ok) {
                req.setAttribute("err", "Fixture generation failed.");
                req.setAttribute("mode", "confirm");
                req.setAttribute("tournament", tour);
                req.setAttribute("enrolledCount", count);
                req.setAttribute("squadsOk", allHave11);
                req.getRequestDispatcher("admin_fixtures.jsp").forward(req, resp);
                return;
            }

            // ✅ Load generated matches with team names
            List<Match> created = new ArrayList<>();
            String sql = "SELECT m.match_id, " +
                         "t1.name AS team_a_name, t2.name AS team_b_name, " +
                         "m.venue, m.datetime " +
                         "FROM matches m " +
                         "JOIN teams t1 ON m.team_a_id = t1.team_id " +
                         "JOIN teams t2 ON m.team_b_id = t2.team_id " +
                         "WHERE m.tournament_id=? ORDER BY m.match_id";

            try (PreparedStatement ps = DaoUtil.getMyPreparedStatement(sql)) {
                ps.setLong(1, tid);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Match m = new Match();
                        m.setMatchId(rs.getLong("match_id"));
                        m.setTeam1Name(rs.getString("team_a_name"));
                        m.setTeam2Name(rs.getString("team_b_name"));
                        m.setVenue(rs.getString("venue"));
                        m.setDateTime(LocalDateTime.parse(rs.getString("datetime")));
                        created.add(m);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            req.setAttribute("tournament", tour);
            req.setAttribute("mode", "result");
            req.setAttribute("msg", "Fixtures generated successfully!");
            req.setAttribute("matches", created);
            req.getRequestDispatcher("admin_fixtures.jsp").forward(req, resp);
            return;
        }

        // 🔹 Confirm mode
        req.setAttribute("mode", "confirm");
        req.setAttribute("tournament", tour);
        req.setAttribute("enrolledCount", tDao.listTeamsInTournament(tid).size());
        req.setAttribute("already", fixturesExist ? 1 : 0);
        req.setAttribute("squadsOk", Boolean.TRUE);
        req.getRequestDispatcher("admin_fixtures.jsp").forward(req, resp);
    }

    private long parseLong(String s) {
        try {
            return Long.parseLong(s);
        } catch (Exception e) {
            return -1L;
        }
    }
}
