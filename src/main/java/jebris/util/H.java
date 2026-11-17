package jebris.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** JSP 화면 출력용 도우미 (HTML 이스케이프, 날짜 형식, 라벨). */
public final class H {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy.MM.dd (E) HH:mm", Locale.KOREAN);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("M월 d일 (E)", Locale.KOREAN);
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter INPUT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private H() {
    }

    /** HTML 이스케이프. JSP에서 사용자 입력을 출력할 때 반드시 사용한다. */
    public static String e(Object value) {
        if (value == null) {
            return "";
        }
        String s = value.toString();
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '<': sb.append("&lt;"); break;
                case '>': sb.append("&gt;"); break;
                case '&': sb.append("&amp;"); break;
                case '"': sb.append("&quot;"); break;
                case '\'': sb.append("&#39;"); break;
                default: sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String dateTime(LocalDateTime t) {
        return t == null ? "" : t.format(DATE_TIME);
    }

    public static String date(LocalDateTime t) {
        return t == null ? "" : t.format(DATE);
    }

    public static String time(LocalDateTime t) {
        return t == null ? "" : t.format(TIME);
    }

    /** input type="datetime-local" 값 */
    public static String input(LocalDateTime t) {
        return t == null ? "" : t.format(INPUT);
    }

    public static String statusLabel(String status) {
        if ("DONE".equals(status)) {
            return "완료";
        }
        if ("CANCELED".equals(status)) {
            return "취소";
        }
        return "예정";
    }

    public static String intentLabel(String intent) {
        if ("add_schedule".equals(intent)) {
            return "일정 등록";
        }
        if ("query_schedule".equals(intent)) {
            return "일정 조회";
        }
        return "대화";
    }

    public static String voiceLabel(String voice) {
        if (voice == null || voice.length() != 2) {
            return voice;
        }
        return (voice.charAt(0) == 'F' ? "여성 " : "남성 ") + voice.charAt(1);
    }

    public static String selected(boolean on) {
        return on ? " selected" : "";
    }
}
