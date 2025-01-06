package app.controllers;

import app.utils.DBConnection;
import app.utils.SessionManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Locale;

public class UserBookingController {

    @FXML private ComboBox<DestinationItem> destinationComboBox;
    @FXML private DatePicker bookingDatePicker;
    @FXML private TextField peopleTextField;
    @FXML private Label totalPriceLabel;
    @FXML private Button bookButton;
    @FXML private Button cancelButton;
    @FXML private Button closeButton;

    private int selectedPackageId;

    // Inner class untuk menyimpan informasi destinasi
    public static class DestinationItem {
        private int destinationId;
        private String name;
        private BigDecimal pricePerEntry;

        public DestinationItem(int destinationId, String name, BigDecimal pricePerEntry) {
            this.destinationId = destinationId;
            this.name = name;
            this.pricePerEntry = pricePerEntry;
        }

        public int getDestinationId() {
            return destinationId;
        }

        public BigDecimal getPricePerEntry() {
            return pricePerEntry;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    @FXML
    public void initialize() {
        // Inisialisasi ComboBox dengan daftar destinasi
        loadDestinations();

        // Event handler untuk menghitung total harga
        destinationComboBox.setOnAction(event -> calculateTotalPrice());
        peopleTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                calculateTotalPrice();
            }
        });

        // Event handler untuk tombol
        bookButton.setOnAction(event -> processBooking());
        cancelButton.setOnAction(event -> closeWindow());
        closeButton.setOnAction(event -> closeWindow());

        // Validasi input
        peopleTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                peopleTextField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }

    private void loadDestinations() {
        ObservableList<DestinationItem> destinations = FXCollections.observableArrayList();
        String query = """
            SELECT d.destination_id, d.name, p.price 
            FROM Destinations d
            JOIN Packages p ON d.destination_id = p.destination_id
            ORDER BY d.name
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int destinationId = rs.getInt("destination_id");
                String name = rs.getString("name");
                BigDecimal price = rs.getBigDecimal("price");
                destinations.add(new DestinationItem(destinationId, name, price));
            }

            destinationComboBox.setItems(destinations);

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error",
                    "Gagal memuat daftar destinasi: " + e.getMessage());
        }
    }

    private void calculateTotalPrice() {
        DestinationItem selectedDestination = destinationComboBox.getValue();
        if (selectedDestination == null) {
            totalPriceLabel.setText("Pilih destinasi terlebih dahulu");
            return;
        }

        int numberOfPeople;
        try {
            numberOfPeople = Integer.parseInt(peopleTextField.getText());
            if (numberOfPeople <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            totalPriceLabel.setText("Jumlah orang tidak valid");
            return;
        }

        // Hitung total harga
        BigDecimal pricePerEntry = selectedDestination.getPricePerEntry();
        BigDecimal totalPrice = pricePerEntry.multiply(BigDecimal.valueOf(numberOfPeople))
                .setScale(2, RoundingMode.HALF_UP);

        // Simpan package ID untuk proses booking
        selectedPackageId = findPackageIdForDestination(selectedDestination.getDestinationId());

        // Format dan tampilkan harga
        totalPriceLabel.setText(formatCurrency(totalPrice));
    }

    private int findPackageIdForDestination(int destinationId) {
        String query = "SELECT package_id FROM Packages WHERE destination_id = ? LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, destinationId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("package_id");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error",
                    "Gagal mencari paket: " + e.getMessage());
        }
        return -1;
    }

    private void processBooking() {
        // Validasi input
        if (!validateBookingInput()) return;

        // Ambil data dari form
        DestinationItem selectedDestination = destinationComboBox.getValue();
        LocalDate bookingDate = bookingDatePicker.getValue();
        int numberOfPeople = Integer.parseInt(peopleTextField.getText());

        // Hitung total harga
        BigDecimal pricePerEntry = selectedDestination.getPricePerEntry();
        BigDecimal totalPrice = pricePerEntry.multiply(BigDecimal.valueOf(numberOfPeople))
                .setScale(2, RoundingMode.HALF_UP);

        // Proses insert booking
        int userId = SessionManager.getInstance().getCurrentUserId();
        insertBookingToDatabase(userId, selectedPackageId, bookingDate, totalPrice);
    }

    private boolean validateBookingInput() {
        DestinationItem selectedDestination = destinationComboBox.getValue();
        LocalDate bookingDate = bookingDatePicker.getValue();
        String peopleText = peopleTextField.getText();

        if (selectedDestination == null) {
            showAlert(Alert.AlertType.WARNING, "Validasi", "Pilih destinasi terlebih dahulu");
            return false;
        }

        if (bookingDate == null) {
            showAlert(Alert.AlertType.WARNING, "Validasi", "Pilih tanggal booking");
            return false;
        }

        if (peopleText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validasi", "Masukkan jumlah orang");
            return false;
        }

        try {
            int numberOfPeople = Integer.parseInt(peopleText);
            if (numberOfPeople <= 0) {
                showAlert(Alert.AlertType.WARNING, "Validasi", "Jumlah orang harus lebih dari 0");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Validasi", "Jumlah orang tidak valid");
            return false;
        }

        return true;
    }

    private void insertBookingToDatabase(int userId, int packageId, LocalDate bookingDate, BigDecimal totalAmount) {
        String insertQuery = "INSERT INTO Bookings (user_id, package_id, booking_date, total_amount, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertQuery)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, packageId);
            stmt.setTimestamp(3, Timestamp.valueOf(bookingDate.atStartOfDay()));
            stmt.setBigDecimal(4, totalAmount);
            stmt.setString(5, "Pending");

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.INFORMATION, "Booking Success", "Booking berhasil dilakukan!");
                    closeWindow();
                });
            } else {
                Platform.runLater(() ->
                        showAlert(Alert.AlertType.ERROR, "Booking Error", "Gagal melakukan booking.")
                );
            }
        } catch (SQLException e) {
            Platform.runLater(() ->
                    showAlert(Alert.AlertType.ERROR, "Database Error",
                            "Terjadi kesalahan: " + e.getMessage())
            );
            e.printStackTrace();
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String formatCurrency(BigDecimal amount) {
        Locale indonesia = new Locale("id", "ID");
        NumberFormat rupiahFormat = NumberFormat.getCurrencyInstance(indonesia);
        rupiahFormat.setCurrency(Currency.getInstance("IDR"));
        return rupiahFormat.format(amount);
    }
}