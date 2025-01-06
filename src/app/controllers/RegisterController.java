package app.controllers;

import app.utils.DBConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RegisterController {

    @FXML
    private TextField usernameField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Label errorLabel;

    @FXML
    public void handleRegister() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validate input fields
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showError("Semua kolom harus diisi!");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Kata sandi dan konfirmasi kata sandi tidak cocok.");
            return;
        }

        // Simulate registration logic (add actual registration logic here)
        boolean registrationSuccess = registerUser(username, email, password);

        if (registrationSuccess) {
            showSuccess("Pendaftaran berhasil!", "Akun Anda telah berhasil dibuat.");
            navigateToLogin();
        } else {
            showError("Pendaftaran gagal. Coba lagi nanti.");
        }
    }

    private boolean registerUser(String username, String email, String password) {
        // Generate a default phone number value (optional, you can make it input as well)
        String phoneNumber = "Not Provided"; // or take it from a user input field if needed
        String role = "Visitor"; // Default role for a new user

        // SQL query to insert user data into the database
        String query = """
                INSERT INTO Users (name, email, password, phone_number, role)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);    // Set username
            stmt.setString(2, email);       // Set email
            stmt.setString(3, password);    // Set password (hashed in real-world apps)
            stmt.setString(4, phoneNumber); // Set phone number (can be empty)
            stmt.setString(5, role);        // Set user role

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0; // If rowsAffected is greater than 0, registration was successful

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Terjadi kesalahan saat menyimpan data. Coba lagi nanti.");
            return false; // Registration failed
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void showSuccess(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Method to navigate to the login page
    @FXML
    private void navigateToLogin() {
        try {
            // Close the current window (Registration form)
            Stage currentStage = (Stage) usernameField.getScene().getWindow();
            currentStage.close();

            // Load the login page FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/auth/login.fxml"));
            Parent loginRoot = loader.load();

            // Create a new stage (window) for the login form
            Stage loginStage = new Stage();
            loginStage.setTitle("Login");
            loginStage.setScene(new Scene(loginRoot));
            loginStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Gagal membuka halaman login.");
        }
    }
}
