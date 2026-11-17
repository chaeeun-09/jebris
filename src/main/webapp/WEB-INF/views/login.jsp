<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="jebris.util.H" %>
<%
    String ctx = request.getContextPath();
    String pageTitle = "로그인";
    String error = (String) request.getAttribute("error");
    String loginId = (String) request.getAttribute("loginId");
%>
<!DOCTYPE html>
<html lang="ko">
<head>
<%@ include file="/WEB-INF/views/common/head.jspf" %>
</head>
<body class="auth-page">
<div class="hud-bg" aria-hidden="true">
    <div class="ring r1"></div><div class="ring r2"></div><div class="ring r3"></div><div class="ring r4"></div>
</div>
<%@ include file="/WEB-INF/views/common/flash.jspf" %>

<main class="auth-card">
    <div class="auth-brand">
        <span class="brand-ring lg" aria-hidden="true"></span>
        <h1>J.E.B.R.I.S</h1>
        <p>말로 묻고 음성으로 듣는 AI 비서</p>
    </div>

    <% if (error != null) { %>
        <div class="alert alert-error"><%= H.e(error) %></div>
    <% } %>

    <form method="post" action="<%= ctx %>/login" class="form" autocomplete="on">
        <label class="field">
            <span>아이디</span>
            <input type="text" name="loginId" value="<%= H.e(loginId) %>" required maxlength="20" autofocus autocomplete="username">
        </label>
        <label class="field">
            <span>비밀번호</span>
            <input type="password" name="password" required maxlength="64" autocomplete="current-password">
        </label>
        <button type="submit" class="btn btn-primary btn-block">로그인</button>
    </form>

    <p class="auth-switch">처음이신가요? <a href="<%= ctx %>/join">회원가입</a></p>
</main>
</body>
</html>
