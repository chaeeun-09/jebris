<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="jebris.util.H, jebris.dto.Schedule, java.util.List" %>
<%
    String ctx = request.getContextPath();
    String pageTitle = "일정";
    String activeMenu = "schedule";
    @SuppressWarnings("unchecked")
    List<Schedule> schedules = (List<Schedule>) request.getAttribute("schedules");
    Schedule form = (Schedule) request.getAttribute("form");
    String error = (String) request.getAttribute("error");
    String q = (String) request.getAttribute("q");
    String status = (String) request.getAttribute("status");
    boolean dbError = request.getAttribute("dbError") != null;
    boolean editing = form != null && form.getScheduleId() > 0;
    String[] statuses = {"PLANNED", "DONE", "CANCELED"};
%>
<!DOCTYPE html>
<html lang="ko">
<head>
<%@ include file="/WEB-INF/views/common/head.jspf" %>
</head>
<body class="app-page">
<%@ include file="/WEB-INF/views/common/nav.jspf" %>

<main class="page">
    <div class="page-head">
        <div>
            <p class="eyebrow">SCHEDULE</p>
            <h1>일정 관리</h1>
            <p class="page-sub">음성으로 등록한 일정도 여기에서 확인하고 고칠 수 있어요.</p>
        </div>
    </div>

    <% if (dbError) { %>
        <div class="alert alert-error">데이터베이스에 연결하지 못했어요. Oracle 실행 상태와 config.properties를 확인해 주세요.</div>
    <% } %>

    <div class="page-grid">
        <!-- 등록·수정 폼 -->
        <section class="panel form-panel" id="scheduleForm">
            <h2><%= editing ? "일정 수정" : "새 일정" %></h2>
            <% if (error != null) { %>
                <div class="alert alert-error"><%= H.e(error) %></div>
            <% } %>
            <form method="post" action="<%= ctx %>/schedule" class="form">
                <input type="hidden" name="action" value="<%= editing ? "update" : "add" %>">
                <input type="hidden" name="backQ" value="<%= H.e(q) %>">
                <input type="hidden" name="backStatus" value="<%= H.e(status) %>">
                <% if (editing) { %>
                    <input type="hidden" name="id" value="<%= form.getScheduleId() %>">
                <% } %>
                <label class="field">
                    <span>제목</span>
                    <input type="text" name="title" required maxlength="100" placeholder="예: 축구 경기"
                           value="<%= form == null ? "" : H.e(form.getTitle()) %>">
                </label>
                <label class="field">
                    <span>날짜와 시간</span>
                    <input type="datetime-local" name="startAt" required
                           value="<%= form == null ? "" : H.input(form.getStartAt()) %>">
                </label>
                <label class="field">
                    <span>메모 <small>선택</small></span>
                    <textarea name="memo" rows="3" maxlength="500" placeholder="장소, 준비물 등"><%= form == null ? "" : H.e(form.getMemo()) %></textarea>
                </label>
                <% if (editing) { %>
                    <label class="field">
                        <span>상태</span>
                        <select name="status">
                            <% for (String st : statuses) { %>
                                <option value="<%= st %>"<%= H.selected(st.equals(form.getStatus())) %>><%= H.statusLabel(st) %></option>
                            <% } %>
                        </select>
                    </label>
                <% } %>
                <div class="form-actions">
                    <button type="submit" class="btn btn-primary"><%= editing ? "수정 저장" : "등록하기" %></button>
                    <% if (editing) { %>
                        <a class="btn btn-ghost" href="<%= ctx %>/schedule">취소</a>
                    <% } %>
                </div>
            </form>
        </section>

        <!-- 목록 -->
        <section class="panel list-panel">
            <form method="get" action="<%= ctx %>/schedule" class="search-bar">
                <input type="search" name="q" value="<%= H.e(q) %>" placeholder="제목이나 메모로 검색">
                <select name="status" onchange="this.form.submit()">
                    <option value="">전체 상태</option>
                    <% for (String st : statuses) { %>
                        <option value="<%= st %>"<%= H.selected(st.equals(status)) %>><%= H.statusLabel(st) %></option>
                    <% } %>
                </select>
                <button type="submit" class="btn btn-ghost">검색</button>
            </form>

            <% if (schedules == null || schedules.isEmpty()) { %>
                <div class="empty-state">
                    <div class="empty-ring" aria-hidden="true"></div>
                    <p><%= (q != null && !q.isEmpty()) || (status != null && !status.isEmpty()) ? "조건에 맞는 일정이 없어요." : "아직 등록된 일정이 없어요." %></p>
                </div>
            <% } else { %>
                <p class="count">총 <b><%= schedules.size() %></b>건</p>
                <ul class="schedule-list">
                    <% for (Schedule s : schedules) { %>
                        <li class="schedule-item status-<%= s.getStatus() %><%= s.isOverdue() ? " overdue" : "" %>">
                            <div class="s-when">
                                <span class="s-date"><%= H.e(H.date(s.getStartAt())) %></span>
                                <span class="s-time"><%= H.e(H.time(s.getStartAt())) %></span>
                            </div>
                            <div class="s-body">
                                <div class="s-title">
                                    <%= H.e(s.getTitle()) %>
                                    <span class="badge badge-<%= s.getStatus() %>"><%= H.statusLabel(s.getStatus()) %></span>
                                    <% if (s.isOverdue()) { %><span class="badge badge-overdue">시간 지남</span><% } %>
                                </div>
                                <% if (s.getMemo() != null) { %>
                                    <div class="s-memo"><%= H.e(s.getMemo()) %></div>
                                <% } %>
                            </div>
                            <div class="s-actions">
                                <% if ("PLANNED".equals(s.getStatus())) { %>
                                    <form method="post" action="<%= ctx %>/schedule">
                                        <input type="hidden" name="action" value="status">
                                        <input type="hidden" name="id" value="<%= s.getScheduleId() %>">
                                        <input type="hidden" name="status" value="DONE">
                                        <input type="hidden" name="backQ" value="<%= H.e(q) %>">
                                        <input type="hidden" name="backStatus" value="<%= H.e(status) %>">
                                        <button type="submit" class="btn btn-ok btn-sm">완료</button>
                                    </form>
                                <% } %>
                                <a class="btn btn-ghost btn-sm" href="<%= ctx %>/schedule?edit=<%= s.getScheduleId() %>#scheduleForm">수정</a>
                                <form method="post" action="<%= ctx %>/schedule" data-confirm="‘<%= H.e(s.getTitle()) %>’ 일정을 삭제할까요?">
                                    <input type="hidden" name="action" value="delete">
                                    <input type="hidden" name="id" value="<%= s.getScheduleId() %>">
                                    <input type="hidden" name="backQ" value="<%= H.e(q) %>">
                                    <input type="hidden" name="backStatus" value="<%= H.e(status) %>">
                                    <button type="submit" class="btn btn-danger btn-sm">삭제</button>
                                </form>
                            </div>
                        </li>
                    <% } %>
                </ul>
            <% } %>
        </section>
    </div>
</main>
</body>
</html>
