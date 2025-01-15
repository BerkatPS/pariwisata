package app.controllers;

import app.models.Destination;
import app.models.Package;
import app.utils.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Locale;

public class PackageFormController {
    @FXML private ComboBox<Destination> destinationComboBox;
    @FXML private TextField packageNameField;
    @FXML private TextArea descriptionField;
    @FXML private TextField priceField;
    @FXML private TextField durationField;

    @FXML private Label destinationError;
    @FXML private Label packageNameError;
    @FXML private Label descriptionError;
    @FXML private Label priceError;
    @FXML private Label durationError;

    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    @FXML
    public void initialize() {
        setupDestinationComboBox();
        setupPriceFormatting();
    }

    private void setupDestinationComboBox() {
        // Konfigurasi tampilan ComboBox
        destinationComboBox.setConverter(new StringConverter<Destination>() {
            @Override
            public String toString(Destination destination) {
                return destination == null ? "" : destination.getName();
            }

            @Override
            public Destination fromString(String string) {
                return null; // Tidak diperlukan untuk ComboBox read-only
            }
        });

        // Memuat destinasi yang belum terelasi dengan paket
        ObservableList<Destination> destinations = FXCollections.observableArrayList();

        String query = """
            SELECT d.* FROM Destinations d
            LEFT JOIN Packages p ON d.destination_id = p.destination_id
            WHERE p.destination_id IS NULL
            OR d.destination_id NOT IN (
                SELECT DISTINCT destination_id 
                FROM Packages 
                WHERE destination_id IS NOT NULL
            )
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Destination destination = new Destination(
                        rs.getInt("destination_id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("location"),
                        rs.getString("opening_hours"),
                        rs.getDouble("price_per_entry"),
                        rs.getString("image_url"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getTimestamp("updated_at").toLocalDateTime()
                );
                destinations.add(destination);
            }

            destinationComboBox.setItems(destinations);

            // Tambahkan listener untuk menampilkan error jika tidak ada destinasi
            if (destinations.isEmpty()) {
                destinationError.setText("No available destinations. Please add a destination first.");
                destinationError.setVisible(true);
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load destinations: " + e.getMessage());
        }
    }

    private void setupPriceFormatting() {
        priceField.focusedProperty().addListener((obs, oldVal, isFocused) -> {
            if (!isFocused) {
                formatPrice();
            }
        });
    }

    private void formatPrice() {
        try {
            if (!priceField.getText().isEmpty()) {
                double price = parsePrice(priceField.getText());
                priceField.setText(currencyFormatter.format(price));
                priceError.setVisible(false);
            }
        } catch (NumberFormatException e) {
            priceError.setText("Invalid price format");
            priceError.setVisible(true);
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) packageNameField.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleSave() {
        if (validateForm()) {
            try {
                savePackage();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Package saved successfully");
                handleClose();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to save package: " + e.getMessage());
            }
        }
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Reset error labels
        clearErrorLabels();

        // Validate Destination
        if (destinationComboBox.getValue() == null) {
            destinationError.setText("Please select a destination");
            destinationError.setVisible(true);
            isValid = false;
        }

        // Validate Package Name
        if (packageNameField.getText().trim().isEmpty()) {
            packageNameError.setText("Package name is required");
            packageNameError.setVisible(true);
            isValid = false;
        }

        // Validate Description
        if (descriptionField.getText().trim().isEmpty()) {
            descriptionError.setText("Description is required");
            descriptionError.setVisible(true);
            isValid = false;
        }

        // Validate Price
        try {
            double price = parsePrice(priceField.getText());
            if (price <= 0) {
                priceError.setText("Price must be greater than zero");
                priceError.setVisible(true);
                isValid = false;
            }
        } catch (NumberFormatException e) {
            priceError.setText("Invalid price format");
            priceError.setVisible(true);
            isValid = false;
        }

        // Validate Duration
        if (durationField.getText().trim().isEmpty()) {
            durationError.setText("Duration is required");
            durationError.setVisible(true);
            isValid = false;
        }

        return isValid;
    }

    private void clearErrorLabels() {
        destinationError.setVisible(false);
        packageNameError.setVisible(false);
        descriptionError.setVisible(false);
        priceError.setVisible(false);
        durationError.setVisible(false);
    }

    private void savePackage() throws SQLException {
        String query = "INSERT INTO Packages (destination_id, package_name, description, price, duration, created_at, updated_at) VALUES (?, ?, ?, ?, ?, NOW(), NOW())";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, destinationComboBox.getValue().getDestinationId());
            stmt.setString(2, packageNameField.getText().trim());
            stmt.setString(3, descriptionField.getText().trim());
            stmt.setDouble(4, parsePrice(priceField.getText()));
            stmt.setString(5, durationField.getText().trim());
            stmt.executeUpdate();
        }
    }

    private double parsePrice(String priceText) {
        return Double.parseDouble(priceText.replaceAll("[^\\d.]", ""));
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}