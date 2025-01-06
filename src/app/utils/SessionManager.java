package app.utils;

public class SessionManager {
    private static SessionManager instance;
    private Integer currentUserId; // Menyimpan User ID
    private String currentRole; // Menyimpan Role User
    private String currentUsername; // Tambahkan ini


    private SessionManager() {
        currentUserId = null;
        currentRole = null;
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public String getCurrentUsername() {
        return currentUsername;
    }


    public void setCurrentUsername(String username) {
        this.currentUsername = username;
    }


    public Integer getCurrentUserId() {
        return currentUserId;
    }

    public void setCurrentUserId(Integer userId) {
        this.currentUserId = userId;
    }

    public String getCurrentRole() {
        return currentRole;
    }

    public void setCurrentRole(String role) {
        this.currentRole = role;
    }

    public void logout() {
        currentUserId = null;
        currentRole = null;
    }
}
