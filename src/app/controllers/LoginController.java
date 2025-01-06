package app.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import app.utils.DBConnection;
import app.utils.SessionManager;

import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private Button togglePasswordVisibility;

    @FXML
    private VBox loginContainer;

    private boolean passwordVisible = false;

    @FXML
    public void initialize() {
        errorLabel.setText("");
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Username dan password wajib diisi");
        } else {
            UserLoginResult result = verifyLoginFromDatabase(username, password);
            if (result != null) {
                errorLabel.setText(""); // Bersihkan pesan error

                // Simpan user ID dan role ke SessionManager
                SessionManager.getInstance().setCurrentUserId(result.getUserId());
                SessionManager.getInstance().setCurrentRole(result.getRole());
                SessionManager.getInstance().setCurrentUsername(username);

                // Navigasi ke dashboard
                navigateToDashboard(result.getRole());
            } else {
                errorLabel.setText("Username atau password salah");
            }
        }
    }

    private UserLoginResult verifyLoginFromDatabase(String username, String password) {
        UserLoginResult result = null;
        try (Connection connection = DBConnection.getConnection()) {
            String query = "SELECT user_id, role FROM users WHERE name = ? AND password = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int userId = resultSet.getInt("user_id");
                String role = resultSet.getString("role");
                result = new UserLoginResult(userId, role);
            }
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Terjadi kesalahan saat mengakses database.");
        }
        return result;
    }

    private void navigateToDashboard(String role) {
        try {
            FXMLLoader loader;
            String dashboardPath;

            // Tentukan path dashboard berdasarkan role
            if ("admin".equalsIgnoreCase(role)) {
                dashboardPath = "/resources/fxml/admin/dashboard.fxml";
            } else {
                dashboardPath = "/resources/fxml/user/visitor_overview.fxml";
            }

            loader = new FXMLLoader(getClass().getResource(dashboardPath));
            Parent root = loader.load();

            // Dapatkan controller yang sesuai
            Object controller = loader.getController();

            // Set welcome message jika method tersedia
            if (controller instanceof DashboardController) {
                ((DashboardController) controller).setWelcomeMessage(role);
            } else if (controller instanceof OverviewController) {
                // Jika menggunakan OverviewController untuk user
                // Tambahkan method setWelcomeMessage di OverviewController
                Method setWelcomeMethod = controller.getClass().getMethod("setWelcomeMessage", String.class);
                setWelcomeMethod.invoke(controller, role);
            }

            // Ganti scene
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(role.equals("admin") ? "Admin Dashboard" : "User Dashboard");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Gagal membuka dashboard. " + e.getMessage());
        }
    }


    @FXML
    public void navigateToRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/auth/register.fxml"));
            Parent registerRoot = loader.load();

            Stage registerStage = new Stage();
            registerStage.setTitle("Register");
            registerStage.setScene(new Scene(registerRoot));
            registerStage.show();

            Stage currentStage = (Stage) loginContainer.getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Gagal membuka halaman pendaftaran.");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void togglePasswordVisibility(MouseEvent event) {
        passwordVisible = !passwordVisible;
        passwordField.setVisible(!passwordVisible);
        togglePasswordVisibility.setText(passwordVisible ? "🙈" : "👁");
    }

    // Class untuk menyimpan hasil login
    private static class UserLoginResult {
        private final int userId;
        private final String role;

        public UserLoginResult(int userId, String role) {
            this.userId = userId;
            this.role = role;
        }

        public int getUserId() {
            return userId;
        }

        public String getRole() {
            return role;
        }
    }
}
