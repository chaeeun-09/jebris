<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="jebris.util.H, jebris.dto.ChatLog, java.util.List" %>
<%
    String ctx = request.getContextPath();
    String pageTitle = "대화 기록";
    String activeMenu = "history";
    @SuppressWarnings("unchecked")
    List<ChatLog> chats = (List<ChatLog>) request.getAttribute("chats");
    String q = (String) request.getAttribute("q");
    boolean dbError = request.getAttribute("dbError") != null;
%>
<!DOCTYPE html>
<html lang="ko">
<head>
<%@ include file="/WEB-INF/views/common/head.jspf" %>
</head>
<body class="app-page">
<%@ include file="/WEB-INF/views/common/nav.jspf" %>

<main class="page page-narrow">
    <div class="page-head">
        <div>
            <p class="eyebrow">HISTORY</p>
            <h1>대화 기록</h1>
            <p class="page-sub">제브리스와 나눈 대화가 최신순으로 저장돼요. (최대 200건 표시)</p>
        </div>
        <% if (chats != null && !chats.isEmpty() && (q == null || q.isEmpty())) { %>
            <form method="post" action="<%= ctx %>/history" data-confirm="대화 기록을 모두 삭제할까요? 되돌릴 수 없어요.">
                <input type="hidden" name="action" value="clear">
                <button type="submit" class="btn btn-danger">전체 삭제</button>
            </form>
        <% } %>
    </div>

    <% if (dbError) { %>
        <div class="alert alert-error">데이터베이스에 연결하지 못했어요.</div>
    <% } %>

    <form method="get" action="<%= ctx %>/history" class="search-bar">
        <input type="search" name="q" value="<%= H.e(q) %>" placeholder="질문이나 답변 내용으로 검색">
        <button type="submit" class="btn btn-ghost">검색</button>
    </form>

    <% if (chats == null || chats.isEmpty()) { %>
        <div class="empty-state panel">
            <div class="empty-ring" aria-hidden="true"></div>
            <p><%= q != null && !q.isEmpty() ? "검색 결과가 없어요." : "아직 대화 기록이 없어요." %></p>
            <a class="btn btn-primary" href="<%= ctx %>/main">대화하러 가기</a>
        </div>
    <% } else { %>
        <ul class="history-list">
            <% for (ChatLog c : chats) { %>
                <li class="panel history-item">
                    <div class="h-head">
                        <span class="badge badge-intent-<%= H.e(c.getIntent()) %>"><%= H.e(H.intentLabel(c.getIntent())) %></span>
                        <time><%= H.e(H.dateTime(c.getCreatedAt())) %></time>
                        <form method="post" action="<%= ctx %>/history" data-confirm="이 대화를 삭제할까요?">
                            <input type="hidden" name="action" value="delete">
                            <input type="hidden" name="id" value="<%= c.getChatId() %>">
                            <button type="submit" class="icon-btn" aria-label="삭제" title="삭제">&times;</button>
                        </form>
                    </div>
                    <p class="h-q"><span class="who">나</span><%= H.e(c.getQuestion()) %></p>
                    <p class="h-a"><span class="who">J</span><%= H.e(c.getAnswer()) %></p>
                </li>
            <% } %>
        </ul>
    <% } %>
</main>
</body>
</html>
