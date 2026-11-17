package jebris.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jebris.dao.ChatLogDAO;
import jebris.dto.Member;
import jebris.util.Web;

/** 대화 기록 조회·검색·삭제 */
@WebServlet("/history")
public class HistoryServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final ChatLogDAO chatLogDAO = new ChatLogDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Member member = Web.member(req);
        String q = Web.param(req, "q");
        try {
            req.setAttribute("chats", chatLogDAO.findByMember(member.getMemberId(), q));
        } catch (SQLException e) {
            e.printStackTrace();
            req.setAttribute("chats", Collections.emptyList());
            req.setAttribute("dbError", true);
        }
        req.setAttribute("q", q);
        Web.view(req, resp, "history");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Member member = Web.member(req);
        String action = Web.param(req, "action");
        try {
            if ("delete".equals(action)) {
                chatLogDAO.delete(member.getMemberId(), Web.paramLong(req, "id", 0));
                Web.flash(req, "대화를 삭제했어요.", "ok");
            } else if ("clear".equals(action)) {
                int n = chatLogDAO.deleteAll(member.getMemberId());
                Web.flash(req, "대화 기록 " + n + "건을 모두 삭제했어요.", "ok");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Web.flash(req, "데이터베이스 오류로 삭제하지 못했어요.", "error");
        }
        Web.redirect(req, resp, "/history");
    }
}
