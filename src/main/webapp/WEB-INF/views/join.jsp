<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="jebris.util.H" %>
<%
    String ctx = request.getContextPath();
    String pageTitle = "회원가입";
    String error = (String) request.getAttribute("error");
    String loginId = (String) request.getAttribute("loginId");
    String memberName = (String) request.getAttribute("memberName");
    String voice = (String) request.getAttribute("voice");
    if (voice == null || voice.isEmpty()) {
        voice = "M1";
    }
    String[] voices = {"M1", "M2", "M3", "M4", "M5", "F1", "F2", "F3", "F4", "F5"};
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
        <h1>회원가입</h1>
        <p>제브리스가 부를 이름과 목소리를 정해 주세요</p>
    </div>

    <% if (error != null) { %>
        <div class="alert alert-error"><%= H.e(error) %></div>
    <% } %>

    <form method="post" action="<%= ctx %>/join" class="form" id="joinForm">
        <label class="field">
            <span>아이디 <small>영문·숫자·_ 4~20자</small></span>
            <input type="text" name="loginId" value="<%= H.e(loginId) %>" required pattern="[A-Za-z0-9_]{4,20}" maxlength="20" autocomplete="username">
        </label>
        <label class="field">
            <span>이름 <small>제브리스가 이렇게 불러요</small></span>
            <input type="text" name="memberName" value="<%= H.e(memberName) %>" required maxlength="20">
        </label>
        <div class="field-row">
            <label class="field">
                <span>비밀번호 <small>8자 이상</small></span>
                <input type="password" name="password" required minlength="8" maxlength="64" autocomplete="new-password">
            </label>
            <label class="field">
                <span>비밀번호 확인</span>
                <input type="password" name="passwordConfirm" required minlength="8" maxlength="64" autocomplete="new-password">
            </label>
        </div>
        <fieldset class="field">
            <legend>답변 목소리</legend>
            <div class="voice-grid">
                <% for (String v : voices) { %>
                    <label class="voice-chip">
                        <input type="radio" name="voice" value="<%= v %>" <%= v.equals(voice) ? "checked" : "" %>>
                        <span><%= H.voiceLabel(v) %></span>
                    </label>
                <% } %>
            </div>
        </fieldset>
        <button type="submit" class="btn btn-primary btn-block">가입하기</button>
    </form>

    <p class="auth-switch">이미 계정이 있나요? <a href="<%= ctx %>/login">로그인</a></p>
</main>
</body>
</html>
