package jebris.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jebris.dto.ChatLog;
import jebris.util.DBUtil;

/** CHAT_LOG 테이블 CRUD */
public class ChatLogDAO {

    private static final String COLUMNS = "chat_id, member_id, question, answer, intent, created_at";

    public long insert(ChatLog c) throws SQLException {
        String sql = "INSERT INTO CHAT_LOG (chat_id, member_id, question, answer, intent) VALUES (seq_chat_log.NEXTVAL, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, new String[] {"chat_id"})) {
            ps.setLong(1, c.getMemberId());
            ps.setString(2, c.getQuestion());
            ps.setString(3, c.getAnswer());
            ps.setString(4, c.getIntent());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getLong(1) : 0L;
            }
        }
    }

    /** 최근 대화 n건을 오래된 순서로 돌려준다 (LLM 대화 맥락용). */
    public List<ChatLog> findRecent(long memberId, int count) throws SQLException {
        String sql = "SELECT * FROM (SELECT " + COLUMNS + " FROM CHAT_LOG WHERE member_id = ?"
                + " ORDER BY created_at DESC, chat_id DESC) WHERE ROWNUM <= ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, memberId);
            ps.setInt(2, count);
            List<ChatLog> result = list(ps);
            Collections.reverse(result);
            return result;
        }
    }

    /** 대화 기록 화면용: 최신순, 최대 200건, 검색어(질문·답변) */
    public List<ChatLog> findByMember(long memberId, String keyword) throws SQLException {
        boolean hasKeyword = keyword != null && !keyword.isEmpty();
        String inner = "SELECT " + COLUMNS + " FROM CHAT_LOG WHERE member_id = ?"
                + (hasKeyword ? " AND (LOWER(question) LIKE ? ESCAPE '\\' OR LOWER(answer) LIKE ? ESCAPE '\\')" : "")
                + " ORDER BY created_at DESC, chat_id DESC";
        String sql = "SELECT * FROM (" + inner + ") WHERE ROWNUM <= 200";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, memberId);
            if (hasKeyword) {
                String like = "%" + ScheduleDAO.escapeLike(keyword.toLowerCase()) + "%";
                ps.setString(2, like);
                ps.setString(3, like);
            }
            return list(ps);
        }
    }

    public int delete(long memberId, long chatId) throws SQLException {
        String sql = "DELETE FROM CHAT_LOG WHERE member_id = ? AND chat_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, memberId);
            ps.setLong(2, chatId);
            return ps.executeUpdate();
        }
    }

    public int deleteAll(long memberId) throws SQLException {
        String sql = "DELETE FROM CHAT_LOG WHERE member_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, memberId);
            return ps.executeUpdate();
        }
    }

    private List<ChatLog> list(PreparedStatement ps) throws SQLException {
        List<ChatLog> result = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ChatLog c = new ChatLog();
                c.setChatId(rs.getLong("chat_id"));
                c.setMemberId(rs.getLong("member_id"));
                c.setQuestion(rs.getString("question"));
                c.setAnswer(rs.getString("answer"));
                c.setIntent(rs.getString("intent"));
                Timestamp created = rs.getTimestamp("created_at");
                c.setCreatedAt(created == null ? null : created.toLocalDateTime());
                result.add(c);
            }
        }
        return result;
    }
}
