package Server;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ECommerceDB.DatabaseManager;
import ECommerceDB.SQLQueryManager;
import Session.UserSession;





class NetworkAuthenticationServiceTest {

    @Mock
    private DatabaseManager mockDbManager;

    @Mock
    private SQLQueryManager mockQueryManager;

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private ResultSet mockResultSet;

    private NetworkAuthenticationService authService;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        authService = new NetworkAuthenticationService(mockDbManager);
        when(mockDbManager.getConnection()).thenReturn(mockConnection);
    }

    @Test
    void testLogin_Success() throws SQLException {
        String username = "testUser";
        String password = "testPass";
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";

        when(mockQueryManager.getQuery("QUERY_USER_LOGIN")).thenReturn(query);
        when(mockConnection.prepareStatement(query)).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("user_id")).thenReturn(1);
        when(mockResultSet.getString("username")).thenReturn(username);

        boolean result = authService.login(username, password);

        assertTrue(result);
        verify(mockPreparedStatement).setString(1, username);
        verify(mockPreparedStatement).setString(2, password);
    }

    @Test
    void testLogin_InvalidCredentials() throws SQLException {
        String username = "testUser";
        String password = "wrongPass";
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";

        when(mockQueryManager.getQuery("QUERY_USER_LOGIN")).thenReturn(query);
        when(mockConnection.prepareStatement(query)).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        boolean result = authService.login(username, password);

        assertFalse(result);
    }

    @Test
    void testLogin_SQLException() throws SQLException {
        String username = "testUser";
        String password = "testPass";
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";

        when(mockQueryManager.getQuery("QUERY_USER_LOGIN")).thenReturn(query);
        when(mockConnection.prepareStatement(query)).thenThrow(new SQLException());

        boolean result = authService.login(username, password);

        assertFalse(result);
    }

    @Test
    void testSignUp_Success() throws SQLException {
        String username = "newUser";
        String password = "newPass";
        String email = "newUser@example.com";
        String query = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";

        when(mockQueryManager.getQuery("QUERY_USER_SIGNUP")).thenReturn(query);
        when(mockConnection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(1);

        NetworkAuthenticationService.SignUpResult result = authService.signUp(username, password, email);

        assertEquals(NetworkAuthenticationService.SignUpResult.SUCCESS, result);
    }

    @Test
    void testSignUp_UsernameTaken() throws SQLException {
        String username = "existingUser";
        String password = "newPass";
        String email = "newUser@example.com";

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);

        NetworkAuthenticationService.SignUpResult result = authService.signUp(username, password, email);

        assertEquals(NetworkAuthenticationService.SignUpResult.USERNAME_TAKEN, result);
    }

    @Test
    void testSignUp_SQLException() throws SQLException {
        String username = "newUser";
        String password = "newPass";
        String email = "newUser@example.com";
        String query = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";

        when(mockQueryManager.getQuery("QUERY_USER_SIGNUP")).thenReturn(query);
        when(mockConnection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)).thenThrow(new SQLException());

        NetworkAuthenticationService.SignUpResult result = authService.signUp(username, password, email);

        assertEquals(NetworkAuthenticationService.SignUpResult.ERROR, result);
    }

    @Test
    void testIsUsernameExists_True() throws SQLException {
        String username = "existingUser";
        String query = "SELECT * FROM users WHERE username = ?";

        when(mockQueryManager.getQuery("QUERY_CHECK_USERNAME")).thenReturn(query);
        when(mockConnection.prepareStatement(query)).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);

        boolean result = authService.isUsernameExists(username);

        assertTrue(result);
    }

    @Test
    void testIsUsernameExists_False() throws SQLException {
        String username = "nonExistingUser";
        String query = "SELECT * FROM users WHERE username = ?";

        when(mockQueryManager.getQuery("QUERY_CHECK_USERNAME")).thenReturn(query);
        when(mockConnection.prepareStatement(query)).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        boolean result = authService.isUsernameExists(username);

        assertFalse(result);
    }

    @Test
    void testIsUsernameExists_SQLException() throws SQLException {
        String username = "testUser";
        String query = "SELECT * FROM users WHERE username = ?";

        when(mockQueryManager.getQuery("QUERY_CHECK_USERNAME")).thenReturn(query);
        when(mockConnection.prepareStatement(query)).thenThrow(new SQLException());

        boolean result = authService.isUsernameExists(username);

        assertFalse(result);
    }
}