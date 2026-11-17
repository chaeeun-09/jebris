<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ page import="jebris.util.H" %>
<%
    String ctx = request.getContextPath();
    Object codeAttr = request.getAttribute("jakarta.servlet.error.status_code");
    int code = codeAttr instanceof Integer ? (Integer) codeAttr : 500;
    String pageTitle = code == 404 ? "페이지 없음" : "오류";
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
<main class="auth-card error-card">
    <p class="error-code"><%= code %></p>
    <h1><%= code == 404 ? "찾는 페이지가 없어요" : "문제가 생겼어요" %></h1>
    <p class="page-sub"><%= code == 404 ? "주소를 다시 확인해 주세요." : "잠시 후 다시 시도해 주세요. 계속되면 서버 콘솔 로그를 확인해 주세요." %></p>
    <a class="btn btn-primary btn-block" href="<%= ctx %>/main">메인으로</a>
</main>
</body>
</html>
