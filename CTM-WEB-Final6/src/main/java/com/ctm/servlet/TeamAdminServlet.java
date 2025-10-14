package com.ctm.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.ctm.daoimpl.TeamDaoImpl;
import com.ctm.model.Team;

@WebServlet("/admteams")
public class TeamAdminServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final TeamDaoImpl dao = new TeamDaoImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        handle(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        handle(req, resp);
    }

    private void handle(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

        HttpSession s = req.getSession(false);
        if (s == null || !"admin".equalsIgnoreCase((String) s.getAttribute("role"))) {
            resp.sendRedirect("index.jsp");
            return;
        }

        resp.setHeader("Cache-Control","no-cache,no-store,must-revalidate");
        resp.setHeader("Pragma","no-cache");
        resp.setDateHeader("Expires",0);

        String action = nv(req.getParameter("action"));
        String name = nv(req.getParameter("name"));
        String city = nv(req.getParameter("city"));
        long id = parseLong(req.getParameter("id"));

        try {
            switch (action.toLowerCase()) {
                case "create":
                    if (dao.existsByName(name)) {
                        resp.sendRedirect("admteams?err=Team+already+exists");
                        return;
                    }
                    dao.createTeam(name, city);
                    resp.sendRedirect("admteams?msg=Team+created");
                    return;

                case "update":
                    dao.updateTeam(id, name, city);
                    resp.sendRedirect("admteams?msg=Team+updated");
                    return;

                case "delete":
                    dao.deleteTeam(id);
                    resp.sendRedirect("admteams?msg=Team+deleted");
                    return;

                default:
                    break;
            }

            List<Team> teams = dao.listTeams();
            req.setAttribute("teams", teams);
            req.getRequestDispatcher("admin_teams.jsp").forward(req, resp);

        } catch (Exception e) {
            e.printStackTrace();
            resp.sendRedirect("admteams?err=Operation+failed");
        }
    }

    private static String nv(String s){ return s==null?"":s.trim(); }
    private static long parseLong(String s){ try{ return Long.parseLong(s); }catch(Exception e){return -1;} }
}
