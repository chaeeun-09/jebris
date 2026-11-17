package jebris.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Oracle Database 연결을 만든다. 사용한 연결은 try-with-resources로 닫는다. */
public final class DBUtil {

    private static final String URL = AppConfig.get("db.url", "jdbc:oracle:thin:@localhost:1521:XE");
    private static final String USER = AppConfig.get("db.user", "jebris");
    private static final String PASSWORD = AppConfig.get("db.password", "jebris");

    static {
        try {
            Class.forName("oracle.jdbc.OracleDriver");
        } catch (ClassNotFoundException e) {
            System.err.println("[JEBRIS] Oracle JDBC 드라이버(ojdbc*.jar)를 WEB-INF/lib에 넣어 주세요.");
        }
    }

    private DBUtil() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
