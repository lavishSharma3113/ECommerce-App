package Session;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
public class UserSessionTest {
    @Test
    void testSetUser() {
        UserSession session = new UserSession();
        session.setUser(1, "testUser");

        assertEquals(1, session.getUserId(), "User ID should be set correctly.");
        assertEquals("testUser", session.getUsername(), "Username should be set correctly.");
        assertTrue(session.isAuthenticated(), "User should be authenticated after setting.");
    }

    @Test
    void testClearSession() {
        UserSession session = new UserSession();
        session.setUser(1, "testUser");
        session.clearSession();

        assertEquals(-1, session.getUserId(), "User ID should be reset to -1 after clearing session.");
        assertNull(session.getUsername(), "Username should be null after clearing session.");
        assertFalse(session.isAuthenticated(), "User should not be authenticated after clearing session.");
    }

    @Test
    void testGetUserId() {
        UserSession session = new UserSession();
        session.setUser(42, "exampleUser");

        assertEquals(42, session.getUserId(), "getUserId should return the correct user ID.");
    }

    @Test
    void testGetUsername() {
        UserSession session = new UserSession();
        session.setUser(42, "exampleUser");

        assertEquals("exampleUser", session.getUsername(), "getUsername should return the correct username.");
    }

    @Test
    void testIsAuthenticated() {
        UserSession session = new UserSession();
        assertFalse(session.isAuthenticated(), "User should not be authenticated initially.");

        session.setUser(42, "exampleUser");
        assertTrue(session.isAuthenticated(), "User should be authenticated after setting.");
    }
}
