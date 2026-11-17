package jebris.dto;

import java.time.LocalDateTime;

/** SCHEDULE 테이블 한 행. status: PLANNED(예정), DONE(완료), CANCELED(취소) */
public class Schedule {

    private long scheduleId;
    private long memberId;
    private String title;
    private LocalDateTime startAt;
    private String memo;
    private String status;
    private LocalDateTime createdAt;

    public long getScheduleId() { return scheduleId; }
    public void setScheduleId(long scheduleId) { this.scheduleId = scheduleId; }

    public long getMemberId() { return memberId; }
    public void setMemberId(long memberId) { this.memberId = memberId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public LocalDateTime getStartAt() { return startAt; }
    public void setStartAt(LocalDateTime startAt) { this.startAt = startAt; }

    public String getMemo() { return memo; }
    public void setMemo(String memo) { this.memo = memo; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    /** 예정 상태인데 시간이 지난 일정 */
    public boolean isOverdue() {
        return "PLANNED".equals(status) && startAt != null && startAt.isBefore(LocalDateTime.now());
    }
}
