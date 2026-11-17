package jebris.filter;

import java.io.IOException;
import java.util.Map;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jebris.util.Web;

/** 로그인이 필요한 주소를 보호한다. API는 401 JSON, 화면은 로그인 페이지로 보낸다. */
@WebFilter(urlPatterns = {"/main", "/schedule", "/history", "/mypage", "/api/*"})
public class LoginFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        if (Web.member(req) != null) {
            chain.doFilter(request, response);
            return;
        }
        if (req.getServletPath().startsWith("/api/")) {
            Web.json(resp, 401, Map.of("ok", false, "error", "로그인이 필요해요."));
        } else {
            Web.flash(req, "로그인이 필요해요.", "info");
            Web.redirect(req, resp, "/login");
        }
    }
}
