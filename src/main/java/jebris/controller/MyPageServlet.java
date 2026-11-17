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

/** 내 정보: 이름·목소리 변경, 비밀번호 변경, 회원 탈퇴 */
@WebServlet("/mypage")
public class MyPageServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final MemberDAO memberDAO = new MemberDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Web.view(req, resp, "mypage");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Member session = Web.member(req);
        String action = Web.param(req, "action");
        try {
            Member db = memberDAO.findById(session.getMemberId());
            if (db == null) {
                req.getSession().invalidate();
                Web.redirect(req, resp, "/login");
                return;
            }
            if ("profile".equals(action)) {
                String name = Web.param(req, "memberName");
                String voice = Web.param(req, "voice");
                if (name.isEmpty() || name.length() > 20) {
                    Web.flash(req, "이름은 1~20자로 입력해 주세요.", "error");
                } else if (!JoinServlet.isVoice(voice)) {
                    Web.flash(req, "목소리를 선택해 주세요.", "error");
                } else {
                    memberDAO.updateProfile(db.getMemberId(), name, voice);
                    session.setMemberName(name);
                    session.setVoice(voice);
                    Web.flash(req, "내 정보를 저장했어요.", "ok");
                }
            } else if ("password".equals(action)) {
                String current = req.getParameter("currentPassword");
                String next = req.getParameter("newPassword");
                String confirm = req.getParameter("newPasswordConfirm");
                if (!PasswordUtil.verify(current, db.getPassword())) {
                    Web.flash(req, "현재 비밀번호가 맞지 않아요.", "error");
                } else if (next == null || next.length() < 8 || next.length() > 64) {
                    Web.flash(req, "새 비밀번호는 8~64자로 입력해 주세요.", "error");
                } else if (!next.equals(confirm)) {
                    Web.flash(req, "새 비밀번호 확인이 일치하지 않아요.", "error");
                } else {
                    memberDAO.updatePassword(db.getMemberId(), PasswordUtil.hash(next));
                    Web.flash(req, "비밀번호를 바꿨어요.", "ok");
                }
            } else if ("withdraw".equals(action)) {
                if (!PasswordUtil.verify(req.getParameter("password"), db.getPassword())) {
                    Web.flash(req, "비밀번호가 맞지 않아 탈퇴하지 않았어요.", "error");
                } else {
                    memberDAO.delete(db.getMemberId());
                    req.getSession().invalidate();
                    Web.flash(req, "탈퇴가 끝났어요. 그동안 제브리스를 써 주셔서 고마워요.", "info");
                    Web.redirect(req, resp, "/login");
                    return;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Web.flash(req, "데이터베이스 오류로 처리하지 못했어요.", "error");
        }
        Web.redirect(req, resp, "/mypage");
    }
}
