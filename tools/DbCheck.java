import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/** jebris 계정으로 접속해 테이블 3개가 있는지 확인한다. 있으면 0, 없으면 1로 끝난다. */
public class DbCheck {
    public static void main(String[] args) {
        try {
            Class.forName("oracle.jdbc.OracleDriver");
            try (Connection c = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:XE", "jebris", "jebris");
                 Statement st = c.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM user_tables WHERE table_name IN ('MEMBER','SCHEDULE','CHAT_LOG')")) {
                rs.next();
                int n = rs.getInt(1);
                System.out.println(" DB 확인: jebris 테이블 " + n + "/3");
                System.exit(n == 3 ? 0 : 1);
            }
        } catch (Exception e) {
            System.out.println(" DB 확인 실패: " + e.getMessage());
            System.exit(1);
        }
    }
}
