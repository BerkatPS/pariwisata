package app.controllers;

import app.utils.DBConnection;
import app.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

public class UserReviewFormController {

    @FXML private ComboBox<BookingDTO> unreviewedDestinationsComboBox;
    @FXML private ComboBox<String> ratingComboBox;
    @FXML private TextArea reviewTextArea;
    @FXML private Button submitReviewButton;

    @FXML
    public void initialize() {
        // Inisialisasi ComboBox rating
        ratingComboBox.getItems().addAll("1", "2", "3", "4", "5");

        // Memuat daftar pemesanan yang belum direview
        loadUnreviewedBookings();
    }

    private void loadUnreviewedBookings() {
        int userId = SessionManager.getInstance().getCurrentUserId();
        List<BookingDTO> unreviewedBookings = fetchUnreviewedBookings(userId);

        unreviewedDestinationsComboBox.getItems().clear();
        unreviewedDestinationsComboBox.getItems().addAll(unreviewedBookings);
    }

    private List<BookingDTO> fetchUnreviewedBookings(int userId) {
        List<BookingDTO> bookings = new ArrayList<>();
        String query = """
            SELECT 
                b.booking_id,
                d.destination_id,
                d.name AS destination_name
            FROM Bookings b
            JOIN Packages pkg ON b.package_id = pkg.package_id
            JOIN Destinations d ON pkg.destination_id = d.destination_id
            LEFT JOIN Reviews r ON d.destination_id = r.destination_id AND r.user_id = ?
            WHERE b.user_id = ? AND b.status = 'Confirmed' AND r.review_id IS NULL
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int bookingId = rs.getInt("booking_id");
                int destinationId = rs.getInt("destination_id");
                String destinationName = rs.getString("destination_name");
                bookings.add(new BookingDTO(bookingId, destinationId, destinationName));
            }

        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Database Error",
                    "Failed to load unreviewed bookings: " + e.getMessage());
        }
        return bookings;
    }

    @FXML
    private void handleSubmitReview() {
        // Validasi input
        BookingDTO selectedBooking = unreviewedDestinationsComboBox.getValue();
        String rating = ratingComboBox.getValue();
        String reviewText = reviewTextArea.getText().trim();

        if (selectedBooking == null) {
            showAlert(AlertType.WARNING, "Input Error", "Please select a booking.");
            return;
        }

        if (rating == null) {
            showAlert(AlertType.WARNING, "Input Error", "Please select a rating.");
            return;
        }

        if (reviewText.isEmpty()) {
            showAlert(AlertType.WARNING, "Input Error", "Please write a review.");
            return;
        }

        // Simpan review
        saveReview(selectedBooking, rating, reviewText);
    }

    private void saveReview(BookingDTO booking, String rating, String reviewText) {
        String insertReviewQuery = """
            INSERT INTO Reviews (
                user_id, 
                destination_id, 
                rating, 
                review_text
            ) VALUES (?, ?, ?, ?)
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertReviewQuery)) {

            int userId = SessionManager.getInstance().getCurrentUserId();

            stmt.setInt(1, userId);
            stmt.setInt(2, booking.getDestinationId());
            stmt.setInt(3, Integer.parseInt(rating));
            stmt.setString(4, reviewText);

            stmt.executeUpdate();

            // Bersihkan form setelah berhasil
            clearForm();

            // Muat ulang daftar pemesanan yang belum direview
            loadUnreviewedBookings();

            showAlert(AlertType.INFORMATION, "Success", "Review submitted successfully!");

        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Database Error",
                    "Failed to submit review: " + e.getMessage());
        }
    }

    private void clearForm() {
        unreviewedDestinationsComboBox.setValue(null);
        ratingComboBox.setValue(null);
        reviewTextArea.clear();
    }

    private void showAlert(AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class BookingDTO {
        private int bookingId;
        private int destinationId;
        private String destinationName;

        public BookingDTO(int bookingId, int destinationId, String destinationName) {
            this.bookingId = bookingId;
            this.destinationId = destinationId;
            this.destinationName = destinationName;
        }

        public int getBookingId() {
            return bookingId;
        }

        public int getDestinationId() {
            return destinationId;
        }

        public String getDestinationName() {
            return destinationName;
        }

        @Override
        public String toString() {
            return destinationName; // Menampilkan nama tujuan di ComboBox
        }
    }

}