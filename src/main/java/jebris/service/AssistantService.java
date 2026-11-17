package jebris.service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jebris.dao.ChatLogDAO;
import jebris.dao.ScheduleDAO;
import jebris.dto.ChatLog;
import jebris.dto.Member;
import jebris.dto.Schedule;
import jebris.util.Json;

/**
 * 제브리스의 두뇌.
 * 1) 현재 시각, 사용자 일정, 최근 대화를 넣어 flash-next에 묻는다.
 * 2) LLM은 {"intent","answer","schedule"} JSON으로 답한다.
 * 3) 일정 등록 요청이면 검증 후 SCHEDULE에 저장한다.
 * 4) 대화를 CHAT_LOG에 저장하고, 원하면 supertonic으로 음성을 만든다.
 */
public class AssistantService {

    public static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    private static final DateTimeFormatter PROMPT_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd(E) HH:mm", Locale.KOREAN);
    private static final DateTimeFormatter LLM_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final int HISTORY_COUNT = 6;
    private static final int SCHEDULE_DAYS = 14;

    private final JebService jeb = new JebService();
    private final ScheduleDAO scheduleDAO = new ScheduleDAO();
    private final ChatLogDAO chatLogDAO = new ChatLogDAO();

    public AssistantReply handle(Member member, String text, boolean wantVoice) {
        LocalDateTime now = LocalDateTime.now(ZONE);
        List<Schedule> schedules;
        List<ChatLog> history;
        try {
            schedules = scheduleDAO.findBetween(member.getMemberId(),
                    now.toLocalDate().atStartOfDay(), now.toLocalDate().plusDays(SCHEDULE_DAYS + 1L).atStartOfDay());
            history = chatLogDAO.findRecent(member.getMemberId(), HISTORY_COUNT);
        } catch (SQLException e) {
            e.printStackTrace();
            return AssistantReply.fail("데이터베이스에 연결하지 못했어요. Oracle이 켜져 있는지 확인해 주세요.");
        }

        // 1) LLM 호출
        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(message("system", systemPrompt(member, now, schedules)));
        for (ChatLog c : history) {
            messages.add(message("user", c.getQuestion()));
            messages.add(message("assistant", c.getAnswer()));
        }
        messages.add(message("user", text));

        String content;
        try {
            content = jeb.chat(messages, 600);
        } catch (JebException e) {
            return AssistantReply.fail(e.getMessage());
        }

        // 2) 응답 해석
        AssistantReply reply = new AssistantReply();
        Map<String, Object> parsed = extractJson(content);
        if (parsed == null) {
            reply.setIntent("chat");
            reply.setAnswer(cleanForSpeech(content));
        } else {
            String intent = Json.str(parsed, "intent");
            String answer = Json.str(parsed, "answer");
            reply.setIntent(intent == null ? "chat" : intent);
            reply.setAnswer(cleanForSpeech(answer == null ? "" : answer));

            // 3) 일정 등록
            if ("add_schedule".equals(reply.getIntent())) {
                Schedule s = toSchedule(member.getMemberId(), Json.asMap(parsed.get("schedule")));
                if (s == null) {
                    reply.setIntent("chat");
                    reply.setAnswer("일정 내용을 정확히 알아듣지 못했어요. 날짜, 시간, 할 일을 다시 말씀해 주세요.");
                } else {
                    try {
                        scheduleDAO.insert(s);
                        reply.setScheduleChanged(true);
                    } catch (SQLException e) {
                        e.printStackTrace();
                        reply.setIntent("chat");
                        reply.setAnswer("일정을 저장하는 중에 문제가 생겼어요. 잠시 후 다시 시도해 주세요.");
                    }
                }
            } else if (!"query_schedule".equals(reply.getIntent())) {
                reply.setIntent("chat");
            }
        }
        if (reply.getAnswer().isEmpty()) {
            reply.setAnswer("죄송해요, 다시 한 번 말씀해 주시겠어요?");
        }

        // 4) 대화 저장
        try {
            ChatLog log = new ChatLog();
            log.setMemberId(member.getMemberId());
            log.setQuestion(limit(text, 1000));
            log.setAnswer(limit(reply.getAnswer(), 1300));
            log.setIntent(reply.getIntent());
            chatLogDAO.insert(log);
        } catch (SQLException e) {
            e.printStackTrace(); // 기록 저장 실패는 답변을 막지 않는다.
        }

        // 5) 음성 합성
        if (wantVoice) {
            try {
                byte[] mp3 = jeb.speak(reply.getAnswer(), member.getVoice());
                reply.setAudioBase64(Base64.getEncoder().encodeToString(mp3));
            } catch (JebException e) {
                reply.setTtsError(e.getMessage());
            }
        }
        return reply;
    }

