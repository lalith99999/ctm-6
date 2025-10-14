package com.ctm.servlet;

import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import com.ctm.daoimpl.TournamentDaoImpl;
import com.ctm.model.Tournament;

@WebServlet("/admtournaments")
public class TournamentAdminServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final TournamentDaoImpl dao = new TournamentDaoImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession s = req.getSession(false);
        if (s == null || !"admin".equalsIgnoreCase((String) s.getAttribute("role"))) {
            resp.sendRedirect("index.jsp");
            return;
        }

        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        resp.setHeader("Pragma", "no-cache");
        resp.setDateHeader("Expires", 0);

        List<Tournament> tournaments = dao.listAllTournaments();
        req.setAttribute("tournaments", tournaments);
        req.getRequestDispatcher("admin_tournaments.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession s = req.getSession(false);
        if (s == null || !"admin".equalsIgnoreCase((String) s.getAttribute("role"))) {
            resp.sendRedirect("index.jsp");
            return;
        }

        String action = nv(req.getParameter("action"));
        String name = nv(req.getParameter("name"));
        long id = parseLong(req.getParameter("id"));

        try {
            if ("create".equalsIgnoreCase(action)) {
                if (dao.existsByName(name)) {
                    resp.sendRedirect("admtournaments?err=Tournament+already+exists");
                    return;
                }
                dao.createTournament(name, "T20");
                resp.sendRedirect("admtournaments?msg=Tournament+created");
                return;
            }

            if ("update".equalsIgnoreCase(action)) {
                dao.updateTournamentName(id, name);
                resp.sendRedirect("admtournaments?msg=Tournament+updated");
                return;
            }

            if ("delete".equalsIgnoreCase(action)) {
                dao.deleteTournament(id);
                resp.sendRedirect("admtournaments?msg=Tournament+deleted");
                return;
            }

            resp.sendRedirect("admtournaments?err=Invalid+action");
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendRedirect("admtournaments?err=Operation+failed");
        }
    }

    private static String nv(String s) { return s == null ? "" : s.trim(); }
    private static long parseLong(String s) {
        try { return Long.parseLong(s); } catch (Exception e) { return -1; }
    }
}
