package app.controllers;

import app.models.Payment;
import app.utils.DBConnection;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Arrays;

public class PaymentController {
    @FXML private TableView<Payment> paymentTable;
    @FXML private TableColumn<Payment, Integer> paymentIdColumn;
    @FXML private TableColumn<Payment, Integer> bookingIdColumn;
    @FXML private TableColumn<Payment, Double> amountColumn;
    @FXML private TableColumn<Payment, String> methodColumn;
    @FXML private TableColumn<Payment, String> statusColumn;
    @FXML private TableColumn<Payment, Void> actionColumn;

    @FXML private TextField searchField;
    @FXML private Button addPaymentButton;
    @FXML private Label totalPaymentsLabel;
    @FXML private Label totalRevenueLabel;

    private ObservableList<Payment> paymentList = FXCollections.observableArrayList();
    private FilteredList<Payment> filteredPayments;

    @FXML
    public void initialize() {
        // Set up the TableView columns
        paymentIdColumn.setCellValueFactory(cellData -> cellData.getValue().paymentIdProperty().asObject());
        bookingIdColumn.setCellValueFactory(cellData -> cellData.getValue().bookingIdProperty().asObject());
        amountColumn.setCellValueFactory(cellData -> cellData.getValue().paymentAmountProperty().asObject());
        methodColumn.setCellValueFactory(cellData -> cellData.getValue().paymentMethodProperty());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

        // Set up action column
        setupActionColumn();

        // Load payments on initialization
        loadPayments();

        // Set up search filter
        setupSearchFilter();
    }

