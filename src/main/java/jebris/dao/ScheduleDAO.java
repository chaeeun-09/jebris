package jebris.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jebris.dto.Schedule;
import jebris.util.DBUtil;

/** SCHEDULE 테이블 CRUD. 모든 조회·수정은 member_id 조건을 붙여 본인 일정만 다룬다. */
public class ScheduleDAO {

    private static final String COLUMNS = "schedule_id, member_id, title, start_at, memo, status, created_at";

    public long insert(Schedule s) throws SQLException {
        String sql = "INSERT INTO SCHEDULE (schedule_id, member_id, title, start_at, memo, status) VALUES (seq_schedule.NEXTVAL, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, new String[] {"schedule_id"})) {
            ps.setLong(1, s.getMemberId());
            ps.setString(2, s.getTitle());
            ps.setTimestamp(3, Timestamp.valueOf(s.getStartAt()));
            ps.setString(4, s.getMemo());
            ps.setString(5, s.getStatus() == null ? "PLANNED" : s.getStatus());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getLong(1) : 0L;
            }
        }
    }

    /** 검색어(제목·메모)와 상태로 목록 조회. 빈 값이면 조건을 쓰지 않는다. */
    public List<Schedule> findByMember(long memberId, String keyword, String status) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT " + COLUMNS + " FROM SCHEDULE WHERE member_id = ?");
        boolean hasKeyword = keyword != null && !keyword.isEmpty();
        boolean hasStatus = status != null && !status.isEmpty();
        if (hasKeyword) {
            sql.append(" AND (LOWER(title) LIKE ? ESCAPE '\\' OR LOWER(memo) LIKE ? ESCAPE '\\')");
        }
        if (hasStatus) {
            sql.append(" AND status = ?");
        }
        sql.append(" ORDER BY start_at ASC, schedule_id ASC");

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int i = 1;
            ps.setLong(i++, memberId);
            if (hasKeyword) {
                String like = "%" + escapeLike(keyword.toLowerCase()) + "%";
                ps.setString(i++, like);
                ps.setString(i++, like);
            }
            if (hasStatus) {
                ps.setString(i++, status);
            }
            return list(ps);
        }
    }

    /** 기간 안의 예정·완료 일정 (취소 제외) */
    public List<Schedule> findBetween(long memberId, LocalDateTime from, LocalDateTime to) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM SCHEDULE WHERE member_id = ? AND status <> 'CANCELED'"
                + " AND start_at >= ? AND start_at < ? ORDER BY start_at ASC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, memberId);
            ps.setTimestamp(2, Timestamp.valueOf(from));
            ps.setTimestamp(3, Timestamp.valueOf(to));
            return list(ps);
        }
    }

    public Schedule findById(long memberId, long scheduleId) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM SCHEDULE WHERE member_id = ? AND schedule_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, memberId);
            ps.setLong(2, scheduleId);
            List<Schedule> result = list(ps);
            return result.isEmpty() ? null : result.get(0);
        }
    }

    public int update(Schedule s) throws SQLException {
        String sql = "UPDATE SCHEDULE SET title = ?, start_at = ?, memo = ?, status = ?"
                + " WHERE member_id = ? AND schedule_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getTitle());
            ps.setTimestamp(2, Timestamp.valueOf(s.getStartAt()));
            ps.setString(3, s.getMemo());
            ps.setString(4, s.getStatus());
            ps.setLong(5, s.getMemberId());
            ps.setLong(6, s.getScheduleId());
            return ps.executeUpdate();
        }
    }

    public int updateStatus(long memberId, long scheduleId, String status) throws SQLException {
        String sql = "UPDATE SCHEDULE SET status = ? WHERE member_id = ? AND schedule_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setLong(2, memberId);
            ps.setLong(3, scheduleId);
            return ps.executeUpdate();
        }
    }

    public int delete(long memberId, long scheduleId) throws SQLException {
        String sql = "DELETE FROM SCHEDULE WHERE member_id = ? AND schedule_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, memberId);
            ps.setLong(2, scheduleId);
            return ps.executeUpdate();
        }
    }

    private List<Schedule> list(PreparedStatement ps) throws SQLException {
        List<Schedule> result = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Schedule s = new Schedule();
                s.setScheduleId(rs.getLong("schedule_id"));
                s.setMemberId(rs.getLong("member_id"));
                s.setTitle(rs.getString("title"));
                s.setStartAt(rs.getTimestamp("start_at").toLocalDateTime());
                s.setMemo(rs.getString("memo"));
                s.setStatus(rs.getString("status"));
                Timestamp created = rs.getTimestamp("created_at");
                s.setCreatedAt(created == null ? null : created.toLocalDateTime());
                result.add(s);
            }
        }
        return result;
    }

    static String escapeLike(String s) {
        return s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
