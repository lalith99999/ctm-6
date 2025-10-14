<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    String role = (String) session.getAttribute("role");
    if ("admin".equalsIgnoreCase(role)) {
        response.sendRedirect("adminmain.jsp");
        return;
    }
    if ("viewer".equalsIgnoreCase(role)) {
        response.sendRedirect("usermain.jsp");
        return;
    }

    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    response.setHeader("Pragma", "no-cache");
    response.setDateHeader("Expires", 0);
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Cricket Tournament Login</title>

    <link rel="stylesheet" href="resources/css/index.css">
    <script src="resources/js/index.js"></script>
</head>

<body>
    <header>🏏 Cricket Tournament Manager</header>

    <div class="container">
        <form action="./login" method="post">
            <h2>Welcome Back</h2>

           
            <%
                String okMsg = request.getParameter("okMsg");
                String errorMsg = (String) request.getAttribute("errorMsg");
                if (okMsg != null && okMsg.trim().length() > 0) {
            %>
                <p class="ok"><%= okMsg %></p>
            <%
                }
                if (errorMsg != null && errorMsg.trim().length() > 0) {
            %>
                <p class="error"><%= errorMsg %></p>
            <%
                }
            %>

            <label>Username</label>
            <input type="text" id="username" name="username" placeholder="Enter your username">

            <label>Password</label>
            <input type="password" id="password" name="password" placeholder="Enter your password">

            <input type="hidden" id="role" name="role">

            <button type="submit" class="viewer-btn" onclick="return validateLoginForm('viewer')">Login as Viewer</button>
            <button type="submit" class="admin-btn" onclick="return validateLoginForm('admin')">Login as Scorer</button>

            <p class="register-line">New user? <a href="register.jsp">Click here</a></p>
        </form>
    </div>

    <footer>⚡ Powered by Planon | Cricket Management Portal</footer>
</body>
</html>
