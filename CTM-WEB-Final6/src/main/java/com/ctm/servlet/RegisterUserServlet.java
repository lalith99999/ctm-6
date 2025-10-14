package com.ctm.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/register")
public class RegisterUserServlet extends HttpServlet {


	 @Override
	    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
	            throws ServletException, IOException {

	        boolean registrationSucceeded = Boolean.TRUE.equals(req.getAttribute("registerSuccess"));

	        if (registrationSucceeded) {
	            String msg = java.net.URLEncoder.encode("New user created. Please login.",
	                                                    java.nio.charset.StandardCharsets.UTF_8);
	            resp.sendRedirect("index.jsp?okMsg=" + msg);
	            return;
	        }

	        req.getRequestDispatcher("register.jsp").forward(req, resp);
	    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Keep everything centralized in doGet
        doGet(request, response);
    }
}
