package com.ctm.servlet;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import com.ctm.daoimpl.MatchDaoImpl;
import com.ctm.daoimpl.TournamentDaoImpl;
import com.ctm.model.Match;
import com.ctm.model.Tournament;

@WebServlet("/startmatch")
public class StartMatchServlet extends HttpServlet {

    private final MatchDaoImpl matchDao = new MatchDaoImpl();
    private final TournamentDaoImpl tournamentDao = new TournamentDaoImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession s = req.getSession(false);
        if (s == null || !"admin".equalsIgnoreCase((String) s.getAttribute("role"))) {
            resp.sendRedirect("index.jsp");
            return;
        }

        resp.setHeader("Cache-Control", "no-cache,no-store,must-revalidate");
        resp.setHeader("Pragma", "no-cache");
        resp.setDateHeader("Expires", 0);

        String tidStr = req.getParameter("tid");

        // 1️⃣ First load: list all tournaments with today’s match count
        if (tidStr == null) {
            req.setAttribute("mode", "tournaments");
            List<Tournament> tournaments = tournamentDao.listAllTournaments();
            Map<Long, Integer> todayCounts = matchDao.todayScheduledCountMap();
            req.setAttribute("tournaments", tournaments);
            req.setAttribute("todayCounts", todayCounts);
            req.getRequestDispatcher("admin_start_match.jsp").forward(req, resp);
            return;
        }

        // 2️⃣ Tournament selected: show matches for today
        long tid = parseLong(tidStr);
        List<Map<String, Object>> matches = matchDao.getTodayMatches(tid);
        Tournament t = tournamentDao.findTournament(tid).orElse(null);

        req.setAttribute("mode", "matches");
        req.setAttribute("tournament", t);
        req.setAttribute("matches", matches);
        req.getRequestDispatcher("admin_start_match.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession s = req.getSession(false);
        if (s == null || !"admin".equalsIgnoreCase((String) s.getAttribute("role"))) {
            resp.sendRedirect("index.jsp");
            return;
        }

        resp.setHeader("Cache-Control", "no-cache,no-store,must-revalidate");
        resp.setHeader("Pragma", "no-cache");
        resp.setDateHeader("Expires", 0);

        long matchId = parseLong(req.getParameter("matchId"));
        long tossWinnerId = parseLong(req.getParameter("tossWinnerId"));
        String tossDecision = req.getParameter("tossDecision");

        boolean ok = matchDao.startMatch(matchId, tossWinnerId, tossDecision);
        resp.sendRedirect(ok ? "startmatch?msg=Match+Started" : "startmatch?err=Failed+to+start+match");
    }

    private long parseLong(String s) {
        try { return Long.parseLong(s); } catch (Exception e) { return -1L; }
    }
}
