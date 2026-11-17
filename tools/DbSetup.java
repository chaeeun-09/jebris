import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * J.E.B.R.I.S DB 설치 도구 (SQL*Plus 없이 JDBC로 실행)
 * 사용: java -cp ojdbc6.jar tools/DbSetup.java <SYSTEM 현재 비밀번호> [만료 시 바꿀 새 비밀번호]
 *  1) SYSTEM으로 jebris 계정 생성 (이미 있으면 건너뜀)
 *  2) jebris로 시퀀스·테이블·인덱스 생성 (기존 것은 지우고 새로 만듦)
 */
public class DbSetup {

    static final String URL = "jdbc:oracle:thin:@localhost:1521:XE";

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            fail("사용법: java DbSetup <SYSTEM 비밀번호> [새 비밀번호]");
        }
        String sysPw = args[0];
        String newPw = args.length > 1 ? args[1] : sysPw;
        Class.forName("oracle.jdbc.OracleDriver");

        System.out.println(" [1/2] SYSTEM 계정으로 jebris 계정을 만듭니다...");
        try (Connection c = connectSystem(sysPw, newPw);
             Statement st = c.createStatement()) {
            // 개발용 DB: 비밀번호가 180일마다 만료되지 않게 한다 (ORA-28001 재발 방지)
            run(st, "ALTER PROFILE DEFAULT LIMIT PASSWORD_LIFE_TIME UNLIMITED", true);
            run(st, "CREATE USER jebris IDENTIFIED BY jebris DEFAULT TABLESPACE users QUOTA UNLIMITED ON users", true);
            run(st, "ALTER USER jebris IDENTIFIED BY jebris ACCOUNT UNLOCK", true);
            run(st, "GRANT CONNECT, RESOURCE, CREATE SESSION, CREATE TABLE, CREATE SEQUENCE TO jebris", false);
        } catch (SQLException e) {
            fail("SYSTEM 로그인 실패: " + e.getMessage()
                    + "\n    - ORA-01017 이면 비밀번호가 틀린 거예요. setup_db.bat 의 SYS_PW 를 고치세요."
                    + "\n    - Connection refused / IO 오류면 서비스(services.msc)에서 OracleServiceXE, OracleXETNSListener 를 시작하세요.");
        }

        System.out.println(" [2/2] jebris 계정으로 테이블을 만듭니다...");
        try (Connection c = DriverManager.getConnection(URL, "jebris", "jebris");
             Statement st = c.createStatement()) {
            String[] drops = {
                "DROP TABLE CHAT_LOG CASCADE CONSTRAINTS PURGE",
                "DROP TABLE SCHEDULE CASCADE CONSTRAINTS PURGE",
                "DROP TABLE MEMBER CASCADE CONSTRAINTS PURGE",
                "DROP SEQUENCE seq_member",
                "DROP SEQUENCE seq_schedule",
                "DROP SEQUENCE seq_chat_log"
            };
            for (String sql : drops) {
                run(st, sql, true);
            }
            String[] creates = {
                "CREATE SEQUENCE seq_member START WITH 1 INCREMENT BY 1 NOCACHE",
                "CREATE SEQUENCE seq_schedule START WITH 1 INCREMENT BY 1 NOCACHE",
                "CREATE SEQUENCE seq_chat_log START WITH 1 INCREMENT BY 1 NOCACHE",
                "CREATE TABLE MEMBER ("
                    + " member_id NUMBER NOT NULL,"
                    + " login_id VARCHAR2(20 CHAR) NOT NULL,"
                    + " password VARCHAR2(200) NOT NULL,"
                    + " member_name VARCHAR2(20 CHAR) NOT NULL,"
                    + " voice VARCHAR2(2) DEFAULT 'M1' NOT NULL,"
                    + " created_at TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,"
                    + " CONSTRAINT pk_member PRIMARY KEY (member_id),"
                    + " CONSTRAINT uq_member_login UNIQUE (login_id))",
                "CREATE TABLE SCHEDULE ("
                    + " schedule_id NUMBER NOT NULL,"
                    + " member_id NUMBER NOT NULL,"
                    + " title VARCHAR2(100 CHAR) NOT NULL,"
                    + " start_at TIMESTAMP NOT NULL,"
                    + " memo VARCHAR2(500 CHAR),"
                    + " status VARCHAR2(10) DEFAULT 'PLANNED' NOT NULL,"
                    + " created_at TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,"
                    + " CONSTRAINT pk_schedule PRIMARY KEY (schedule_id),"
                    + " CONSTRAINT fk_schedule_member FOREIGN KEY (member_id) REFERENCES MEMBER (member_id) ON DELETE CASCADE,"
                    + " CONSTRAINT ck_schedule_status CHECK (status IN ('PLANNED', 'DONE', 'CANCELED')))",
                "CREATE TABLE CHAT_LOG ("
                    + " chat_id NUMBER NOT NULL,"
                    + " member_id NUMBER NOT NULL,"
                    + " question VARCHAR2(1000 CHAR) NOT NULL,"
                    + " answer VARCHAR2(1300 CHAR) NOT NULL,"
                    + " intent VARCHAR2(20) DEFAULT 'chat' NOT NULL,"
                    + " created_at TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,"
                    + " CONSTRAINT pk_chat_log PRIMARY KEY (chat_id),"
                    + " CONSTRAINT fk_chat_member FOREIGN KEY (member_id) REFERENCES MEMBER (member_id) ON DELETE CASCADE)",
                "CREATE INDEX idx_schedule_member_start ON SCHEDULE (member_id, start_at)",
                "CREATE INDEX idx_chat_member_created ON CHAT_LOG (member_id, created_at)"
            };
            for (String sql : creates) {
                run(st, sql, false);
            }
            try (ResultSet rs = st.executeQuery("SELECT table_name FROM user_tables ORDER BY table_name")) {
                StringBuilder sb = new StringBuilder();
                while (rs.next()) {
                    sb.append(rs.getString(1)).append(' ');
                }
                System.out.println("\n 완료! 만들어진 테이블: " + sb.toString().trim());
            }
        } catch (SQLException e) {
            fail("테이블 생성 실패: " + e.getMessage());
        }
    }

    /**
     * SYSTEM 로그인. 비밀번호가 만료(ORA-28001)됐으면 같은 비밀번호로 갱신하면서 다시 로그인한다.
     */
    static Connection connectSystem(String pw, String newPw) throws SQLException {
        // 이미 새 비밀번호로 바뀌어 있으면 그걸로 바로 로그인
        if (!newPw.equals(pw)) {
            try {
                return DriverManager.getConnection(URL, "system", newPw);
            } catch (SQLException ignored) {
                // 아직 안 바뀌었으면 현재 비밀번호로 시도
            }
        }
        try {
            return DriverManager.getConnection(URL, "system", pw);
        } catch (SQLException e) {
            if (e.getErrorCode() != 28001 && !String.valueOf(e.getMessage()).contains("ORA-28001")) {
                throw e;
            }
            System.out.println("    SYSTEM 비밀번호가 만료돼서 새 비밀번호로 바꿉니다...");
            Properties props = new Properties();
            props.setProperty("user", "system");
            props.setProperty("password", pw);
            props.setProperty("oracle.jdbc.newPassword", newPw);
            props.setProperty("OCINewPassword", newPw); // ojdbc6(11.2) 이름
            Connection c = DriverManager.getConnection(URL, props);
            System.out.println("    SYSTEM 비밀번호를 바꿨어요. 이제부터 SYSTEM 비밀번호는 새 비밀번호예요.");
            return c;
        }
    }

    /** ignoreError=true 면 '이미 있음/없음' 같은 오류를 무시한다. */
    static void run(Statement st, String sql, boolean ignoreError) throws SQLException {
        try {
            st.execute(sql);
        } catch (SQLException e) {
            if (!ignoreError) {
                throw e;
            }
        }
    }

    static void fail(String message) {
        System.out.println("\n !! " + message);
        System.exit(1);
    }
}
