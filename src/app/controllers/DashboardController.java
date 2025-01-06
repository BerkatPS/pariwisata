package app.controllers;

import app.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;

public class DashboardController {

    @FXML
    private BorderPane rootPane; // Root pane dari BorderPane di FXML
    @FXML
    private Label welcomeLabel; // Label untuk menampilkan pesan selamat datang

    private String currentRole; // Role saat ini untuk menentukan akses pengguna
    @FXML
    private Button dashboardButton;
    @FXML
    private Button analyticsButton;
    @FXML
    private Button destinationsButton;
    @FXML
    private Button packagesButton;
    @FXML
    private Button bookingsButton;
    @FXML
    private Button paymentsButton;
    @FXML
    private Button transportButton;

    @FXML
    public void initialize() {
        // Set icons for each button
        setButtonIcon(dashboardButton, "/resources/icons/dashboard.png");
        setButtonIcon(analyticsButton, "/resources/icons/placeholder.png");
        setButtonIcon(destinationsButton, "/resources/icons/destination.png");
        setButtonIcon(packagesButton, "/resources/icons/list.png");
        setButtonIcon(bookingsButton, "/resources/icons/bookings.png");
        setButtonIcon(paymentsButton, "/resources/icons/payment-method.png");
        setButtonIcon(transportButton, "/resources/icons/transport.png");
        // Set welcome message
        currentRole = SessionManager.getInstance().getCurrentRole();
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + currentRole + "!");
        }

    }

    @FXML
    public void logout() {
        try {
            SessionManager.getInstance().logout();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/auth/login.fxml"));
            Parent loginPane = loader.load();

            // Dapatkan stage saat ini
            Stage currentStage = (Stage) rootPane.getScene().getWindow();

            // Buat scene baru dengan halaman login
            Scene loginScene = new Scene(loginPane);

            // Set scene baru ke stage yang ada
            currentStage.setScene(loginScene);
            currentStage.setTitle("Login - Travel Booking App");
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setButtonIcon(Button button, String iconPath) {
        try {
            ImageView icon = new ImageView(new Image(getClass().getResource(iconPath).toExternalForm()));
            icon.setFitHeight(24);
            icon.setFitWidth(24);
            button.setGraphic(icon);
        } catch (Exception e) {
            System.err.println("Error loading icon: " + iconPath);
            e.printStackTrace();
        }
    }
    // Metode untuk set welcome message dan role
    public void setWelcomeMessage(String role) {
        currentRole = SessionManager.getInstance().getCurrentRole();
        welcomeLabel.setText("Welcome, " + role + "!");

        if (welcomeLabel != null && currentRole != null) {
            welcomeLabel.setText("Welcome, " + currentRole + "!");
        } else if (welcomeLabel == null) {
            System.out.println("Error: welcomeLabel is null. Ensure it is properly linked in the FXML file.");
        } else {
            System.out.println("Error: currentRole is null. Ensure the user is logged in.");
        }
    }

    // Metode untuk memuat halaman dashboard
    public void showDashboard() {
        if ("admin".equalsIgnoreCase(currentRole)) {
            loadPage("/resources/fxml/admin/dashboard.fxml");
        } else if ("visitor".equalsIgnoreCase(currentRole)) {
            loadPage("/resources/fxml/user/visitor_dashboard.fxml");
        } else {
            showAccessDenied();
        }
    }

    // Metode untuk memuat halaman analytics
    public void showAnalytics() {
        if ("admin".equalsIgnoreCase(currentRole)) {
            loadPage("/resources/fxml/admin/analytics.fxml");
        } else {
            showAccessDenied();
        }
    }

    // Metode untuk memuat halaman user management
    @FXML
    public void showUserManagement() {
        if ("admin".equalsIgnoreCase(currentRole)) {
            loadPage("/resources/fxml/admin/user_management.fxml");
        } else {
            showAccessDenied();
        }
    }

    // Metode untuk memuat halaman destination management
    @FXML
    public void showDestinationManagement() {
        if ("admin".equalsIgnoreCase(currentRole)) {
            loadPage("/resources/fxml/admin/destinations.fxml");
        } else if ("visitor".equalsIgnoreCase(currentRole)) {
            loadPage("/resources/fxml/user/visitor_destinations.fxml");
        } else {
            showAccessDenied();
        }
    }

    // Metode untuk memuat halaman package management
    @FXML
    public void showPackageManagement() {
        if ("admin".equalsIgnoreCase(currentRole)) {
            loadPage("/resources/fxml/admin/packages.fxml");
        } else if ("visitor".equalsIgnoreCase(currentRole)) {
            loadPage("/resources/fxml/user/visitor_packages.fxml");
        } else {
            showAccessDenied();
        }
    }

    // Metode untuk memuat halaman booking management
    @FXML
    public void showBookingManagement() {
        loadPage("/resources/fxml/admin/bookings.fxml");
    }

    // Metode untuk memuat halaman payment management
    @FXML
    public void showPaymentManagement() {
        if ("admin".equalsIgnoreCase(currentRole)) {
            loadPage("/resources/fxml/admin/payments.fxml");
        } else {
            showAccessDenied();
        }
    }

    // Metode untuk memuat halaman transport management
    @FXML
    public void showTransportManagement() {
        if ("admin".equalsIgnoreCase(currentRole)) {
            loadPage("/resources/fxml/admin/transport.fxml");
        } else if ("visitor".equalsIgnoreCase(currentRole)) {
            loadPage("/resources/fxml/user/visitor_transport.fxml");
        } else {
            showAccessDenied();
        }
    }

    // Metode untuk memuat halaman ke dalam bagian center dari BorderPane
    private void loadPage(String page) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(page));
            Parent pane = loader.load();
            rootPane.setCenter(pane); // Ganti konten di bagian center dari BorderPane
        } catch (IOException e) {
            e.printStackTrace();
            showError("Halaman tidak ditemukan: " + page);
        }
    }

    // Menampilkan pesan error jika halaman tidak bisa diakses
    private void showAccessDenied() {
        showError("Anda tidak memiliki akses ke halaman ini.");
    }

    // Menampilkan dialog error
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Access Denied");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
