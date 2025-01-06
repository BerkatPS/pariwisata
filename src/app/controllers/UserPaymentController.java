package app.controllers;

import app.utils.DBConnection;
import app.utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.print.PrinterJob;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UserPaymentController {

    @FXML
    private TextField nameField;

    @FXML
    private TextField cardNumberField;

    @FXML
    private TextField expiryMonthField;

    @FXML
    private TextField expiryYearField;

    @FXML
    private TextField cvvField;

    @FXML
    private ComboBox<String> paymentMethodComboBox;

    @FXML
    private ComboBox<String> bookingComboBox;

    @FXML
    private Label totalAmountLabel;

    @FXML
    private Button payButton;

    @FXML
    private Button printButton;

    @FXML
    private Button generatePDFButton;

    private ObservableList<String> paymentMethods = FXCollections.observableArrayList("Credit Card", "Debit Card", "PayPal");

    @FXML
    private void initialize() {
        paymentMethodComboBox.setItems(paymentMethods);
        loadUnpaidBookings();

        // Listener untuk memilih booking dan menampilkan total
        bookingComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                String[] splitValue = newValue.split(" - \\$");
                if (splitValue.length == 2) {
                    totalAmountLabel.setText("$" + splitValue[1]);
                }
            } else {
                totalAmountLabel.setText("");
            }
        });

        payButton.setOnAction(event -> processPayment());
//        printButton.setOnAction(event -> generatePDF());
    }

    private void loadUnpaidBookings() {
        ObservableList<String> unpaidBookings = FXCollections.observableArrayList();
        Integer userId = SessionManager.getInstance().getCurrentUserId();

        if (userId == null) {
            showAlert("Error", "User not logged in.");
            return;
        }

        String query = "SELECT booking_id, total_amount FROM Bookings WHERE status = 'Pending' AND user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int bookingId = rs.getInt("booking_id");
                double totalAmount = rs.getDouble("total_amount");
                unpaidBookings.add("Booking ID: " + bookingId + " - $" + totalAmount);
            }
            bookingComboBox.setItems(unpaidBookings);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Proses pembayaran
    private void processPayment() {
        String selectedBooking = bookingComboBox.getValue();
        if (selectedBooking == null) {
            showAlert("Error", "Please select a booking to pay for.");
            return;
        }

        Integer userId = SessionManager.getInstance().getCurrentUserId();
        if (userId == null) {
            showAlert("Error", "User not logged in.");
            return;
        }

        try {
            // Ambil ID booking dari combobox
            int bookingId = Integer.parseInt(selectedBooking.split(" ")[2]);
            String paymentMethod = paymentMethodComboBox.getValue();
            String name = nameField.getText();
            String cardNumber = cardNumberField.getText();
            String expiryMonth = expiryMonthField.getText();
            String expiryYear = expiryYearField.getText();
            String cvv = cvvField.getText();

            // Validasi input pembayaran
            if (paymentMethod == null || name.isEmpty() || cardNumber.isEmpty() || expiryMonth.isEmpty() || expiryYear.isEmpty() || cvv.isEmpty()) {
                showAlert("Error", "Please fill in all payment details.");
                return;
            }

            double paymentAmount = Double.parseDouble(totalAmountLabel.getText().replace("$", ""));
            LocalDateTime paymentDate = LocalDateTime.now();
            String status = "Completed";

            // Query untuk insert ke Payments
            String insertPaymentQuery = "INSERT INTO Payments (booking_id, payment_date, payment_amount, payment_method, status, created_at) VALUES (?, ?, ?, ?, ?, ?)";
            // Query untuk update status Booking
            String updateBookingQuery = "UPDATE Bookings SET status = ?, updated_at = ? WHERE booking_id = ?";

            try (Connection conn = DBConnection.getConnection()) {
                conn.setAutoCommit(false); // Mulai transaksi

                // Insert ke tabel Payments
                try (PreparedStatement paymentStmt = conn.prepareStatement(insertPaymentQuery)) {
                    paymentStmt.setInt(1, bookingId);
                    paymentStmt.setString(2, paymentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    paymentStmt.setDouble(3, paymentAmount);
                    paymentStmt.setString(4, paymentMethod);
                    paymentStmt.setString(5, status);
                    paymentStmt.setString(6, paymentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    paymentStmt.executeUpdate();
                }

                // Update status Booking
                try (PreparedStatement bookingStmt = conn.prepareStatement(updateBookingQuery)) {
                    bookingStmt.setString(1, status);
                    bookingStmt.setString(2, paymentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    bookingStmt.setInt(3, bookingId);
                    bookingStmt.executeUpdate();
                }

                conn.commit(); // Commit transaksi
                showAlert("Success", "Payment successful and booking status updated!");
                resetForm();
                loadUnpaidBookings();

            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Error", "Failed to process payment.");
            }

        } catch (NumberFormatException e) {
            showAlert("Error", "Invalid booking ID format.");
        }
    }


    private void generatePDF() {
        String selectedBooking = bookingComboBox.getValue();
        if (selectedBooking == null) {
            showAlert("Error", "Please select a booking to generate the PDF.");
            return;
        }

        VBox layout = new VBox(10);
        layout.getChildren().addAll(
                new Label("Payment Receipt"),
                new Label("Booking ID: " + selectedBooking),
                new Label("Payment Method: " + paymentMethodComboBox.getValue()),
                new Label("Total Amount: " + totalAmountLabel.getText()),
                new Label("Payment Date: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))),
                new Label("Status: Completed")
        );

        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null) {
            boolean success = job.showPrintDialog(null);
            if (success) {
                if (job.printPage(layout)) {
                    job.endJob();
                    showAlert("Success", "PDF generated successfully!");
                }
            }
        }
    }

    private void resetForm() {
        nameField.clear();
        cardNumberField.clear();
        expiryMonthField.clear();
        expiryYearField.clear();
        cvvField.clear();
        paymentMethodComboBox.getSelectionModel().clearSelection();
        bookingComboBox.getSelectionModel().clearSelection();
        totalAmountLabel.setText("");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
