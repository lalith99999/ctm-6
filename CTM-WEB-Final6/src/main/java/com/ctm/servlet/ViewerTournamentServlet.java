package com.ctm.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.ctm.dao.TournamentDao;
import com.ctm.daoimpl.TournamentDaoImpl;
import com.ctm.model.Tournament;

/**
 * Servlet implementation class ViewerTournamentServlet
 */
@WebServlet("/tournaments")
public class ViewerTournamentServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final TournamentDao tournamentDao = new TournamentDaoImpl();
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ViewerTournamentServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */

    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Session guard
        HttpSession session = req.getSession(false);
        if (session == null || !"viewer".equalsIgnoreCase((String) session.getAttribute("role"))) {
            resp.sendRedirect("index.jsp");
            return;
        }

        // Fetch all tournaments
        List<Tournament> tournaments = tournamentDao.listAllTournaments();

        // Set and forward to JSP
        req.setAttribute("tournaments", tournaments);
        req.getRequestDispatcher("viewer_tournament.jsp").forward(req, resp);
    }
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
