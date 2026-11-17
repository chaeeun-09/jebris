package jebris.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import jebris.dto.Member;
import jebris.util.DBUtil;

/** MEMBER 테이블 CRUD */
public class MemberDAO {

    private static final String COLUMNS = "member_id, login_id, password, member_name, voice, created_at";

    public long insert(Member m) throws SQLException {
        String sql = "INSERT INTO MEMBER (member_id, login_id, password, member_name, voice) VALUES (seq_member.NEXTVAL, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, new String[] {"member_id"})) {
            ps.setString(1, m.getLoginId());
            ps.setString(2, m.getPassword());
            ps.setString(3, m.getMemberName());
            ps.setString(4, m.getVoice());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getLong(1) : 0L;
            }
        }
    }

    public Member findByLoginId(String loginId) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM MEMBER WHERE login_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, loginId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public Member findById(long memberId) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM MEMBER WHERE member_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public boolean existsLoginId(String loginId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM MEMBER WHERE login_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, loginId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public int updateProfile(long memberId, String memberName, String voice) throws SQLException {
        String sql = "UPDATE MEMBER SET member_name = ?, voice = ? WHERE member_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, memberName);
            ps.setString(2, voice);
            ps.setLong(3, memberId);
            return ps.executeUpdate();
        }
    }

    public int updatePassword(long memberId, String passwordHash) throws SQLException {
        String sql = "UPDATE MEMBER SET password = ? WHERE member_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, passwordHash);
            ps.setLong(2, memberId);
            return ps.executeUpdate();
        }
    }

    /** 회원 탈퇴. 일정과 대화 기록은 FK의 ON DELETE CASCADE로 함께 지워진다. */
    public int delete(long memberId) throws SQLException {
        String sql = "DELETE FROM MEMBER WHERE member_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, memberId);
            return ps.executeUpdate();
        }
    }

    private Member map(ResultSet rs) throws SQLException {
        Member m = new Member();
        m.setMemberId(rs.getLong("member_id"));
        m.setLoginId(rs.getString("login_id"));
        m.setPassword(rs.getString("password"));
        m.setMemberName(rs.getString("member_name"));
        m.setVoice(rs.getString("voice"));
        Timestamp created = rs.getTimestamp("created_at");
        m.setCreatedAt(created == null ? null : created.toLocalDateTime());
        return m;
    }
}
