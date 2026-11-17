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

/** 메인(제브리스 HUD) 화면. 최근 대화 10건을 미리 보여준다. 일정은 /api/today로 불러온다. */
@WebServlet("/main")
public class MainServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final ChatLogDAO chatLogDAO = new ChatLogDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Member member = Web.member(req);
        try {
            req.setAttribute("recentChats", chatLogDAO.findRecent(member.getMemberId(), 10));
        } catch (SQLException e) {
            e.printStackTrace();
            req.setAttribute("recentChats", Collections.emptyList());
            req.setAttribute("dbError", true);
        }
        Web.view(req, resp, "main");
    }
}
