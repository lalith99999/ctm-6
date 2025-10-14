package com.ctm.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.ctm.daoimpl.PlayerDaoImpl;
import com.ctm.daoimpl.TeamDaoImpl;
import com.ctm.model.Player;
import com.ctm.model.PlayerType;
import com.ctm.model.Team;

@WebServlet("/admplayers")
public class AdminPlayerServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final PlayerDaoImpl playerDao = new PlayerDaoImpl();
    private final TeamDaoImpl teamDao = new TeamDaoImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        process(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        process(req, resp);
    }

    private void process(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

        HttpSession s = req.getSession(false);
        if (s == null || !"admin".equalsIgnoreCase((String) s.getAttribute("role"))) {
            resp.sendRedirect("index.jsp");
            return;
        }

        resp.setHeader("Cache-Control", "no-cache,no-store,must-revalidate");
        resp.setHeader("Pragma", "no-cache");
        resp.setDateHeader("Expires", 0);

        long teamId = parseLong(req.getParameter("teamId"));
        if (teamId <= 0) {
            resp.sendRedirect("admteams?err=Invalid+team");
            return;
        }

        String action = nv(req.getParameter("action"));
        long jersey = parseLong(req.getParameter("jerseyNo"));
        String name = nv(req.getParameter("name"));
        String typeStr = nv(req.getParameter("type"));

        try {
            switch (action.toLowerCase()) {
                case "create":
                    if (playerDao.existsInTeam(teamId, jersey)) {
                        resp.sendRedirect("admplayers?teamId="+teamId+"&err=Jersey+already+exists");
                        return;
                    }
                    if (playerDao.maxLimitReached(teamId)) {
                        resp.sendRedirect("admplayers?teamId="+teamId+"&err=11+players+max");
                        return;
                    }
                    playerDao.createPlayer(teamId, jersey, name, PlayerType.valueOf(typeStr));
                    resp.sendRedirect("admplayers?teamId="+teamId+"&msg=Player+added");
                    return;

                case "update":
                    playerDao.updatePlayer(teamId, jersey, name, PlayerType.valueOf(typeStr));
                    resp.sendRedirect("admplayers?teamId="+teamId+"&msg=Player+updated");
                    return;

                case "delete":
                    playerDao.deletePlayer(teamId, jersey);
                    resp.sendRedirect("admplayers?teamId="+teamId+"&msg=Player+deleted");
                    return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendRedirect("admplayers?teamId="+teamId+"&err=Operation+failed");
            return;
        }

        List<Player> players = playerDao.listPlayers(teamId);
        Team team = teamDao.findTeam(teamId).orElse(new Team(teamId,"Unknown","Unknown"));
        req.setAttribute("team", team);
        req.setAttribute("players", players);
        req.getRequestDispatcher("admin_players.jsp").forward(req, resp);
    }

    private static String nv(String s){ return s==null?"":s.trim(); }
    private static long parseLong(String s){ try{ return Long.parseLong(s);}catch(Exception e){return -1;} }
}
