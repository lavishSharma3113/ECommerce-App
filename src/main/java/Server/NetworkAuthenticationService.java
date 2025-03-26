package Server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import ECommerceDB.DatabaseManager;
import ECommerceDB.SQLQueryManager;
import Session.UserSession;

public class NetworkAuthenticationService {
    public final DatabaseManager dbManager;
    public final UserSession session;

    public NetworkAuthenticationService(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        this.session = new UserSession();
    }

    public boolean login(String username, String password) {
        SQLQueryManager queryManager = new SQLQueryManager();
        String query = queryManager.getQuery("QUERY_USER_LOGIN");
        System.out.println("the query is "+query);
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     query)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int userId = rs.getInt("user_id");
                    String name = rs.getString("username");
                    session.setUser(userId, name);
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public enum SignUpResult {
        SUCCESS,
        USERNAME_TAKEN,
        ERROR
    }

    public SignUpResult signUp(String username, String password, String email) {

        if (isUsernameExists(username)) {
            return SignUpResult.USERNAME_TAKEN;
        }

        SQLQueryManager queryManager = new SQLQueryManager();
        String query = queryManager.getQuery("QUERY_USER_SIGNUP");
        System.out.println("The signup query is " + query);

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, email);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int userId = generatedKeys.getInt(1);
                        session.setUser(userId, username);
                        return SignUpResult.SUCCESS;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return SignUpResult.ERROR;
        }

        return SignUpResult.ERROR;
    }

    private boolean isUsernameExists(String username) {
        SQLQueryManager queryManager = new SQLQueryManager();
        String query = queryManager.getQuery("QUERY_CHECK_USERNAME");
        System.out.println("Checking username existence: " + query);

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }


    public UserSession getCurrentSession() {
        return session;
    }
}

