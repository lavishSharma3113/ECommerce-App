package Session;

public class UserSession {
    private int userId;
    private String username;
    private boolean authenticated;

    public UserSession() {
        this.authenticated = false;
    }

    public void setUser(int userId, String username) {
        this.userId = userId;
        this.username = username;
        this.authenticated = true;
    }

    public void clearSession() {
        this.userId = -1;
        this.username = null;
        this.authenticated = false;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }
}
