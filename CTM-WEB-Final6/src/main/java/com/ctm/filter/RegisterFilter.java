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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ctm.util.DaoUtil;

@WebFilter("/register")
public class RegisterFilter implements Filter {

    private static final String PWD_REGEX = "^(?=.*[A-Z])(?=.*[^A-Za-z0-9]).{8,}$";

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

        String uname  = req.getParameter("uname");
        String pass   = req.getParameter("pass");
        String repass = req.getParameter("repass");

        if (blank(uname) || blank(pass) || blank(repass) || !pass.equals(repass) || !pass.matches(PWD_REGEX)) {
            resp.sendRedirect("register.jsp");
            return;
        }

        try (PreparedStatement chk = DaoUtil.getMyPreparedStatement(
                "SELECT 1 FROM users WHERE username=?")) {
            chk.setString(1, uname);
            try (ResultSet rs = chk.executeQuery()) {
                if (rs.next()) {
                    resp.sendRedirect("register.jsp");
                    return;
                }
            }
        } catch (SQLException e) {
            resp.sendRedirect("register.jsp");
            return;
        }

        try (PreparedStatement ins = DaoUtil.getMyPreparedStatement(
                "INSERT INTO users (id, username, password) " +
                "VALUES ((SELECT NVL(MAX(id),0)+1 FROM users), ?, ?)")) {
            ins.setString(1, uname);
            ins.setString(2, pass);
            if (ins.executeUpdate() <= 0) {
                resp.sendRedirect("register.jsp");
                return;
            }
        } catch (SQLException e) {
            resp.sendRedirect("register.jsp");
            return;
        }

        request.setAttribute("registerSuccess", true);
        chain.doFilter(request, response);
    }

    private boolean blank(String s) { return s == null || s.trim().isEmpty(); }
}
