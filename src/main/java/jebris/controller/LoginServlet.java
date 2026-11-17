package jebris.controller;

import java.io.IOException;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jebris.dao.MemberDAO;
import jebris.dto.Member;
import jebris.util.PasswordUtil;
import jebris.util.Web;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final MemberDAO memberDAO = new MemberDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (Web.member(req) != null) {
            Web.redirect(req, resp, "/main");
            return;
        }
        Web.view(req, resp, "login");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String loginId = Web.param(req, "loginId");
        String password = req.getParameter("password");
        req.setAttribute("loginId", loginId);

        if (loginId.isEmpty() || password == null || password.isEmpty()) {
            req.setAttribute("error", "아이디와 비밀번호를 입력해 주세요.");
            Web.view(req, resp, "login");
            return;
        }
        try {
            Member member = memberDAO.findByLoginId(loginId);
            if (member == null || !PasswordUtil.verify(password, member.getPassword())) {
                req.setAttribute("error", "아이디 또는 비밀번호가 맞지 않아요.");
                Web.view(req, resp, "login");
                return;
            }
            // 세션 고정 공격을 막기 위해 로그인할 때 세션을 새로 만든다.
            HttpSession old = req.getSession(false);
            if (old != null) {
                old.invalidate();
            }
            member.setPassword(null);
            req.getSession(true).setAttribute(Web.LOGIN_MEMBER, member);
            Web.flash(req, member.getMemberName() + "님, 다시 오신 걸 환영해요.", "ok");
            Web.redirect(req, resp, "/main");
        } catch (SQLException e) {
            e.printStackTrace();
            req.setAttribute("error", "데이터베이스에 연결하지 못했어요. Oracle이 켜져 있는지 확인해 주세요.");
            Web.view(req, resp, "login");
        }
    }
}
