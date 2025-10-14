package com.ctm.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.ctm.dao.MatchDao;
import com.ctm.daoimpl.MatchDaoImpl;
import com.ctm.model.Match;

@WebServlet("/updateinnings")
public class UpdateInningsServlet extends HttpServlet {
    private final MatchDao matchDao = new MatchDaoImpl();

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

        List<Match> live = matchDao.getLiveMatches();
        req.setAttribute("liveMatches", live);
        req.getRequestDispatcher("admin_update_innings.jsp").forward(req, resp);
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

        String step = req.getParameter("step");
        long matchId = parseLong(req.getParameter("matchId"));

        if ("choose".equals(step)) {
            req.setAttribute("selectedMatchId", matchId);
            req.getRequestDispatcher("admin_update_innings.jsp").forward(req, resp);
            return;
        }

        int total = parseInt(req.getParameter("total"));
        int overs = parseInt(req.getParameter("overs"));
        int wickets = parseInt(req.getParameter("wickets"));

        if (!validInputs(total, overs, wickets)) {
            resp.sendRedirect("updateinnings?err=Invalid+inputs");
            return;
        }

        boolean success = false;
        if ("saveFirst".equals(step)) {
            success = matchDao.updateFirstInnings(matchId, total, overs, wickets);
            resp.sendRedirect(success ? "updateinnings?msg=First+innings+saved"
                    : "updateinnings?err=Save+failed");
            return;
        } else if ("saveSecond".equals(step)) {
            success = matchDao.updateSecondInnings(matchId, total, overs, wickets);
            if (success) matchDao.computeAndPersistResult(matchId);
            resp.sendRedirect(success
                    ? "updateinnings?msg=Second+innings+saved+and+result+computed"
                    : "updateinnings?err=Save+failed");
            return;
        }

        resp.sendRedirect("updateinnings?err=Unknown+action");
    }

    private boolean validInputs(int total, int overs, int wickets) {
        if (total < 0 || overs < 0 || overs > 20 || wickets < 0 || wickets > 10) return false;
        if (wickets < 10 && overs != 20) return false;
        return true;
    }

    private long parseLong(String v) {
        try { return Long.parseLong(v); } catch (Exception e) { return 0L; }
    }

    private int parseInt(String v) {
        try { return Integer.parseInt(v); } catch (Exception e) { return -1; }
    }
}
