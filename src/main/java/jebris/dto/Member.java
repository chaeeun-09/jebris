package jebris.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/** MEMBER 테이블 한 행 */
public class Member implements Serializable {

    private static final long serialVersionUID = 1L;

    private long memberId;
    private String loginId;
    private String password;
    private String memberName;
    private String voice;
    private LocalDateTime createdAt;

    public long getMemberId() { return memberId; }
    public void setMemberId(long memberId) { this.memberId = memberId; }

    public String getLoginId() { return loginId; }
    public void setLoginId(String loginId) { this.loginId = loginId; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }

    public String getVoice() { return voice; }
    public void setVoice(String voice) { this.voice = voice; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
