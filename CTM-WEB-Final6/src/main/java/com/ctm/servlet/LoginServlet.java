package com.ctm.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public LoginServlet() { super(); }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Object ok = request.getAttribute("loginOk");
        if (ok == null) {
            request.getRequestDispatcher("index.jsp").forward(request, response);
            return;
        }

        String role = ok.toString().toLowerCase();

        if ("admin".equals(role)) {
            response.sendRedirect("adminmain.jsp");
            return;
        }

        if ("user".equals(role) || "viewer".equals(role)) {
            response.sendRedirect("tournament_home.jsp");
            return;
        }

        response.sendRedirect("index.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
