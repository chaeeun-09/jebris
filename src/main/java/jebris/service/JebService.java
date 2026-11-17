package jebris.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jebris.util.AppConfig;
import jebris.util.Json;

/**
 * 제브 AI Hub 호출.
 *  - flash-next : POST /v1/chat/completions (OpenAI Chat Completions 호환)
 *  - supertonic : POST /v1/audio/speech      (OpenAI Audio Speech 호환, mp3 응답)
 * API Key는 서버에서만 사용하고 브라우저로 보내지 않는다.
 */
public class JebService {

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final String baseUrl = trimSlash(AppConfig.get("jeb.baseUrl", "https://jeb.https.gsmsv.site/v1"));
    private final String apiKey = AppConfig.get("jeb.apiKey", "");
    private final String chatModel = AppConfig.get("jeb.chatModel", "flash-next");
    private final String ttsModel = AppConfig.get("jeb.ttsModel", "supertonic");

    /** LLM에 대화를 보내고 답변 텍스트(content)를 받는다. */
    public String chat(List<Map<String, Object>> messages, int maxTokens) throws JebException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", chatModel);
        body.put("messages", messages);
        body.put("max_tokens", maxTokens);
        body.put("temperature", 0.6);
        body.put("stream", false);
        // 생각(reasoning)을 끄면 빠르고 크레딧도 덜 든다.
        body.put("chat_template_kwargs", Map.of("enable_thinking", false));

        byte[] raw = post("/chat/completions", Json.stringify(body), Duration.ofSeconds(120));
        try {
            Map<String, Object> json = Json.asMap(Json.parse(new String(raw, StandardCharsets.UTF_8)));
            List<Object> choices = Json.asList(json.get("choices"));
            Map<String, Object> message = Json.asMap(Json.asMap(choices.get(0)).get("message"));
            String content = Json.str(message, "content");
            if (content == null || content.isBlank()) {
                throw new JebException(502, "제브가 빈 답변을 보냈어요. 다시 말씀해 주세요.");
            }
            return content.trim();
        } catch (RuntimeException e) {
            throw new JebException(502, "제브 응답을 해석하지 못했어요.");
        }
    }

    /** 텍스트를 음성(mp3)으로 바꾼다. voice: M1~M5, F1~F5 */
    public byte[] speak(String text, String voice) throws JebException {
        String input = text.length() > 2000 ? text.substring(0, 2000) : text;
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", ttsModel);
        body.put("input", input);
        body.put("voice", voice == null || voice.isEmpty() ? "M1" : voice);
        body.put("lang", "ko");
        body.put("speed", 1.1);
        body.put("response_format", "mp3");
        return post("/audio/speech", Json.stringify(body), Duration.ofSeconds(90));
    }

    private byte[] post(String path, String json, Duration timeout) throws JebException {
        if (apiKey.isEmpty() || apiKey.startsWith("YOUR_")) {
            throw new JebException(0, "제브 API Key가 없어요. config.properties의 jeb.apiKey를 채워 주세요.");
        }
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .timeout(timeout)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();
        HttpResponse<byte[]> response;
        try {
            response = CLIENT.send(request, HttpResponse.BodyHandlers.ofByteArray());
        } catch (HttpTimeoutException e) {
            throw new JebException(504, "제브 응답이 너무 늦어요. 잠시 후 다시 시도해 주세요.");
        } catch (IOException e) {
            throw new JebException(0, "제브 서버에 연결하지 못했어요. 인터넷 연결을 확인해 주세요.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new JebException(0, "요청이 중단되었어요.");
        }
        int status = response.statusCode();
        if (status != 200) {
            throw new JebException(status, friendlyError(status, response.body()));
        }
        return response.body();
    }

    private String friendlyError(int status, byte[] body) {
        String detail = "";
        try {
            Map<String, Object> json = Json.asMap(Json.parse(new String(body, StandardCharsets.UTF_8)));
            Map<String, Object> error = Json.asMap(json.get("error"));
            detail = error != null ? Json.str(error, "message") : Json.str(json, "detail");
        } catch (RuntimeException ignored) {
            // 본문이 JSON이 아니면 상태 코드만으로 안내한다.
        }
        switch (status) {
            case 401: return "제브 API Key가 올바르지 않거나 만료됐어요.";
            case 403: return "이 API Key에 필요한 권한(LLM/음성 합성)이 없어요.";
            case 429: return "제브 사용량을 다 썼거나 요청이 겹쳤어요. " + (detail == null ? "" : detail);
            case 502:
            case 503: return "제브 서버가 바빠요. 잠시 후 다시 시도해 주세요.";
            default: return "제브 오류(" + status + "): " + (detail == null ? "" : detail);
        }
    }

    private static String trimSlash(String s) {
        return s.endsWith("/") ? s.substring(0, s.length() - 1) : s;
    }
}
