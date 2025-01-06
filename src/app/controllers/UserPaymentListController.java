package app.controllers;

import app.utils.DBConnection;
import app.utils.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;

public class UserPaymentListController {

    @FXML
    private VBox paymentContainer;


    @FXML
    public void initialize() {
        loadPayments();
    }



    private void loadPayments() {
        int userId = SessionManager.getInstance().getCurrentUserId();
        String query = """
    SELECT b.booking_id,
           d.name AS destination_name,
           b.booking_date,
           b.total_amount,
           b.status AS booking_status
    FROM Bookings b
    JOIN Packages pkg ON b.package_id = pkg.package_id
    JOIN Destinations d ON pkg.destination_id = d.destination_id
    WHERE b.user_id = ? AND b.status = 'Pending'
    ORDER BY b.booking_date DESC
""";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                VBox bookingCard = createPendingBookingCard(
                        rs.getInt("booking_id"),
                        rs.getString("destination_name"),
                        rs.getDate("booking_date").toLocalDate(),
                        rs.getBigDecimal("total_amount"),
                        rs.getString("booking_status")
                );
                paymentContainer.getChildren().add(bookingCard);
                if (paymentContainer.getChildren().isEmpty()) {
                    Label emptyStateLabel = new Label("No payments found");
                    emptyStateLabel.getStyleClass().add("empty-state-label");
                    paymentContainer.getChildren().add(emptyStateLabel);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error",
                    "Gagal memuat data booking pending: " + e.getMessage());
        }
    }

    private VBox createPendingBookingCard(int bookingId, String destinationName,
                                          LocalDate bookingDate, BigDecimal totalAmount,
                                          String status) {
        VBox bookingCard = new VBox();
        bookingCard.getStyleClass().add("booking-card");

        HBox bookingInfo = new HBox();
        bookingInfo.getStyleClass().add("booking-info");
        bookingInfo.setSpacing(15);

        // Destination
        VBox destinationBox = new VBox();
        destinationBox.getStyleClass().add("info-section");
        Label destinationLabel = new Label("Destination:");
        destinationLabel.getStyleClass().add("info-label");
        Label destinationText = new Label(destinationName);
        destinationText.getStyleClass().add("info-text");
        destinationBox.getChildren().addAll(destinationLabel, destinationText);

        // Booking Date
        VBox dateBox = new VBox();
        dateBox.getStyleClass().add("info-section");
        Label dateLabel = new Label("Booking Date:");
        dateLabel.getStyleClass().add("info-label");
        Label dateText = new Label(bookingDate.toString());
        dateText.getStyleClass().add("info-text");
        dateBox.getChildren().addAll(dateLabel, dateText);

        // Total Amount
        VBox amountBox = new VBox();
        amountBox.getStyleClass().add("info-section");
        Label amountLabel = new Label("Total Amount:");
        amountLabel.getStyleClass().add("info-label");
        Label amountText = new Label(formatCurrency(totalAmount));
        amountText.getStyleClass().add("price-text");
        amountBox.getChildren().addAll(amountLabel, amountText);

        // Status
        VBox statusBox = new VBox();
        statusBox.getStyleClass().add("info-section");
        Label statusLabel = new Label("Status:");
        statusLabel.getStyleClass().add("info-label");
        Label statusText = new Label(status);
        statusText.getStyleClass().add("status-badge status-" + status.toLowerCase());
        statusBox.getChildren().addAll(statusLabel, statusText);

        // Action Buttons
        HBox actionBox = new HBox();
        actionBox.setSpacing(10);
        Button payButton = new Button("Pay Now");
        payButton.getStyleClass().add("book-now-button");
        payButton.setOnAction(event -> processPayment(bookingId));

        Button cancelButton = new Button("Cancel Booking");
        cancelButton.getStyleClass().add("cancel-button");
        cancelButton.setOnAction(event -> cancelBooking(bookingId));

        actionBox.getChildren().addAll(payButton, cancelButton);

        bookingInfo.getChildren().addAll(destinationBox, dateBox, amountBox, statusBox);
        bookingCard.getChildren().addAll(bookingInfo, actionBox);

        return bookingCard;
    }

    private void processPayment(int bookingId) {
        try {
            // Insert payment record
            String insertPaymentQuery = """
        INSERT INTO Payments (booking_id, payment_date, payment_amount, payment_method, status) 
        SELECT booking_id, CURRENT_TIMESTAMP, total_amount, 'Credit Card', 'Paid'
        FROM Bookings 
        WHERE booking_id = ?
    """;

            // Update booking status
            String updateBookingQuery = "UPDATE Bookings SET status = 'Confirmed' WHERE booking_id = ?";

            try (Connection conn = DBConnection.getConnection()) {
                // Disable auto-commit for transaction
                conn.setAutoCommit(false);

                // Insert payment
                try (PreparedStatement paymentStmt = conn.prepareStatement(insertPaymentQuery)) {
                    paymentStmt.setInt(1, bookingId);
                    paymentStmt.executeUpdate();
                }

                // Update booking status
                try (PreparedStatement bookingStmt = conn.prepareStatement(updateBookingQuery)) {
                    bookingStmt.setInt(1, bookingId);
                    bookingStmt.executeUpdate();
                }

                // Commit transaction
                conn.commit();

                // Refresh booking list
                Platform.runLater(() -> {
                    paymentContainer.getChildren().clear();
                    loadPayments();
                    showAlert(Alert.AlertType.INFORMATION, "Payment Successful",
                            "Pembayaran berhasil dilakukan.");
                });

            } catch (SQLException e) {
                // Rollback transaction in case of error
                throw e;
            }

        } catch (SQLException e) {
            Platform.runLater(() ->
                    showAlert(Alert.AlertType.ERROR, "Payment Error",
                            "Gagal memproses pembayaran: " + e.getMessage())
            );
        }
    }

    private void cancelBooking(int bookingId) {
        try {
            String updateQuery = "UPDATE Bookings SET status = 'Canceled' WHERE booking_id = ?";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(updateQuery)) {

                stmt.setInt(1, bookingId);
                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    Platform.runLater(() -> {
                        paymentContainer.getChildren().clear();
                        loadPayments();
                        showAlert(Alert.AlertType.INFORMATION, "Booking Canceled",
                                "Booking berhasil dibatalkan.");
                    });
                }
            }
        } catch (SQLException e) {
            Platform.runLater(() ->
                    showAlert(Alert.AlertType.ERROR, "Cancellation Error",
                            "Gagal membatalkan booking: " + e.getMessage())
            );
        }
    }

    private void openPaymentForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/path/to/payment_form.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Make Payment");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open payment form: " + e.getMessage());
        }
    }

    // Metode bantuan untuk format mata uang
    private String formatCurrency(BigDecimal amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        return formatter.format(amount);
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}