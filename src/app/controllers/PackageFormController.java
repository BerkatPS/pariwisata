package app.controllers;

import app.models.Package;
import app.utils.DBConnection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.Locale;

public class PackageFormController {

    @FXML private Label formTitleLabel;
    @FXML private TextField packageNameField;
    @FXML private TextArea descriptionField;
    @FXML private TextField priceField;
    @FXML private TextField durationField;
    @FXML private Button saveButton;

    @FXML private Label packageNameError;
    @FXML private Label descriptionError;
    @FXML private Label priceError;
    @FXML private Label durationError;

    private Package editingPackage;
    private boolean isEditMode = false;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    @FXML
    public void initialize() {
        setupValidation();
        setupPriceFormatting();
    }

    private void setupValidation() {
        // Real-time validation for Package Name
        packageNameField.textProperty().addListener((obs, oldVal, newVal) -> {
            validateField(packageNameField, packageNameError, "Package name is required",
                    newVal != null && !newVal.trim().isEmpty());
        });

        // Real-time validation for Description
        descriptionField.textProperty().addListener((obs, oldVal, newVal) -> {
            validateField(descriptionField, descriptionError, "Description is required",
                    newVal != null && !newVal.trim().isEmpty());
        });

        // Real-time validation for Price with currency format
        priceField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty()) {
                try {
                    double price = parsePrice(newVal);
                    validateField(priceField, priceError, "", price > 0);
                    if (price <= 0) {
                        priceError.setText("Price must be greater than 0");
                    }
                } catch (NumberFormatException e) {
                    priceError.setText("Invalid price format");
                    priceError.setVisible(true);
                }
            }
        });

        // Real-time validation for Duration
        durationField.textProperty().addListener((obs, oldVal, newVal) -> {
            validateField(durationField, durationError, "Duration is required",
                    newVal != null && !newVal.trim().isEmpty());
        });
    }

    private void setupPriceFormatting() {
        // Format price when focus is lost
        priceField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused && !priceField.getText().isEmpty()) {
                try {
                    double price = parsePrice(priceField.getText());
                    priceField.setText(currencyFormat.format(price));
                } catch (NumberFormatException e) {
                    priceError.setText("Invalid price format");
                    priceError.setVisible(true);
                }
            }
        });
    }

    public void initForEdit(Package pkg) {
        this.editingPackage = pkg;
        this.isEditMode = true;
        formTitleLabel.setText("Edit Package");
        saveButton.setText("Update Package");

        // Populate fields with existing data
        packageNameField.setText(pkg.getPackageName());
        descriptionField.setText(pkg.getDescription());
        priceField.setText(currencyFormat.format(pkg.getPrice()));
        durationField.setText(pkg.getDuration());
    }

    @FXML
    private void handleSave() {
        if (validateForm()) {
            try (Connection conn = DBConnection.getConnection()) {
                if (isEditMode) {
                    updatePackage(conn);
                } else {
                    insertPackage(conn);
                }
                showAlert(Alert.AlertType.INFORMATION, "Success",
                        isEditMode ? "Package updated successfully" : "Package added successfully");
                handleBack();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Error",
                        "Failed to " + (isEditMode ? "update" : "save") + " package: " + e.getMessage());
            }
        }
    }

    private void insertPackage(Connection conn) throws SQLException {
        String sql = "INSERT INTO Packages (package_name, description, price, duration, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, NOW(), NOW())";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, packageNameField.getText().trim());
            stmt.setString(2, descriptionField.getText().trim());
            stmt.setDouble(3, parsePrice(priceField.getText()));
            stmt.setString(4, durationField.getText().trim());
            stmt.executeUpdate();
        }
    }

    private void updatePackage(Connection conn) throws SQLException {
        String sql = "UPDATE Packages SET package_name = ?, description = ?, price = ?, " +
                "duration = ?, updated_at = NOW() WHERE package_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, packageNameField.getText().trim());
            stmt.setString(2, descriptionField.getText().trim());
            stmt.setDouble(3, parsePrice(priceField.getText()));
            stmt.setString(4, durationField.getText().trim());
            stmt.setInt(5, editingPackage.getPackageId());
            stmt.executeUpdate();
        }
    }

    @FXML
    private void handleClear() {
        packageNameField.clear();
        descriptionField.clear();
        priceField.clear();
        durationField.clear();
        clearValidationErrors();
    }

    @FXML
    private void handleBack() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/resources/fxml/admin/package.fxml"));
            Scene scene = new Scene(root);
            Stage stage = (Stage) saveButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean validateForm() {
        boolean isValid = true;

        isValid &= validateField(packageNameField, packageNameError, "Package name is required",
                !packageNameField.getText().trim().isEmpty());

        isValid &= validateField(descriptionField, descriptionError, "Description is required",
                !descriptionField.getText().trim().isEmpty());

        try {
            double price = parsePrice(priceField.getText());
            isValid &= validateField(priceField, priceError, "Price must be greater than 0", price > 0);
        } catch (NumberFormatException e) {
            priceError.setText("Invalid price format");
            priceError.setVisible(true);
            isValid = false;
        }

        isValid &= validateField(durationField, durationError, "Duration is required",
                !durationField.getText().trim().isEmpty());

        return isValid;
    }

    private boolean validateField(TextInputControl field, Label errorLabel, String errorMessage, boolean condition) {
        errorLabel.setText(errorMessage);
        errorLabel.setVisible(!condition);
        errorLabel.setManaged(!condition);
        return condition;
    }

    private void clearValidationErrors() {
        packageNameError.setVisible(false);
        descriptionError.setVisible(false);
        priceError.setVisible(false);
        durationError.setVisible(false);
    }

    private double parsePrice(String priceStr) {
        // Remove currency symbol and formatting
        String normalized = priceStr.replaceAll("[Rp.,\\s]", "");
        return Double.parseDouble(normalized);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}