    private String systemPrompt(Member member, LocalDateTime now, List<Schedule> schedules) {
        StringBuilder sb = new StringBuilder();
        sb.append("너는 '제브리스(J.E.B.R.I.S)'라는 한국어 음성 AI 비서다. ");
        sb.append("아이언맨의 자비스처럼 침착하고 정중하며 가끔 위트 있게 말한다. ");
        sb.append("사용자는 ").append(member.getMemberName()).append("님이다.\n");
        sb.append("현재 시각: ").append(now.format(PROMPT_TIME)).append(" (한국 시간)\n\n");

        sb.append("[사용자 일정: 오늘부터 ").append(SCHEDULE_DAYS).append("일]\n");
        if (schedules.isEmpty()) {
            sb.append("- 등록된 일정 없음\n");
        } else {
            for (Schedule s : schedules) {
                sb.append("- ").append(s.getStartAt().format(PROMPT_TIME)).append(' ').append(s.getTitle());
                if (s.getMemo() != null && !s.getMemo().isEmpty()) {
                    sb.append(" (메모: ").append(s.getMemo()).append(')');
                }
                sb.append("DONE".equals(s.getStatus()) ? " [완료]" : " [예정]").append('\n');
            }
        }

        sb.append("\n반드시 아래 형식의 JSON 한 개만 출력한다. 코드 블록이나 다른 글자는 쓰지 않는다.\n");
        sb.append("{\"intent\":\"chat 또는 add_schedule 또는 query_schedule\",\"answer\":\"사용자에게 말할 답변\",");
        sb.append("\"schedule\":null 또는 {\"title\":\"할 일\",\"start_at\":\"YYYY-MM-DD HH:mm\",\"memo\":\"\"}}\n\n");
        sb.append("규칙:\n");
        sb.append("1. answer는 소리 내어 읽힌다. 1~3문장, 150자 이내로 짧게. 이모지, 마크다운, 목록 기호, 괄호 설명을 쓰지 않는다.\n");
        sb.append("2. 일정 추가·등록·잡아줘·기억해줘 요청이면 intent는 add_schedule이고 schedule을 채운다. ");
        sb.append("'내일', '이번 주 금요일', '저녁 7시' 같은 표현은 현재 시각을 기준으로 실제 날짜와 24시간제 시각으로 바꾼다. ");
        sb.append("시각이 없으면 09:00으로 한다. answer에는 등록한 날짜와 내용을 짧게 확인해 준다.\n");
        sb.append("3. 무엇을 할지(제목)를 알 수 없으면 add_schedule 대신 chat으로 무엇을 등록할지 되묻는다.\n");
        sb.append("4. 일정을 물어보면 intent는 query_schedule이고, 위 일정 목록만 근거로 답한다. 목록에 없으면 없다고 말한다.\n");
        sb.append("5. 그 밖의 대화는 intent를 chat, schedule을 null로 한다. 모르는 사실은 지어내지 않는다.\n");
        return sb.toString();
    }

    /** LLM이 앞뒤에 다른 글자나 코드 블록을 붙여도 첫 { ~ 마지막 } 만 읽는다. */
    private Map<String, Object> extractJson(String content) {
        int start = content.indexOf('{');
        int end = content.lastIndexOf('}');
        if (start < 0 || end <= start) {
            return null;
        }
        try {
            return Json.asMap(Json.parse(content.substring(start, end + 1)));
        } catch (RuntimeException e) {
            return null;
        }
    }

    /** LLM이 준 일정 정보를 검증해서 Schedule로 만든다. 잘못되면 null. */
    private Schedule toSchedule(long memberId, Map<String, Object> data) {
        if (data == null) {
            return null;
        }
        String title = Json.str(data, "title");
        String startAt = Json.str(data, "start_at");
        String memo = Json.str(data, "memo");
        if (title == null || title.isBlank() || title.length() > 100 || startAt == null) {
            return null;
        }
        LocalDateTime when = parseDateTime(startAt);
        if (when == null) {
            return null;
        }
        Schedule s = new Schedule();
        s.setMemberId(memberId);
        s.setTitle(title.trim());
        s.setStartAt(when);
        s.setMemo(memo == null || memo.isBlank() ? null : limit(memo.trim(), 500));
        s.setStatus("PLANNED");
        return s;
    }

    static LocalDateTime parseDateTime(String value) {
        String v = value.trim().replace('T', ' ');
        try {
            if (v.length() == 10) {
                return LocalDate.parse(v).atTime(9, 0);
            }
            if (v.length() > 16) {
                v = v.substring(0, 16);
            }
            return LocalDateTime.parse(v, LLM_TIME);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static Map<String, Object> message(String role, String content) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("role", role);
        m.put("content", content);
        return m;
    }

    private static String cleanForSpeech(String s) {
        return s.replaceAll("[*#`_>]", "").replaceAll("\\s+", " ").trim();
    }

    private static String limit(String s, int max) {
        return s.length() > max ? s.substring(0, max) : s;
    }
}
