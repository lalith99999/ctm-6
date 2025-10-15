package com.ctm.servlet;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.ctm.dao.MatchDao;
import com.ctm.daoimpl.MatchDaoImpl;
import com.ctm.daoimpl.TournamentDaoImpl;
import com.ctm.model.Match;
import com.ctm.model.Tournament;

@WebServlet("/updateinnings")
public class UpdateInningsServlet extends HttpServlet {
    private final MatchDao matchDao = new MatchDaoImpl();
    private final TournamentDaoImpl tournamentDao = new TournamentDaoImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession s = req.getSession(false);
        if (s == null || !"admin".equalsIgnoreCase(String.valueOf(s.getAttribute("role")))) {
            resp.sendRedirect("index.jsp");
            return;
        }

        resp.setHeader("Cache-Control", "no-cache,no-store,must-revalidate");
        resp.setHeader("Pragma", "no-cache");
        resp.setDateHeader("Expires", 0);

        if (req.getParameter("msg") != null) req.setAttribute("msg", req.getParameter("msg"));
        if (req.getParameter("err") != null) req.setAttribute("err", req.getParameter("err"));

        String action = req.getParameter("action");
        if (action == null) action = "tournaments";

        switch (action) {
            case "matches":
                long tid = parseLong(req.getParameter("tid"));
                Tournament tour = tournamentDao.findTournament(tid).orElse(null);
                req.setAttribute("mode", "matches");
                req.setAttribute("tournament", tour);
                req.setAttribute("liveMatches", matchDao.getLiveMatchesByTournament(tid));
                break;
            case "form":
                long matchId = parseLong(req.getParameter("matchId"));
                Optional<Match> opt = matchDao.findMatch(matchId);
                if (!opt.isPresent()) {
                    resp.sendRedirect("updateinnings?err=" + URLEncoder.encode("Match not found", StandardCharsets.UTF_8));
                    return;
                }
                Match match = opt.get();
                Tournament tournament = tournamentDao.findTournament(match.getTournamentId()).orElse(null);

                Long firstTeam = match.getFirstInningsTeamId();
                if (firstTeam == null && match.getTossWinnerTeamId() != null && match.getTossDecision() != null) {
                    firstTeam = match.getTossDecision() == com.ctm.model.TossDecision.BAT
                            ? match.getTossWinnerTeamId()
                            : (match.getTossWinnerTeamId() == match.getTeam1Id() ? match.getTeam2Id() : match.getTeam1Id());
                }

                req.setAttribute("mode", "form");
                req.setAttribute("match", match);
                req.setAttribute("tournament", tournament);
                req.setAttribute("firstTeamId", firstTeam);
                req.setAttribute("target", computeTarget(match, firstTeam));
                break;
            default:
                req.setAttribute("mode", "tournaments");
                req.setAttribute("tournaments", matchDao.liveTournamentCounts());
                break;
        }

        req.getRequestDispatcher("WEB-INF/admin_update_innings.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession s = req.getSession(false);
        if (s == null || !"admin".equalsIgnoreCase(String.valueOf(s.getAttribute("role")))) {
            resp.sendRedirect("index.jsp");
            return;
        }

        resp.setHeader("Cache-Control", "no-cache,no-store,must-revalidate");
        resp.setHeader("Pragma", "no-cache");
        resp.setDateHeader("Expires", 0);

        String phase = req.getParameter("phase");
        long matchId = parseLong(req.getParameter("matchId"));
        long tid = parseLong(req.getParameter("tid"));

        if ("first".equalsIgnoreCase(phase)) {
            long battingTeamId = parseLong(req.getParameter("battingTeamId"));
            int runs = parseInt(req.getParameter("runs"));
            int wickets = parseInt(req.getParameter("wickets"));
            int extras = parseInt(req.getParameter("extras"));
            double overs = parseOvers(req.getParameter("overs"));

            if (runs < 0 || wickets < 0 || overs < 0 || extras < 0) {
                redirectWithError(resp, matchId, "Invalid first innings inputs");
                return;
            }

            boolean ok = matchDao.lockFirstInnings(matchId, battingTeamId, runs, wickets, overs, extras);
            if (ok) {
                redirectWithMessage(resp, matchId, "First innings locked");
            } else {
                redirectWithError(resp, matchId, "Unable to lock first innings");
            }
            return;
        }

        if ("second".equalsIgnoreCase(phase)) {
            int runs = parseInt(req.getParameter("runs"));
            int wickets = parseInt(req.getParameter("wickets"));
            int extras = parseInt(req.getParameter("extras"));
            double overs = parseOvers(req.getParameter("overs"));

            if (runs < 0 || wickets < 0 || overs < 0 || extras < 0) {
                redirectWithError(resp, matchId, "Invalid second innings inputs");
                return;
            }

            boolean ok = matchDao.lockSecondInnings(matchId, runs, wickets, overs, extras);
            if (ok) {
                String encoded = URLEncoder.encode("Match completed", StandardCharsets.UTF_8);
                resp.sendRedirect("updateinnings?action=matches&tid=" + tid + "&msg=" + encoded);
            } else {
                redirectWithError(resp, matchId, "Unable to lock second innings");
            }
            return;
        }

        resp.sendRedirect("updateinnings?err=" + URLEncoder.encode("Unknown action", StandardCharsets.UTF_8));
    }

    private void redirectWithMessage(HttpServletResponse resp, long matchId, String message) throws IOException {
        resp.sendRedirect("updateinnings?action=form&matchId=" + matchId + "&msg=" + URLEncoder.encode(message, StandardCharsets.UTF_8));
    }

    private void redirectWithError(HttpServletResponse resp, long matchId, String message) throws IOException {
        resp.sendRedirect("updateinnings?action=form&matchId=" + matchId + "&err=" + URLEncoder.encode(message, StandardCharsets.UTF_8));
    }

    private long parseLong(String v) {
        try { return Long.parseLong(v); } catch (Exception e) { return 0L; }
    }

    private int parseInt(String v) {
        try { return Integer.parseInt(v); } catch (Exception e) { return -1; }
    }

    private double parseOvers(String value) {
        if (value == null || value.trim().isEmpty()) return -1d;
        String txt = value.trim();
        try {
            if (!txt.contains(".")) {
                int overs = Integer.parseInt(txt);
                return overs;
            }
            String[] parts = txt.split("\\.");
            int overs = Integer.parseInt(parts[0]);
            int balls = Integer.parseInt(parts[1]);
            if (balls < 0 || balls > 5) return -1d;
            return overs + (balls / 10.0);
        } catch (NumberFormatException e) {
            return -1d;
        }
    }

    private Integer computeTarget(Match match, Long firstTeamId) {
        if (firstTeamId == null) return null;
        int runs = (firstTeamId == match.getTeam1Id()) ? match.getARuns() : match.getBRuns();
        if (runs <= 0) return null;
        return runs + 1;
    }
}
