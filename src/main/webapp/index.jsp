<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    // 시작 주소: 로그인 여부에 따라 메인 또는 로그인 화면으로 보낸다.
    response.sendRedirect(request.getContextPath() + (session.getAttribute("loginMember") != null ? "/main" : "/login"));
%>
