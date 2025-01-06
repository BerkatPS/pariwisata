package app.controllers;

import app.models.ReviewData;
import app.utils.DBConnection;
import app.utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

public class UserReviewController {

    // FXML injected fields from the new design
    @FXML private VBox reviewContainer;
    @FXML private Button openReviewButton;
    @FXML private ComboBox<String> filterComboBox;
    @FXML private Text totalReviewsText;

    // Existing form fields
    @FXML private ComboBox<String> unreviewedDestinationsComboBox;
    @FXML private TextField destinationIdInput;
    @FXML private TextField userIdInput;
    @FXML private ComboBox<Integer> ratingComboBox;
    @FXML private TextArea reviewTextArea;
    @FXML private DatePicker reviewDatePicker;
    @FXML private Button submitReviewButton;

    @FXML
    public void initialize() {
        setupFilterComboBox();
        loadReviews();
        updateStats();
        loadUnreviewedDestinations();

        // Set up event handlers
        openReviewButton.setOnAction(event -> openReviewForm());
        filterComboBox.setOnAction(event -> filterReviews());

        // Set up form event handlers if in form view
        if (submitReviewButton != null) {
            submitReviewButton.setOnAction(event -> insertReview());
        }

        if (unreviewedDestinationsComboBox != null) {
            unreviewedDestinationsComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    destinationIdInput.setText(getDestinationIdFromComboBoxValue(newValue));
                }
            });
        }
    }

    private void setupFilterComboBox() {
        if (filterComboBox != null) {
            filterComboBox.getItems().addAll(
                    "All Reviews",
                    "Highest Rated",
                    "Most Recent",
                    "Oldest First"
            );
            filterComboBox.setValue("All Reviews");
        }
    }

    private void loadReviews() {
        String query = """
            SELECT 
                r.review_id,
                u.name AS username,
                d.name AS destination_name,
                r.rating,
                r.review_text,
                r.created_at
            FROM Reviews r
            JOIN Users u ON r.user_id = u.user_id
            JOIN Destinations d ON r.destination_id = d.destination_id
            WHERE r.user_id = ?
            ORDER BY r.created_at DESC
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, SessionManager.getInstance().getCurrentUserId());
            ResultSet rs = stmt.executeQuery();

            reviewContainer.getChildren().clear();
            while (rs.next()) {
                ReviewData review = new ReviewData(
                        rs.getString("username"),
                        rs.getString("destination_name"),
                        rs.getInt("rating"),
                        rs.getString("review_text"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                );
                reviewContainer.getChildren().add(createReviewCard(review));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to load reviews.");
        }
    }

    // Your existing methods
    private void loadUnreviewedDestinations() {
        if (unreviewedDestinationsComboBox == null) return;

        Integer userId = SessionManager.getInstance().getCurrentUserId();
        if (userId == null) {
            System.out.println("User not logged in.");
            return;
        }

        String query = """
                SELECT
                    d.destination_id,
                    d.name AS destination_name
                FROM
                    Payments p
                JOIN
                    Bookings b ON p.booking_id = b.booking_id
                JOIN
                    Packages pkg ON b.package_id = pkg.package_id
                JOIN
                    Destinations d ON pkg.destination_id = d.destination_id
                LEFT JOIN
                    Reviews r ON b.user_id = r.user_id AND d.destination_id = r.destination_id
                WHERE
                    p.status = 'Confirmed'
                    AND r.review_id IS NULL
                    AND b.user_id = ?
                """;

        ObservableList<String> destinationsList = FXCollections.observableArrayList();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int destinationId = rs.getInt("destination_id");
                String destinationName = rs.getString("destination_name");
                destinationsList.add(destinationId + " - " + destinationName);
            }

            unreviewedDestinationsComboBox.setItems(destinationsList);

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to load unreviewed destinations.");
        }
    }

    private String getDestinationIdFromComboBoxValue(String comboBoxValue) {
        return comboBoxValue.split(" - ")[0];
    }

    private void insertReview() {
        Integer userId = SessionManager.getInstance().getCurrentUserId();
        String destinationId = destinationIdInput.getText();
        Integer rating = ratingComboBox.getValue();
        String reviewText = reviewTextArea.getText();
        String date = reviewDatePicker.getValue().toString();

        if (userId == null || destinationId.isEmpty() || rating == null || reviewText.isEmpty() || date.isEmpty()) {
            System.out.println("All fields are required.");
            return;
        }

        String query = """
                INSERT INTO Reviews (user_id, destination_id, rating, review_text, created_at)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, Integer.parseInt(destinationId));
            stmt.setInt(3, rating);
            stmt.setString(4, reviewText);
            stmt.setString(5, date);

            stmt.executeUpdate();
            System.out.println("Review submitted successfully.");

            clearFormFields();
            loadReviews(); // Reload the reviews list
            updateStats(); // Update the statistics

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to insert review.");
        }
    }

    private void clearFormFields() {
        if (destinationIdInput != null) destinationIdInput.clear();
        if (ratingComboBox != null) ratingComboBox.getSelectionModel().clearSelection();
        if (reviewTextArea != null) reviewTextArea.clear();
        if (reviewDatePicker != null) reviewDatePicker.setValue(null);
    }

    // New methods from the modern design
    private HBox createReviewCard(ReviewData review) {
        HBox card = new HBox();
        card.getStyleClass().add("review-card");

        ImageView avatar = new ImageView(new Image(getClass().getResource("/resources/images/ulun-danu.jpg").toExternalForm()));
        avatar.setFitWidth(50);
        avatar.setFitHeight(50);
        avatar.getStyleClass().add("user-avatar");

        VBox content = new VBox(10);

        HBox header = new HBox(10);
        header.getStyleClass().add("review-header");

        VBox userInfo = new VBox(5);
        userInfo.getStyleClass().add("review-user-info");

        Label username = new Label(review.username());
        username.getStyleClass().add("review-username");

        Label date = new Label(review.createdAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        date.getStyleClass().add("review-date");

        userInfo.getChildren().addAll(username, date);

        Label rating = new Label("★".repeat(review.rating()));
        rating.getStyleClass().add("review-rating");

        header.getChildren().addAll(userInfo, rating);

        Label destination = new Label("Review for " + review.destinationName());
        destination.getStyleClass().add("review-destination");

        Label reviewText = new Label(review.reviewText());
        reviewText.getStyleClass().add("review-content");
        reviewText.setWrapText(true);

        content.getChildren().addAll(header, destination, reviewText);
        card.getChildren().addAll(avatar, content);

        return card;
    }

    private void updateStats() {
        String query = "SELECT COUNT(*) as total FROM Reviews WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, SessionManager.getInstance().getCurrentUserId());
            ResultSet rs = stmt.executeQuery();

            if (rs.next() && totalReviewsText != null) {
                totalReviewsText.setText(String.valueOf(rs.getInt("total")));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void filterReviews() {
        String filter = filterComboBox.getValue();
        loadReviews(); // Implement proper filtering based on selection
    }

    private void openReviewForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/user/form_visitor_review.fxml"));
            Parent reviewFormRoot = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Write a Review");
            stage.setScene(new Scene(reviewFormRoot));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to open review form.");
        }
    }
}