    private void setupActionColumn() {
        actionColumn.setCellFactory(column -> new TableCell<Payment, Void>() {
            private final Button confirmButton = new Button("Confirm");
            private final Button cancelButton = new Button("Cancel");


            {
                confirmButton.getStyleClass().add("button-secondary");
                cancelButton.getStyleClass().add("button-danger");

                confirmButton.setOnAction(event -> handleConfirmPayment());
                cancelButton.setOnAction(event -> handleCancelPayment());
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox actionBox = new HBox(10, confirmButton, cancelButton);
                    setGraphic(actionBox);
                }
            }
        });
    }

    // handleConfirmPayment

    // handleCancelPayment


    private void setupSearchFilter() {
        filteredPayments = new FilteredList<>(paymentList, p -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredPayments.setPredicate(payment -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();
                return String.valueOf(payment.getPaymentId()).contains(lowerCaseFilter) ||
                        String.valueOf(payment.getBookingId()).contains(lowerCaseFilter) ||
                        payment.getStatus().toLowerCase().contains(lowerCaseFilter);
            });
        });
        paymentTable.setItems(filteredPayments);
    }

    @FXML
    private void handleConfirmPayment() {
        Payment selectedPayment = paymentTable.getSelectionModel().getSelectedItem();
        if (selectedPayment == null) {
            showAlert("Please select a payment to confirm.", Alert.AlertType.WARNING);
            return;
        }

        // Cek apakah pembayaran sudah dalam status yang dapat dikonfirmasi
        if ("Pending".equals(selectedPayment.getStatus())) {
            try (Connection connection = DBConnection.getConnection()) {
                // Transaction untuk memastikan konsistensi data
                connection.setAutoCommit(false);

                try {
                    // Update status pembayaran
                    String paymentUpdateQuery = "UPDATE Payments SET status = 'Confirmed', updated_at = CURRENT_TIMESTAMP WHERE payment_id = ?";
                    try (PreparedStatement paymentStmt = connection.prepareStatement(paymentUpdateQuery)) {
                        paymentStmt.setInt(1, selectedPayment.getPaymentId());
                        paymentStmt.executeUpdate();
                    }

                    // Update status booking terkait
                    String bookingUpdateQuery = "UPDATE Bookings SET status = 'Confirmed' WHERE booking_id = ?";
                    try (PreparedStatement bookingStmt = connection.prepareStatement(bookingUpdateQuery)) {
                        bookingStmt.setInt(1, selectedPayment.getBookingId());
                        bookingStmt.executeUpdate();
                    }

                    // Commit transaksi
                    connection.commit();

                    // Refresh data di UI
                    Platform.runLater(() -> {
                        loadPayments();
                        showAlert("Payment confirmed successfully!", Alert.AlertType.INFORMATION);
                    });

                } catch (SQLException e) {
                    // Rollback transaksi jika terjadi kesalahan
                    connection.rollback();
                    throw e;
                } finally {
                    // Kembalikan auto-commit ke default
                    connection.setAutoCommit(true);
                }

            } catch (SQLException e) {
                Platform.runLater(() -> {
                    e.printStackTrace();
                    showAlert("Error confirming payment: " + e.getMessage(), Alert.AlertType.ERROR);
                });
            }
        } else {
            showAlert("Only pending payments can be confirmed.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void handleCancelPayment() {
        Payment selectedPayment = paymentTable.getSelectionModel().getSelectedItem();
        if (selectedPayment == null) {
            showAlert("Please select a payment to cancel.", Alert.AlertType.WARNING);
            return;
        }

        // Cek apakah pembayaran dapat dibatalkan
        if (Arrays.asList("Pending", "Confirmed").contains(selectedPayment.getStatus())) {
            try (Connection connection = DBConnection.getConnection()) {
                // Transaction untuk memastikan konsistensi data
                connection.setAutoCommit(false);

                try {
                    // Update status pembayaran
                    String paymentUpdateQuery = "UPDATE Payments SET status = 'Cancelled', updated_at = CURRENT_TIMESTAMP WHERE payment_id = ?";
                    try (PreparedStatement paymentStmt = connection.prepareStatement(paymentUpdateQuery)) {
                        paymentStmt.setInt(1, selectedPayment.getPaymentId());
                        paymentStmt.executeUpdate();
                    }

                    // Update status booking terkait
                    String bookingUpdateQuery = "UPDATE Bookings SET status = 'Cancelled' WHERE booking_id = ?";
                    try (PreparedStatement bookingStmt = connection.prepareStatement(bookingUpdateQuery)) {
                        bookingStmt.setInt(1, selectedPayment.getBookingId());
                        bookingStmt.executeUpdate();
                    }

                    // Jika pembayaran sudah dikonfirmasi, kembalikan dana
                    if ("Confirmed".equals(selectedPayment.getStatus())) {
                        // Proses pengembalian dana (sesuaikan dengan logika bisnis Anda)
                        processRefund(connection, selectedPayment);
                    }

                    // Commit transaksi
                    connection.commit();

                    // Refresh data di UI
                    Platform.runLater(() -> {
                        loadPayments();
                        showAlert("Payment cancelled successfully!", Alert.AlertType.INFORMATION);
                    });

                } catch (SQLException e) {
                    // Rollback transaksi jika terjadi kesalahan
                    connection.rollback();
                    throw e;
                } finally {
                    // Kembalikan auto-commit ke default
                    connection.setAutoCommit(true);
                }

            } catch (SQLException e) {
                Platform.runLater(() -> {
                    e.printStackTrace();
                    showAlert("Error cancelling payment: " + e.getMessage(), Alert.AlertType.ERROR);
                });
            }
        } else {
            showAlert("This payment cannot be cancelled.", Alert.AlertType.WARNING);
        }
    }

    // Metode tambahan untuk memproses pengembalian dana
    private void processRefund(Connection connection, Payment payment) throws SQLException {
        // Contoh sederhana untuk mencatat pengembalian dana
        String refundQuery = """
        INSERT INTO Refunds (
            payment_id, 
            booking_id, 
            refund_amount, 
            refund_date, 
            status
        ) VALUES (?, ?, ?, CURRENT_TIMESTAMP, 'Processed')
    """;

        try (PreparedStatement refundStmt = connection.prepareStatement(refundQuery)) {
            refundStmt.setInt(1, payment.getPaymentId());
            refundStmt.setInt(2, payment.getBookingId());
            refundStmt.setDouble(3, payment.getPaymentAmount());
            refundStmt.executeUpdate();
        }
    }

    // Metode bantuan untuk menampilkan alert

    @FXML
    private void loadPayments() {
        paymentList.clear(); // Clear the list before loading
        try (Connection connection = DBConnection.getConnection()) {
            if (connection != null) {
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT * FROM Payments");

                while (resultSet.next()) {
                    Payment payment = new Payment(
                            resultSet.getInt("payment_id"),
                            resultSet.getInt("booking_id"),
                            resultSet.getTimestamp("payment_date").toLocalDateTime(),
                            resultSet.getDouble("payment_amount"),
                            resultSet.getString("payment_method"),
                            resultSet.getString("status"),
                            resultSet.getTimestamp("created_at").toLocalDateTime()
                    );
                    paymentList.add(payment);
                }

                paymentTable.setItems(paymentList);
                updateStatistics();
            } else {
                showAlert("Failed to connect to the database.", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error loading payments: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void updateStatistics() {
        totalPaymentsLabel.setText(String.valueOf(paymentList.size()));
        double totalRevenue = paymentList.stream().mapToDouble(Payment::getPaymentAmount).sum();
        totalRevenueLabel.setText("Rp " + totalRevenue);
    }

    private void showAlert(String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
