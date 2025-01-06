package app.controllers;

import app.utils.DBConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.io.IOException;

public class OverviewController {

    @FXML private Button btnOverview, btnBooking, btnDestinations, btnPayments, btnReviews, btnTransport;
    @FXML private Label totalBookings, totalRevenue, totalReviews, averageRating;
    @FXML private TabPane tabPane;
    @FXML private Label welcomeLabel; // Pastikan Anda memiliki Label ini di FXML


    @FXML
    public void initialize() {
//        loadStatistics();
    }


    public void setWelcomeMessage(String role) {
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + role + "!");
        }
    }



    private void loadStatistics() {
        try {
            // Query untuk total bookings
            String bookingQuery = "SELECT COUNT(*) FROM bookings";
            PreparedStatement bookingStmt = DBConnection.getConnection().prepareStatement(bookingQuery);
            ResultSet bookingRs = bookingStmt.executeQuery();
            if (bookingRs.next()) {
                totalBookings.setText(String.valueOf(bookingRs.getInt(1)));
            }

            // Query untuk total revenue
            String revenueQuery = "SELECT SUM(payment_amount) FROM payments";
            PreparedStatement revenueStmt = DBConnection.getConnection().prepareStatement(revenueQuery);
            ResultSet revenueRs = revenueStmt.executeQuery();
            if (revenueRs.next()) {
                totalRevenue.setText("$" + revenueRs.getDouble(1));
            }

            // Query untuk total reviews
            String reviewsQuery = "SELECT COUNT(*) FROM reviews";
            PreparedStatement reviewsStmt = DBConnection.getConnection().prepareStatement(reviewsQuery);
            ResultSet reviewsRs = reviewsStmt.executeQuery();
            if (reviewsRs.next()) {
                totalReviews.setText(String.valueOf(reviewsRs.getInt(1)));
            }

            // Query untuk rata-rata rating
            String ratingQuery = "SELECT AVG(rating) FROM reviews";
            PreparedStatement ratingStmt = DBConnection.getConnection().prepareStatement(ratingQuery);
            ResultSet ratingRs = ratingStmt.executeQuery();
            if (ratingRs.next()) {
                averageRating.setText(String.format("%.1f", ratingRs.getDouble(1)));
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Gagal memuat statistik.");
        }
    }

    @FXML
    private void goToOverview() {
        navigateTo("visitor_overview.fxml");
    }

    @FXML
    private void goToBooking() {
        navigateTo("visitor_booking.fxml");
    }

    @FXML
    private void goToDestinations() {
        navigateTo("visitor_destinations.fxml");
    }

    @FXML
    private void goToPayments() {
        navigateTo("visitor_payments.fxml");
    }

    @FXML
    private void goToReviews() {
        navigateTo("visitor_review.fxml");
    }

    @FXML
    private void goToTransport() {
        navigateTo("visitor_transport.fxml");
    }

    private void navigateTo(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user/" + fxmlFile));
            Parent root = loader.load();
            Stage stage = (Stage) btnOverview.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Gagal navigasi ke " + fxmlFile);
        }
    }
}
