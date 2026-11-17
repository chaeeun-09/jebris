package jebris.dto;

import java.time.LocalDateTime;

/** CHAT_LOG 테이블 한 행. intent: chat, add_schedule, query_schedule */
public class ChatLog {

    private long chatId;
    private long memberId;
    private String question;
    private String answer;
    private String intent;
    private LocalDateTime createdAt;

    public long getChatId() { return chatId; }
    public void setChatId(long chatId) { this.chatId = chatId; }

    public long getMemberId() { return memberId; }
    public void setMemberId(long memberId) { this.memberId = memberId; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public String getIntent() { return intent; }
    public void setIntent(String intent) { this.intent = intent; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
