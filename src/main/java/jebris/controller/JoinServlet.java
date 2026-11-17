package jebris.controller;

import java.io.IOException;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jebris.dao.MemberDAO;
import jebris.dto.Member;
import jebris.util.PasswordUtil;
import jebris.util.Web;

@WebServlet("/join")
public class JoinServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    static final String VOICES = "M1 M2 M3 M4 M5 F1 F2 F3 F4 F5";
    private final MemberDAO memberDAO = new MemberDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Web.view(req, resp, "join");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String loginId = Web.param(req, "loginId");
        String name = Web.param(req, "memberName");
        String voice = Web.param(req, "voice");
        String password = req.getParameter("password");
        String confirm = req.getParameter("passwordConfirm");
        req.setAttribute("loginId", loginId);
        req.setAttribute("memberName", name);
        req.setAttribute("voice", voice);

        String error = validate(loginId, name, voice, password, confirm);
        if (error != null) {
            req.setAttribute("error", error);
            Web.view(req, resp, "join");
            return;
        }
        try {
            if (memberDAO.existsLoginId(loginId)) {
                req.setAttribute("error", "이미 사용 중인 아이디예요.");
                Web.view(req, resp, "join");
                return;
            }
            Member m = new Member();
            m.setLoginId(loginId);
            m.setPassword(PasswordUtil.hash(password));
            m.setMemberName(name);
            m.setVoice(voice);
            memberDAO.insert(m);
            Web.flash(req, "가입이 끝났어요. 로그인해 주세요.", "ok");
            Web.redirect(req, resp, "/login");
        } catch (SQLException e) {
            e.printStackTrace();
            req.setAttribute("error", "가입 중 데이터베이스 오류가 생겼어요.");
            Web.view(req, resp, "join");
        }
    }

    static String validate(String loginId, String name, String voice, String password, String confirm) {
        if (!loginId.matches("[A-Za-z0-9_]{4,20}")) {
            return "아이디는 영문, 숫자, _ 로 4~20자여야 해요.";
        }
        if (name.isEmpty() || name.length() > 20) {
            return "이름은 1~20자로 입력해 주세요.";
        }
        if (!isVoice(voice)) {
            return "목소리를 선택해 주세요.";
        }
        if (password == null || password.length() < 8 || password.length() > 64) {
            return "비밀번호는 8~64자로 입력해 주세요.";
        }
        if (!password.equals(confirm)) {
            return "비밀번호 확인이 일치하지 않아요.";
        }
        return null;
    }

    static boolean isVoice(String voice) {
        return voice != null && voice.length() == 2 && VOICES.contains(voice);
    }
}
