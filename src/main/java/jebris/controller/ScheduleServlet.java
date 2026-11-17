package jebris.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Collections;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jebris.dao.ScheduleDAO;
import jebris.dto.Member;
import jebris.dto.Schedule;
import jebris.util.Web;

/**
 * 일정 관리 (CRUD + 검색)
 *  GET  /schedule?q=검색어&status=PLANNED&edit=번호
 *  POST /schedule action=add | update | status | delete
 */
@WebServlet("/schedule")
public class ScheduleServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final ScheduleDAO scheduleDAO = new ScheduleDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Member member = Web.member(req);
        Schedule form = null;
        long editId = Web.paramLong(req, "edit", 0);
        if (editId > 0) {
            try {
                form = scheduleDAO.findById(member.getMemberId(), editId);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (form == null) {
                Web.flash(req, "수정할 일정을 찾지 못했어요.", "error");
                Web.redirect(req, resp, "/schedule");
                return;
            }
        }
        render(req, resp, member, form, null);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Member member = Web.member(req);
        String action = Web.param(req, "action");
        long id = Web.paramLong(req, "id", 0);
        try {
            switch (action) {
                case "add":
                case "update": {
                    Schedule s = readForm(req, member.getMemberId());
                    String error = validate(req, s);
                    if (error != null) {
                        render(req, resp, member, s, error);
                        return;
                    }
                    if ("add".equals(action)) {
                        s.setStatus("PLANNED");
                        scheduleDAO.insert(s);
                        Web.flash(req, "일정을 등록했어요.", "ok");
                    } else {
                        s.setScheduleId(id);
                        if (scheduleDAO.update(s) == 0) {
                            Web.flash(req, "수정할 일정을 찾지 못했어요.", "error");
                        } else {
                            Web.flash(req, "일정을 수정했어요.", "ok");
                        }
                    }
                    break;
                }
                case "status": {
                    String status = Web.param(req, "status");
                    if (!isStatus(status)) {
                        Web.flash(req, "잘못된 상태 값이에요.", "error");
                    } else if (scheduleDAO.updateStatus(member.getMemberId(), id, status) > 0) {
                        Web.flash(req, "상태를 바꿨어요.", "ok");
                    }
                    break;
                }
                case "delete": {
                    if (scheduleDAO.delete(member.getMemberId(), id) > 0) {
                        Web.flash(req, "일정을 삭제했어요.", "ok");
                    } else {
                        Web.flash(req, "이미 삭제된 일정이에요.", "error");
                    }
                    break;
                }
                default:
                    Web.flash(req, "알 수 없는 요청이에요.", "error");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Web.flash(req, "데이터베이스 오류로 처리하지 못했어요.", "error");
        }
        Web.redirect(req, resp, "/schedule" + backQuery(req));
    }

    private void render(HttpServletRequest req, HttpServletResponse resp, Member member, Schedule form, String error)
            throws ServletException, IOException {
        String q = Web.param(req, "q");
        String status = Web.param(req, "status");
        if (!isStatus(status)) {
            status = "";
        }
        if (req.getMethod().equals("POST")) {
            // 등록·수정 실패 후 다시 그릴 때는 검색 조건을 비운다.
            q = Web.param(req, "backQ");
            status = isStatus(Web.param(req, "backStatus")) ? Web.param(req, "backStatus") : "";
        }
        try {
            req.setAttribute("schedules", scheduleDAO.findByMember(member.getMemberId(), q, status));
        } catch (SQLException e) {
            e.printStackTrace();
            req.setAttribute("schedules", Collections.emptyList());
            req.setAttribute("dbError", true);
        }
        req.setAttribute("q", q);
        req.setAttribute("status", status);
        req.setAttribute("form", form);
        req.setAttribute("error", error);
        Web.view(req, resp, "schedule");
    }

    private Schedule readForm(HttpServletRequest req, long memberId) {
        Schedule s = new Schedule();
        s.setMemberId(memberId);
        s.setScheduleId(Web.paramLong(req, "id", 0));
        s.setTitle(Web.param(req, "title"));
        String memo = Web.param(req, "memo");
        s.setMemo(memo.isEmpty() ? null : memo);
        String status = Web.param(req, "status");
        s.setStatus(isStatus(status) ? status : "PLANNED");
        try {
            s.setStartAt(LocalDateTime.parse(Web.param(req, "startAt")));
        } catch (DateTimeParseException e) {
            s.setStartAt(null);
        }
        return s;
    }

    private String validate(HttpServletRequest req, Schedule s) {
        if (s.getTitle().isEmpty()) {
            return "일정 제목을 입력해 주세요.";
        }
        if (s.getTitle().length() > 100) {
            return "일정 제목은 100자 이하로 입력해 주세요.";
        }
        if (s.getStartAt() == null) {
            return "날짜와 시간을 올바르게 입력해 주세요.";
        }
        if (s.getMemo() != null && s.getMemo().length() > 500) {
            return "메모는 500자 이하로 입력해 주세요.";
        }
        if ("add".equals(Web.param(req, "action")) && s.getStartAt().isBefore(LocalDateTime.now().minusMinutes(1))) {
            return "지난 시간에는 새 일정을 등록할 수 없어요.";
        }
        return null;
    }

    private String backQuery(HttpServletRequest req) {
        String q = Web.param(req, "backQ");
        String status = Web.param(req, "backStatus");
        StringBuilder sb = new StringBuilder();
        if (!q.isEmpty()) {
            sb.append("q=").append(java.net.URLEncoder.encode(q, java.nio.charset.StandardCharsets.UTF_8));
        }
        if (isStatus(status)) {
            sb.append(sb.length() > 0 ? "&" : "").append("status=").append(status);
        }
        return sb.length() == 0 ? "" : "?" + sb;
    }

    static boolean isStatus(String status) {
        return "PLANNED".equals(status) || "DONE".equals(status) || "CANCELED".equals(status);
    }
}
