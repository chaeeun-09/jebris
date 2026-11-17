<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="jebris.util.H, jebris.dto.Member" %>
<%
    String ctx = request.getContextPath();
    String pageTitle = "내 정보";
    String activeMenu = "mypage";
    Member me = (Member) session.getAttribute("loginMember");
    String[] voices = {"M1", "M2", "M3", "M4", "M5", "F1", "F2", "F3", "F4", "F5"};
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
            <p class="eyebrow">PROFILE</p>
            <h1>내 정보</h1>
            <p class="page-sub">가입일 <%= H.e(H.dateTime(me.getCreatedAt())) %></p>
        </div>
    </div>

    <section class="panel card-section">
        <h2>프로필</h2>
        <form method="post" action="<%= ctx %>/mypage" class="form">
            <input type="hidden" name="action" value="profile">
            <label class="field">
                <span>아이디</span>
                <input type="text" value="<%= H.e(me.getLoginId()) %>" disabled>
            </label>
            <label class="field">
                <span>이름 <small>제브리스가 이렇게 불러요</small></span>
                <input type="text" name="memberName" value="<%= H.e(me.getMemberName()) %>" required maxlength="20">
            </label>
            <fieldset class="field">
                <legend>답변 목소리</legend>
                <div class="voice-grid">
                    <% for (String v : voices) { %>
                        <label class="voice-chip">
                            <input type="radio" name="voice" value="<%= v %>" <%= v.equals(me.getVoice()) ? "checked" : "" %>>
                            <span><%= H.voiceLabel(v) %></span>
                        </label>
                    <% } %>
                </div>
            </fieldset>
            <div class="form-actions">
                <button type="submit" class="btn btn-primary">저장</button>
            </div>
        </form>
    </section>

    <section class="panel card-section">
        <h2>비밀번호 변경</h2>
        <form method="post" action="<%= ctx %>/mypage" class="form">
            <input type="hidden" name="action" value="password">
            <label class="field">
                <span>현재 비밀번호</span>
                <input type="password" name="currentPassword" required autocomplete="current-password">
            </label>
            <div class="field-row">
                <label class="field">
                    <span>새 비밀번호 <small>8자 이상</small></span>
                    <input type="password" name="newPassword" required minlength="8" maxlength="64" autocomplete="new-password">
                </label>
                <label class="field">
                    <span>새 비밀번호 확인</span>
                    <input type="password" name="newPasswordConfirm" required minlength="8" maxlength="64" autocomplete="new-password">
                </label>
            </div>
            <div class="form-actions">
                <button type="submit" class="btn btn-primary">비밀번호 변경</button>
            </div>
        </form>
    </section>

    <section class="panel card-section danger-zone">
        <h2>회원 탈퇴</h2>
        <p class="page-sub">탈퇴하면 일정과 대화 기록이 모두 삭제되고 되돌릴 수 없어요.</p>
        <form method="post" action="<%= ctx %>/mypage" class="form inline-form" data-confirm="정말 탈퇴할까요? 모든 데이터가 삭제돼요.">
            <input type="hidden" name="action" value="withdraw">
            <input type="password" name="password" required placeholder="비밀번호 확인" autocomplete="current-password">
            <button type="submit" class="btn btn-danger">탈퇴하기</button>
        </form>
    </section>
</main>
</body>
</html>
