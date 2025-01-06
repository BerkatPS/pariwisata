package app.controllers;

import app.utils.DBConnection;
import app.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Locale;

public class UserDashboardController {
    @FXML
    private StackPane contentArea;
    @FXML private Label welcomeLabel;
    @FXML private Label totalBookingsLabel;
    @FXML private Label upcomingTripsLabel;
    @FXML private Label totalSpentLabel;
    @FXML private HBox featuredDestinationsContainer;
    @FXML private TableView recentBookingsTable;
    @FXML
    private BorderPane rootPane;

    @FXML
    public void initialize() {
        welcomeLabel.setText("Welcome, " + SessionManager.getInstance().getCurrentUsername() + "!");
        loadUserStats();
        loadFeaturedDestinations();
        loadRecentBookings();
    }

    private void loadUserStats() {
        try (Connection connection = DBConnection.getConnection()) {
            String query = "SELECT COUNT(*) AS totalBookings, SUM(total_amount) AS totalSpent " +
                    "FROM Bookings WHERE user_id = ? AND status = 'Confirmed'";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, SessionManager.getInstance().getCurrentUserId());
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                // Format total bookings
                int totalBookings = resultSet.getInt("totalBookings");
                totalBookingsLabel.setText(String.valueOf(totalBookings));

                // Format total spent dalam Rupiah
                double totalSpent = resultSet.getDouble("totalSpent");

                // Menggunakan NumberFormat untuk format Rupiah
                NumberFormat rupiahFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
                totalSpentLabel.setText(rupiahFormat.format(totalSpent));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadFeaturedDestinations() {
        // Load featured destinations from the database and add them to the featuredDestinationsContainer
        // This is a placeholder for the actual implementation
    }

    private void loadRecentBookings() {
        // Load recent bookings from the database and populate the recentBookingsTable
        // This is a placeholder for the actual implementation
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/auth/login.fxml"));
            Parent loginRoot = loader.load();

            Scene loginScene = new Scene(loginRoot);
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(loginScene);
            stage.setTitle("Login - Travel Booking App");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Failed to load login page.");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Method to load the overview page
    public void loadOverview() {
        loadPage("/resources/fxml/user/visitor_overview.fxml");
    }

    // Method to load the destinations page
    public void loadDestinations() {
        loadPage("/resources/fxml/user/visitor_destinations.fxml");
    }

    // Method to load the bookings page
    public void loadBookings() {
        loadPage("/resources/fxml/user/visitor_bookings.fxml");
    }

    // Method to load the transport management page
    public void loadTransport() {
        loadPage("TransportManagement.fxml");
    }

    // Method to load the reviews page
    public void loadReviews() {
        loadPage("/resources/fxml/user/visitor_review.fxml");
    }

    // Method to load the payment management page
    public void loadPayments() {
        loadPage("/resources/fxml/user/visitor_payments.fxml");
    }

    // Method to load the account settings page
    public void loadAccountSettings() {
        loadPage("AccountSettings.fxml");
    }

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
}