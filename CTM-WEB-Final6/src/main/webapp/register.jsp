<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
  // Prevent normal caching
  response.setHeader("Cache-Control","no-cache, no-store, must-revalidate");
  response.setHeader("Pragma","no-cache");
  response.setDateHeader("Expires",0);
%>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>Register (Viewer)</title>
  <link rel="stylesheet" href="resources/css/register.css">
  <script src="resources/js/register.js"></script>
</head>
<body>
  <!-- your existing HTML form unchanged -->
  <div class="page">
    <div class="card">
      <h2>Register as Viewer</h2>
      <p class="rule">Password rule: <b>minimum 8 characters</b>, include at least <b>1 Capital letter</b> and <b>1 Special character</b>.</p>

      <form action="register" method="post">
        <div class="field">
          <label>Username</label>
          <input type="text" name="uname" placeholder="Enter username">
        </div>

        <div class="field">
          <label>Password</label>
          <input type="password" name="pass" placeholder="Min 8 chars, 1 Capital, 1 Special">
        </div>

        <div class="field">
          <label>Re-enter Password</label>
          <input type="password" name="repass" placeholder="Re-enter password">
        </div>

        <button class="btn btn-primary" type="submit">Submit</button>
      </form>

      <div class="links">
        <a href="index.jsp">Back to Login</a>
      </div>
    </div>
  </div>
</body>
</html>
