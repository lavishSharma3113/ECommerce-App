package ECommerceDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    private final String jdbcUrl;
    private final String username;
    private final String password;

    public DatabaseManager(String jdbcUrl, String username, String password) {
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, username, password);
    }

    public boolean testConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = getConnection()) {
                return conn != null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

