package jebris.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jebris.dao.ScheduleDAO;
import jebris.dto.Member;
import jebris.dto.Schedule;
import jebris.service.AssistantService;
import jebris.util.H;
import jebris.util.Web;

/** 메인 화면 왼쪽의 '다가오는 일정' (오늘부터 7일) */
@WebServlet("/api/today")
public class TodayApiServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final ScheduleDAO scheduleDAO = new ScheduleDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Member member = Web.member(req);
        LocalDate today = LocalDate.now(AssistantService.ZONE);
        try {
            List<Schedule> schedules = scheduleDAO.findBetween(member.getMemberId(),
                    today.atStartOfDay(), today.plusDays(8).atStartOfDay());
            List<Map<String, Object>> items = new ArrayList<>();
            for (Schedule s : schedules) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("id", s.getScheduleId());
                item.put("title", s.getTitle());
                item.put("memo", s.getMemo());
                item.put("date", H.date(s.getStartAt()));
                item.put("time", H.time(s.getStartAt()));
                item.put("today", s.getStartAt().toLocalDate().equals(today));
                item.put("status", s.getStatus());
                item.put("overdue", s.isOverdue());
                items.add(item);
            }
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("ok", true);
            body.put("items", items);
            Web.json(resp, 200, body);
        } catch (SQLException e) {
            e.printStackTrace();
            Web.json(resp, 500, Map.of("ok", false, "error", "일정을 불러오지 못했어요."));
        }
    }
}
