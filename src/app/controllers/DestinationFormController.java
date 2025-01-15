package app.controllers;

import app.utils.DBConnection;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.UUID;

public class DestinationFormController {
    @FXML private TextField nameField;
    @FXML private TextField locationField;
    @FXML private TextArea descriptionField;
    @FXML private TextField openingHoursField;
    @FXML private TextField priceField;
    @FXML private ImageView imagePreview;

    private File selectedImageFile;

    @FXML
    public void handleImageUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Destination Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files",
                        "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );

        // Get the selected file
        File file = fileChooser.showOpenDialog(nameField.getScene().getWindow());

        if (file != null) {
            try {
                // Load and display image preview
                Image image = new Image(file.toURI().toString());
                imagePreview.setImage(image);

                // Store the selected file for later use
                selectedImageFile = file;
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Image Error",
                        "Failed to load image: " + e.getMessage());
            }
        }
    }

    @FXML
    public void handleSave() {
        // Validate input fields
        if (!validateInput()) {
            return;
        }

        try {
            // Save image to resources
            String imagePath = saveImage();

            // Save destination to database
            saveDestinationToDatabase(imagePath);

            // Close the form
            closeForm();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Save Error",
                    "Failed to save destination: " + e.getMessage());
        }
    }

    private boolean validateInput() {
        StringBuilder errors = new StringBuilder();

        if (nameField.getText().trim().isEmpty())
            errors.append("Name is required.\n");

        if (locationField.getText().trim().isEmpty())
            errors.append("Location is required.\n");

        if (descriptionField.getText().trim().isEmpty())
            errors.append("Description is required.\n");

        // Price validation
        try {
            double price = Double.parseDouble(priceField.getText());
            if (price < 0)
                errors.append("Price must be non-negative.\n");
        } catch (NumberFormatException e) {
            errors.append("Invalid price format.\n");
        }

        if (!errors.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", errors.toString());
            return false;
        }

        return true;
    }

    private String saveImage() throws IOException {
        if (selectedImageFile == null) {
            return "default_destination.png"; // Default image if no image selected
        }

        // Generate unique filename
        String uniqueFileName = UUID.randomUUID().toString() +
                getFileExtension(selectedImageFile.getName());

        // Define destination path
        Path destinationDir = Paths.get("src/resources/fxml/images");
        Files.createDirectories(destinationDir);

        Path destinationPath = destinationDir.resolve(uniqueFileName);

        // Copy file to destination
        Files.copy(selectedImageFile.toPath(),
                destinationPath,
                StandardCopyOption.REPLACE_EXISTING
        );

        return "destinations/" + uniqueFileName;
    }

    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex == -1) ? "" : filename.substring(dotIndex);
    }

    private void saveDestinationToDatabase(String imagePath) throws SQLException {
        String query = """
            INSERT INTO Destinations 
            (name, location, description, opening_hours, price_per_entry, image_url, created_at, updated_at) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, nameField.getText());
            statement.setString(2, locationField.getText());
            statement.setString(3, descriptionField.getText());
            statement.setString(4, openingHoursField.getText());
            statement.setDouble(5, Double.parseDouble(priceField.getText()));
            statement.setString(6, imagePath);
            statement.setObject(7, LocalDateTime.now());
            statement.setObject(8, LocalDateTime.now());
            statement.executeUpdate();
        }
    }

    @FXML
    public void handleCancel() {
        closeForm();
    }

    private void closeForm() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}