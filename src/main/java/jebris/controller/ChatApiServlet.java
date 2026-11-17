package jebris.controller;

import java.io.IOException;
import java.util.Map;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jebris.dto.Member;
import jebris.service.AssistantReply;
import jebris.service.AssistantService;
import jebris.util.Web;

/**
 * 음성 인식 결과(텍스트)를 받아 제브리스 답변을 JSON으로 돌려준다.
 * 요청: text=질문, voice=true|false
 * 응답: {ok, answer, intent, audio(base64 mp3), ttsError, scheduleChanged, error}
 */
@WebServlet("/api/chat")
public class ChatApiServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final AssistantService assistant = new AssistantService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Member member = Web.member(req);
        String text = Web.param(req, "text");
        boolean wantVoice = "true".equals(req.getParameter("voice"));

        if (text.isEmpty()) {
            Web.json(resp, 400, Map.of("ok", false, "error", "질문이 비어 있어요."));
            return;
        }
        if (text.length() > 500) {
            Web.json(resp, 400, Map.of("ok", false, "error", "질문은 500자 이하로 말씀해 주세요."));
            return;
        }
        AssistantReply reply = assistant.handle(member, text, wantVoice);
        Web.json(resp, reply.isOk() ? 200 : 502, reply.toMap());
    }
}
