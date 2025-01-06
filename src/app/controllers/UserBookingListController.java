package app.controllers;

import app.models.Booking;
import app.utils.DBConnection;
import app.utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Locale;

public class UserBookingListController {

    @FXML
    private VBox bookingContainer;

    @FXML
    private Button bookNowButton;

    @FXML
    private Button loadData;

    @FXML
    private Button bookButton;

    @FXML
    public void initialize() {
        loadBookings();
        bookButton.setOnAction(event -> openBookingForm());
        loadData.setOnAction(event -> loadData());
    }



    @FXML
    private void openBookingForm() {
        try {
            // Load the FXML for the booking form
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/user/visitor_booking.fxml"));
            Parent bookingFormRoot = loader.load();

            // Create a new Stage for the booking form
            Stage bookingStage = new Stage();
            bookingStage.initModality(Modality.APPLICATION_MODAL); // Blocking interaction with other windows
            bookingStage.initStyle(StageStyle.UNDECORATED); // Removes default window decorations
            bookingStage.setTitle("Create New Booking");

            // Create a scene with custom styling
            Scene scene = new Scene(bookingFormRoot);
            scene.setFill(Color.TRANSPARENT);
            bookingStage.initStyle(StageStyle.TRANSPARENT);

            bookingStage.setScene(scene);
            bookingStage.show();

            // Optional: Add dragging functionality to the undecorated stage
            addDraggingFunctionality(bookingFormRoot, bookingStage);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open booking form.");
        }
    }
    private void addDraggingFunctionality(Parent root, Stage stage) {
        final Delta dragDelta = new Delta();
        root.setOnMousePressed(mouseEvent -> {
            dragDelta.x = stage.getX() - mouseEvent.getScreenX();
            dragDelta.y = stage.getY() - mouseEvent.getScreenY();
        });
        root.setOnMouseDragged(mouseEvent -> {
            stage.setX(mouseEvent.getScreenX() + dragDelta.x);
            stage.setY(mouseEvent.getScreenY() + dragDelta.y);
        });
    }

    // Helper class to track mouse movement
    private static class Delta {
        double x, y;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void loadData() {
        loadBookings();
    }

    private void loadBookings() {
        String query = """
                SELECT 
                    d.name AS destinationName,
                    p.package_name AS packageName, 
                    b.booking_date AS bookingDate,
                    b.total_amount AS totalAmount,
                    b.status,
                    b.created_at AS bookingCreated
                FROM Bookings b
                JOIN Packages p ON b.package_id = p.package_id
                JOIN Destinations d ON p.destination_id = d.destination_id
                WHERE b.user_id = ?
                ORDER BY b.booking_date DESC
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, SessionManager.getInstance().getCurrentUserId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String destinationName = rs.getString("destinationName");
                String packageName = rs.getString("packageName");
                LocalDateTime bookingDate = rs.getTimestamp("bookingDate").toLocalDateTime();
                double totalAmount = rs.getDouble("totalAmount");
                String status = rs.getString("status");
                LocalDateTime createdAt = rs.getTimestamp("bookingCreated").toLocalDateTime();

                VBox bookingCard = createBookingCard(
                        destinationName,
                        packageName,
                        bookingDate,
                        totalAmount,
                        status,
                        createdAt
                );
                bookingContainer.getChildren().add(bookingCard);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to load booking data.");
        }
    }

    private VBox createBookingCard(
            String destinationName,
            String packageName,
            LocalDateTime bookingDate,
            double totalAmount,
            String status,
            LocalDateTime createdAt
    ) {
        VBox bookingCard = new VBox();
        bookingCard.getStyleClass().add("booking-card");

        HBox bookingInfo = new HBox();
        bookingInfo.getStyleClass().add("booking-info");

        // Destination & Package Info
        VBox destinationBox = createInfoSection(
                "Destination",
                destinationName + "\n" + packageName
        );

        // Booking Date Info
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy\nhh:mm a");
        VBox dateBox = createInfoSection(
                "Travel Date",
                bookingDate.format(dateFormatter)
        );

        // Amount Info
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        VBox amountBox = createInfoSection(
                "Total Amount",
                currencyFormatter.format(totalAmount)
        );
        amountBox.getChildren().get(1).getStyleClass().add("price-text");

        // Status Badge
        Label statusBadge = new Label(status.toUpperCase());
        statusBadge.getStyleClass().addAll("status-badge", "status-" + status.toLowerCase());

        // Created Date Info
        VBox createdBox = createInfoSection(
                "Booked On",
                createdAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
        );

        bookingInfo.getChildren().addAll(
                destinationBox,
                dateBox,
                amountBox,
                createdBox,
                statusBadge
        );

        bookingCard.getChildren().add(bookingInfo);

        return bookingCard;
    }

    private VBox createInfoSection(String label, String value) {
        VBox section = new VBox();
        section.getStyleClass().add("info-section");

        Label labelText = new Label(label);
        labelText.getStyleClass().add("info-label");

        Label valueText = new Label(value);
        valueText.getStyleClass().add("info-text");

        section.getChildren().addAll(labelText, valueText);
        return section;
    }
}