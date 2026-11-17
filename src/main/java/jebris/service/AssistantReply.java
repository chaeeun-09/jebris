package jebris.service;

import java.util.LinkedHashMap;
import java.util.Map;

/** /api/chat 응답 내용 */
public class AssistantReply {

    private boolean ok = true;
    private String answer;
    private String intent = "chat";
    private String audioBase64;
    private String ttsError;
    private boolean scheduleChanged;
    private String error;

    public static AssistantReply fail(String message) {
        AssistantReply r = new AssistantReply();
        r.ok = false;
        r.intent = "error";
        r.error = message;
        r.answer = message;
        return r;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("ok", ok);
        map.put("answer", answer);
        map.put("intent", intent);
        map.put("audio", audioBase64);
        map.put("ttsError", ttsError);
        map.put("scheduleChanged", scheduleChanged);
        map.put("error", error);
        return map;
    }

    public boolean isOk() { return ok; }
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
    public String getIntent() { return intent; }
    public void setIntent(String intent) { this.intent = intent; }
    public void setAudioBase64(String audioBase64) { this.audioBase64 = audioBase64; }
    public void setTtsError(String ttsError) { this.ttsError = ttsError; }
    public void setScheduleChanged(boolean scheduleChanged) { this.scheduleChanged = scheduleChanged; }
}
