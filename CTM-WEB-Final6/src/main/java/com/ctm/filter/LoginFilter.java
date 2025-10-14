package com.ctm.filter;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.ctm.util.DaoUtil;

@WebFilter("/login")
public class LoginFilter extends HttpFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req  = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        resp.setHeader("Cache-Control","no-cache, no-store, must-revalidate");
        resp.setHeader("Pragma","no-cache");
        resp.setDateHeader("Expires",0);

        if (!"POST".equalsIgnoreCase(req.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        String role = v(req.getParameter("role"));
        String username = v(req.getParameter("username"));
        String password = v(req.getParameter("password"));

        if (role.isEmpty() || username.isEmpty() || password.isEmpty()) {
            req.setAttribute("errorMsg", "Enter username, password, and select login type.");
            req.getRequestDispatcher("index.jsp").forward(request, response);
            return;
        }

        String table = role.equalsIgnoreCase("admin") ? "admins" : "users";
        boolean ok = false;

        try (PreparedStatement ps = DaoUtil.getMyPreparedStatement(
                "SELECT 1 FROM " + table + " WHERE username=? AND password=?")) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                ok = rs.next();
            }
        } catch (SQLException e) {
            req.setAttribute("errorMsg", "Database error.");
            req.getRequestDispatcher("index.jsp").forward(request, response);
            return;
        }

        if (!ok) {
            req.setAttribute("errorMsg", "Invalid credentials for selected role.");
            req.getRequestDispatcher("index.jsp").forward(request, response);
            return;
        }

        HttpSession session = req.getSession(true);
        session.setAttribute("role", role.toLowerCase());
        session.setAttribute("username", username);
        request.setAttribute("loginOk", role.toLowerCase());

        chain.doFilter(request, response);
    }

    private String v(String s) { return s == null ? "" : s.trim(); }
}
