package com.ctm.servlet;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.ctm.daoimpl.TeamDaoImpl;
import com.ctm.daoimpl.TournamentDaoImpl;
import com.ctm.model.Team;
import com.ctm.model.TeamStanding;
import com.ctm.model.Tournament;
import com.ctm.util.DaoUtil;

@WebServlet("/enroll")
public class EnrollTeamsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final TeamDaoImpl teamDao = new TeamDaoImpl();
    private final TournamentDaoImpl tDao = new TournamentDaoImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession s = req.getSession(false);
        if (s == null || !"admin".equalsIgnoreCase(String.valueOf(s.getAttribute("role")))) {
            resp.sendRedirect("index.jsp");
            return;
        }

        resp.setHeader("Cache-Control","no-cache,no-store,must-revalidate");
        resp.setHeader("Pragma","no-cache");
        resp.setDateHeader("Expires",0);

        long tid = parseLong(req.getParameter("tid"));
        String action = n(req.getParameter("action"));
        long teamId = parseLong(req.getParameter("teamId"));

        // 🟢 If tid is not passed → show tournament list
        if (tid <= 0) {
            List<Tournament> all = tDao.listAllTournaments();
            Set<Long> lockedIds = new HashSet<>();
            for (Tournament t : all) if (hasFixtures(t.getId())) lockedIds.add(t.getId());

            req.setAttribute("tournaments", all);
            req.setAttribute("lockedIds", lockedIds);
            req.getRequestDispatcher("admin_enroll.jsp").forward(req, resp);
            return;
        }

        // 🟢 Specific tournament section
        Optional<Tournament> tourOpt = tDao.findTournament(tid);
        if (!tourOpt.isPresent()) {
            resp.sendRedirect("enroll?err=Tournament+not+found");
            return;
        }
        boolean locked = hasFixtures(tid);

        // Actions (add/remove)
        try {
            if ("add".equalsIgnoreCase(action) && teamId > 0) {
                if (locked) {
                    resp.sendRedirect("enroll?tid=" + tid + "&err=locked");
                    return;
                }
                List<TeamStanding> enrolled = tDao.listTeamsInTournament(tid);
                boolean exists = enrolled.stream().anyMatch(ts -> ts.getTeamId() == teamId);
                if (!exists) tDao.enrollTeam(tid, teamId);
                resp.sendRedirect("enroll?tid=" + tid + "&msg=added");
                return;
            }

            if ("remove".equalsIgnoreCase(action) && teamId > 0) {
                if (locked) {
                    resp.sendRedirect("enroll?tid=" + tid + "&err=locked");
                    return;
                }
                tDao.deleteTournamentTeam(tid, teamId);
                resp.sendRedirect("enroll?tid=" + tid + "&msg=removed");
                return;
            }
        } catch (Exception ignore) {
            resp.sendRedirect("enroll?tid=" + tid + "&err=1");
            return;
        }

        // Load tournament teams
        List<TeamStanding> enrolled = tDao.listTeamsInTournament(tid);
        Set<Long> enrolledIds = enrolled.stream().map(TeamStanding::getTeamId).collect(Collectors.toSet());
        List<Team> allTeams = teamDao.listTeams();
        List<Team> available = allTeams.stream().filter(t -> !enrolledIds.contains(t.getId())).collect(Collectors.toList());

        req.setAttribute("tournaments", tDao.listAllTournaments());
        req.setAttribute("lockedIds", getLockedSet());
        req.setAttribute("selectedTournament", tourOpt.get());
        req.setAttribute("enrolled", enrolled);
        req.setAttribute("available", available);
        req.setAttribute("locked", locked);
        req.setAttribute("msg", n(req.getParameter("msg")));
        req.setAttribute("err", n(req.getParameter("err")));

        req.getRequestDispatcher("admin_enroll.jsp").forward(req, resp);
    }

    private boolean hasFixtures(long tournamentId) {
        final String sql = "SELECT COUNT(*) FROM matches WHERE tournament_id=?";
        try (PreparedStatement ps = DaoUtil.getMyPreparedStatement(sql)) {
            ps.setLong(1, tournamentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) { return false; }
    }

    private Set<Long> getLockedSet() {
        Set<Long> locked = new HashSet<>();
        try (PreparedStatement ps = DaoUtil.getMyPreparedStatement("SELECT DISTINCT tournament_id FROM matches")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) locked.add(rs.getLong(1));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return locked;
    }

    private String n(String s){ return s==null?"":s.trim(); }
    private long parseLong(String s){ try{ return Long.parseLong(s);}catch(Exception e){ return -1L; } }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException { doGet(req, resp); }
}
