package ECommerceDB;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DatabaseManagerTest {

    private static final String VALID_JDBC_URL = "jdbc:mysql://localhost:3306/testdb";
    private static final String VALID_USERNAME = "root";
    private static final String VALID_PASSWORD = "password";

    private static final String INVALID_JDBC_URL = "jdbc:mysql://localhost:3306/invalid";
    private DatabaseManager dbManager;

    @BeforeEach
    void setup() {
        dbManager = new DatabaseManager(VALID_JDBC_URL, VALID_USERNAME, VALID_PASSWORD);
    }

    @Test
    void testValidConnection() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
            mockedDriverManager.when(() -> DriverManager.getConnection(VALID_JDBC_URL, VALID_USERNAME, VALID_PASSWORD))
                    .thenReturn(mockConnection);

            Connection connection = dbManager.getConnection();
            assertNotNull(connection, "Connection should not be null with valid credentials.");
            mockedDriverManager.verify(() -> DriverManager.getConnection(VALID_JDBC_URL, VALID_USERNAME, VALID_PASSWORD), times(1));
        }
    }

    @Test
    void testInvalidConnection() throws SQLException {
        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
            mockedDriverManager.when(() -> DriverManager.getConnection(VALID_JDBC_URL, "invalid_user", "invalid_pass"))
                    .thenThrow(new SQLException("Invalid credentials"));

            DatabaseManager invalidDbManager = new DatabaseManager(VALID_JDBC_URL, "invalid_user", "invalid_pass");
            assertThrows(SQLException.class, invalidDbManager::getConnection, "SQLException should be thrown for invalid credentials.");
        }
    }

    @Test
    void testValidCredential() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        when(mockConnection.isValid(2)).thenReturn(true);

        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
            mockedDriverManager.when(() -> DriverManager.getConnection(VALID_JDBC_URL, VALID_USERNAME, VALID_PASSWORD))
                    .thenReturn(mockConnection);

            assertTrue(dbManager.testConnection(), "testConnection should return true for valid credentials.");
        }
    }

    @Test
    void testInvalidCredential() throws SQLException {
        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
            mockedDriverManager.when(() -> DriverManager.getConnection(VALID_JDBC_URL, "invalid_user", "invalid_pass"))
                    .thenThrow(new SQLException("Invalid credentials"));

            DatabaseManager invalidDbManager = new DatabaseManager(VALID_JDBC_URL, "invalid_user", "invalid_pass");
            assertFalse(invalidDbManager.testConnection(), "testConnection should return false for invalid credentials.");
        }
    }
}