package jebris.util;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jebris.dto.Member;

/** 서블릿에서 자주 쓰는 처리 모음. */
public final class Web {

    public static final String LOGIN_MEMBER = "loginMember";

    private Web() {
    }

    public static Member member(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return session == null ? null : (Member) session.getAttribute(LOGIN_MEMBER);
    }

    /** /WEB-INF/views/{name}.jsp 로 포워드 */
    public static void view(HttpServletRequest req, HttpServletResponse resp, String name)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/" + name + ".jsp").forward(req, resp);
    }

    public static void redirect(HttpServletRequest req, HttpServletResponse resp, String path) throws IOException {
        resp.sendRedirect(req.getContextPath() + path);
    }

    /** 다음 화면에서 한 번 보여줄 알림. type: info, ok, error */
    public static void flash(HttpServletRequest req, String message, String type) {
        HttpSession session = req.getSession();
        session.setAttribute("flash", message);
        session.setAttribute("flashType", type);
    }

    public static void json(HttpServletResponse resp, int status, Object body) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Cache-Control", "no-store");
        resp.getWriter().write(Json.stringify(body));
    }

    public static String param(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        return v == null ? "" : v.trim();
    }

    public static long paramLong(HttpServletRequest req, String name, long defaultValue) {
        try {
            return Long.parseLong(param(req, name));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
