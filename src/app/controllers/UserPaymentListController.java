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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserPaymentListController {

    @FXML private VBox paymentContainer;
    @FXML private Label pendingCountLabel;
    @FXML private Label totalPendingAmountLabel;
    @FXML private Label confirmedCountLabel;
    @FXML private Label totalConfirmedAmountLabel;

    private static final NumberFormat CURRENCY_FORMATTER =
            NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    @FXML
    public void initialize() {
        loadPaymentStatistics();
        loadPayments();
    }

    private void loadPaymentStatistics() {
        int userId = SessionManager.getInstance().getCurrentUserId();
        String statisticsQuery = """
            SELECT 
                SUM(CASE WHEN b.status = 'Pending' THEN 1 ELSE 0 END) AS pending_count,
                SUM(CASE WHEN b.status = 'Pending' THEN b.total_amount ELSE 0 END) AS total_pending_amount,
                SUM(CASE WHEN b.status = 'Confirmed' THEN 1 ELSE 0 END) AS confirmed_count,
                SUM(CASE WHEN b.status = 'Confirmed' THEN b.total_amount ELSE 0 END) AS total_confirmed_amount
            FROM Bookings b
            WHERE b.user_id = ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(statisticsQuery)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int pendingCount = rs.getInt("pending_count");
                BigDecimal totalPendingAmount = rs.getBigDecimal("total_pending_amount");
                int confirmedCount = rs.getInt("confirmed_count");
                BigDecimal totalConfirmedAmount = rs.getBigDecimal("total_confirmed_amount");

                Platform.runLater(() -> {
                    pendingCountLabel.setText(String.valueOf(pendingCount));
                    totalPendingAmountLabel.setText(formatCurrency(totalPendingAmount));
                    confirmedCountLabel.setText(String.valueOf(confirmedCount));
                    totalConfirmedAmountLabel.setText(formatCurrency(totalConfirmedAmount));
                });
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error",
                    "Gagal memuat statistik pembayaran: " + e.getMessage());
        }
    }

    private void loadPayments() {
        // Gunakan ExecutorService untuk menjalankan tugas database di background
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            int userId = SessionManager.getInstance().getCurrentUserId();
            List<BookingPaymentDTO> payments = fetchPaymentsFromDatabase(userId);

            // Gunakan Platform.runLater untuk update UI
            Platform.runLater(() -> {
                updatePaymentContainer(payments);
            });

            // Shutdown executor service
            executorService.shutdown();
        });
    }

    // Data Transfer Object untuk menyimpan data pembayaran
    private static class BookingPaymentDTO {
        int bookingId;
        String destinationName;
        LocalDate bookingDate;
        BigDecimal totalAmount;
        String bookingStatus;
        boolean isPaid;

        public BookingPaymentDTO(int bookingId, String destinationName, LocalDate bookingDate,
                                 BigDecimal totalAmount, String bookingStatus, boolean isPaid) {
            this.bookingId = bookingId;
            this.destinationName = destinationName;
            this.bookingDate = bookingDate;
            this.totalAmount = totalAmount;
            this.bookingStatus = bookingStatus;
            this.isPaid = isPaid;
        }
    }

    private List<BookingPaymentDTO> fetchPaymentsFromDatabase(int userId) {
        List<BookingPaymentDTO> payments = new ArrayList<>();
        String query = """
        SELECT 
            b.booking_id,
            d.name AS destination_name,
            b.booking_date,
            b.total_amount,
            b.status AS booking_status,
            p.payment_date
        FROM Bookings b
        JOIN Packages pkg ON b.package_id = pkg.package_id
        JOIN Destinations d ON pkg.destination_id = d.destination_id
        LEFT JOIN Payments p ON b.booking_id = p.booking_id
        WHERE b.user_id = ? AND (b.status = 'Pending' OR b.status = 'Confirmed')
        ORDER BY b.booking_date DESC
    """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    BookingPaymentDTO payment = new BookingPaymentDTO(
                            rs.getInt("booking_id"),
                            rs.getString("destination_name"),
                            convertToLocalDate(rs.getDate("booking_date")),
                            rs.getBigDecimal("total_amount"),
                            rs.getString("booking_status"),
                            rs.getDate("payment_date") != null
                    );
                    payments.add(payment);
                }
            }
        } catch (SQLException e) {
            // Log error atau handle secara tepat
            Platform.runLater(() ->
                    showAlert(Alert.AlertType.ERROR, "Database Error",
                            "Gagal memuat data pembayaran: " + e.getMessage())
            );
        }

        return payments;
    }

    private void updatePaymentContainer(List<BookingPaymentDTO> payments) {
        // Pastikan method ini dipanggil di JavaFX Application Thread
        paymentContainer.getChildren().clear();

        if (payments.isEmpty()) {
            Label emptyStateLabel = new Label("No payments found");
            emptyStateLabel.getStyleClass().add("empty-state-label");
            paymentContainer.getChildren().add(emptyStateLabel);
            return;
        }

        for (BookingPaymentDTO payment : payments) {
            VBox bookingCard = createBookingCard(
                    payment.bookingId,
                    payment.destinationName,
                    payment.bookingDate,
                    payment.totalAmount,
                    payment.bookingStatus,
                    payment.isPaid
            );
            paymentContainer.getChildren().add(bookingCard);
        }
    }

    // Utility method untuk konversi java.sql.Date ke LocalDate
    private LocalDate convertToLocalDate(java.sql.Date sqlDate) {
        return sqlDate != null ? sqlDate.toLocalDate() : null;
    }

    private VBox createBookingCard(int bookingId, String destinationName,
                                   LocalDate bookingDate, BigDecimal totalAmount,
                                   String status, boolean isPaid) {
        VBox bookingCard = new VBox();
        bookingCard.getStyleClass().add("booking-card");

        HBox bookingInfo = new HBox();
        bookingInfo.getStyleClass().add("booking-info");
        bookingInfo.setSpacing(15);

        // Destination
        VBox destinationBox = createInfoSection("Destination:", destinationName);

        // Booking Date
        VBox dateBox = createInfoSection("Booking Date:", bookingDate.toString());

        // Total Amount
        VBox amountBox = createInfoSection("Total Amount:", formatCurrency(totalAmount));

        // Status
        VBox statusBox = createInfoSection("Status:", status);
        statusBox.lookup(".info-text").getStyleClass().add("status-" + status.toLowerCase());

        bookingInfo.getChildren().addAll(destinationBox, dateBox, amountBox, statusBox);

        // Action Buttons
        HBox actionBox = new HBox();
        actionBox.setSpacing(10);

        if (status.equals("Pending")) {
            Button payButton = createButton("Pay Now", "book-now-button");
            payButton.setOnAction(event -> processPayment(bookingId));

            Button cancelButton = createButton("Cancel Booking", "cancel-button");
            cancelButton.setOnAction(event -> cancelBooking(bookingId));

            actionBox.getChildren().addAll(payButton, cancelButton);
        } else if (status.equals("Confirmed") && !isPaid) {
            Button payButton = createButton("Complete Payment", "book-now-button");
            payButton.setOnAction(event -> processPayment(bookingId));

            actionBox.getChildren().add(payButton);
        } else {
            Label paidLabel = new Label("Paid");
            paidLabel.getStyleClass().add("info-text");
            actionBox.getChildren().add(paidLabel);
        }

        bookingCard.getChildren().addAll(bookingInfo, actionBox);

        return bookingCard;
    }


    private VBox createInfoSection(String label, String value) {
        VBox section = new VBox();
        section.getStyleClass().add("info-section");

        Label labelControl = new Label(label);
        labelControl.getStyleClass().add("info-label");

        Label valueControl = new Label(value);
        valueControl.getStyleClass().add("info-text");

        section.getChildren().addAll(labelControl, valueControl); return section;
    }

    private Button createButton(String text, String styleClass) {
        Button button = new Button(text);
        button.getStyleClass().add(styleClass);
        return button;
    }

    private void processPayment(int bookingId) {
        try {
            String insertPaymentQuery = """
                INSERT INTO Payments (booking_id, payment_date, payment_amount, payment_method, status) 
                SELECT booking_id, CURRENT_TIMESTAMP, total_amount, 'Credit Card', 'Paid'
                FROM Bookings 
                WHERE booking_id = ?
            """;

            String updateBookingQuery = "UPDATE Bookings SET status = 'Confirmed' WHERE booking_id = ?";

            try (Connection conn = DBConnection.getConnection()) {
                conn.setAutoCommit(false);

                try (PreparedStatement paymentStmt = conn.prepareStatement(insertPaymentQuery)) {
                    paymentStmt.setInt(1, bookingId);
                    paymentStmt.executeUpdate();
                }

                try (PreparedStatement bookingStmt = conn.prepareStatement(updateBookingQuery)) {
                    bookingStmt.setInt(1, bookingId);
                    bookingStmt.executeUpdate();
                }

                conn.commit();

                Platform.runLater(() -> {
                    paymentContainer.getChildren().clear();
                    loadPayments();
                    showAlert(Alert.AlertType.INFORMATION, "Payment Successful",
                            "Pembayaran berhasil dilakukan.");
                });

            } catch (SQLException e) {
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

    private String formatCurrency(BigDecimal amount) {
        return CURRENCY_FORMATTER.format(amount != null ? amount : BigDecimal.ZERO);
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}