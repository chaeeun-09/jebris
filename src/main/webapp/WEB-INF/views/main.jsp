<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="jebris.util.H, jebris.dto.ChatLog, java.util.List" %>
<%
    String ctx = request.getContextPath();
    String pageTitle = "메인";
    String activeMenu = "main";
    @SuppressWarnings("unchecked")
    List<ChatLog> recentChats = (List<ChatLog>) request.getAttribute("recentChats");
    boolean dbError = request.getAttribute("dbError") != null;
%>
<!DOCTYPE html>
<html lang="ko">
<head>
<%@ include file="/WEB-INF/views/common/head.jspf" %>
<script src="<%= ctx %>/js/jarvis.js" defer></script>
</head>
<body class="app-page" data-ctx="<%= H.e(ctx) %>">
<%@ include file="/WEB-INF/views/common/nav.jspf" %>

<main class="main-grid">
    <!-- 왼쪽: 다가오는 일정 -->
    <aside class="panel schedule-panel">
        <div class="panel-head">
            <h2>다가오는 일정</h2>
            <a href="<%= ctx %>/schedule" class="link-sm">전체 보기</a>
        </div>
        <ul class="today-list" id="todayList">
            <li class="empty">일정을 불러오는 중...</li>
        </ul>
        <div class="panel-foot">
            <p class="hint-title">이렇게 말해 보세요</p>
            <ul class="hint-list">
                <li>“금요일 저녁 7시에 축구 추가해줘”</li>
                <li>“내일 일정 뭐 있어?”</li>
                <li>“오늘 할 일 정리해줘”</li>
            </ul>
        </div>
    </aside>

    <!-- 가운데: 제브리스 코어 -->
    <section class="core-area">
        <div class="clock">
            <div class="clock-time" id="clockTime">--:--</div>
            <div class="clock-date" id="clockDate"></div>
        </div>

        <div class="core" id="core" data-state="idle">
            <div class="ring r1"></div>
            <div class="ring r2"></div>
            <div class="ring r3"></div>
            <div class="ring r4"></div>
            <div class="core-dot"></div>
        </div>

        <div class="state-label" id="stateLabel">대기 중</div>
        <div class="transcript" id="transcript">마이크를 누르고 말을 걸어 보세요</div>

        <button type="button" class="mic-btn" id="micBtn" aria-label="말하기 시작">
            <svg viewBox="0 0 24 24" width="34" height="34" aria-hidden="true">
                <path fill="currentColor" d="M12 15a3.5 3.5 0 0 0 3.5-3.5v-6a3.5 3.5 0 1 0-7 0v6A3.5 3.5 0 0 0 12 15Zm6-3.5a1 1 0 1 1 2 0 8 8 0 0 1-7 7.94V21a1 1 0 1 1-2 0v-1.56A8 8 0 0 1 4 11.5a1 1 0 1 1 2 0 6 6 0 0 0 12 0Z"/>
            </svg>
        </button>
        <p class="mic-hint">클릭하거나 <kbd>Space</kbd>를 눌러 말하기</p>

        <form class="text-form" id="textForm" autocomplete="off">
            <input type="text" id="textInput" maxlength="500" placeholder="말 대신 입력해도 돼요">
            <button type="submit" class="btn btn-primary">보내기</button>
        </form>

        <label class="switch">
            <input type="checkbox" id="voiceToggle" checked>
            <span class="switch-track"><span class="switch-thumb"></span></span>
            <span>음성으로 답하기</span>
        </label>

        <p class="warn" id="speechWarn" hidden>이 브라우저는 음성 인식을 지원하지 않아요. 크롬이나 엣지에서 열거나, 위 입력창을 사용해 주세요.</p>
        <% if (dbError) { %>
            <p class="warn">데이터베이스에 연결하지 못했어요. Oracle 실행 상태와 config.properties를 확인해 주세요.</p>
        <% } %>
    </section>

    <!-- 오른쪽: 대화 -->
    <aside class="panel chat-panel">
        <div class="panel-head">
            <h2>대화</h2>
            <a href="<%= ctx %>/history" class="link-sm">기록 보기</a>
        </div>
        <div class="chat-log" id="chatLog">
            <% if (recentChats == null || recentChats.isEmpty()) { %>
                <div class="chat-empty" id="chatEmpty">
                    <p>아직 대화가 없어요.</p>
                    <p>“안녕, 제브리스”라고 말해 보세요.</p>
                </div>
            <% } else {
                   for (ChatLog c : recentChats) { %>
                <div class="msg msg-user"><div class="bubble"><%= H.e(c.getQuestion()) %></div></div>
                <div class="msg msg-assistant">
                    <div class="bubble"><%= H.e(c.getAnswer()) %></div>
                    <div class="msg-meta"><%= H.e(H.intentLabel(c.getIntent())) %> · <%= H.e(H.time(c.getCreatedAt())) %></div>
                </div>
            <%     }
               } %>
        </div>
    </aside>
</main>
</body>
</html>
