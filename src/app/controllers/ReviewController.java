package app.controllers;

import app.models.Review;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import app.utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;

public class ReviewController {

    @FXML
    private TableView<Review> reviewTable;

    @FXML
    private TableColumn<Review, Integer> reviewIdColumn;
    @FXML
    private TableColumn<Review, Integer> userIdColumn;
    @FXML
    private TableColumn<Review, Integer> destinationIdColumn;
    @FXML
    private TableColumn<Review, Integer> ratingColumn;
    @FXML
    private TableColumn<Review, String> reviewTextColumn;
    @FXML
    private TableColumn<Review, LocalDateTime> createdAtColumn;

    @FXML
    private TextField userIdField, destinationIdField, ratingField;
    @FXML
    private TextArea reviewTextField;

    @FXML
    private Button addReviewButton, updateReviewButton, deleteReviewButton;

    private ObservableList<Review> reviewList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Menginisialisasi kolom-kolom di TableView
        reviewIdColumn.setCellValueFactory(cellData -> cellData.getValue().reviewIdProperty().asObject());
        userIdColumn.setCellValueFactory(cellData -> cellData.getValue().userIdProperty().asObject());
        destinationIdColumn.setCellValueFactory(cellData -> cellData.getValue().destinationIdProperty().asObject());
        ratingColumn.setCellValueFactory(cellData -> cellData.getValue().ratingProperty().asObject());
        reviewTextColumn.setCellValueFactory(cellData -> cellData.getValue().reviewTextProperty());
        createdAtColumn.setCellValueFactory(cellData -> cellData.getValue().createdAtProperty());

        // Memuat data dari database
        loadReviews();
    }

    @FXML
    private void loadReviews() {
        reviewList.clear(); // Kosongkan daftar sebelum memuat
        try (Connection connection = DBConnection.getConnection()) {
            if (connection != null) {
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT * FROM Reviews");

                while (resultSet.next()) {
                    reviewList.add(new Review(
                            resultSet.getInt("review_id"),
                            resultSet.getInt("user_id"),
                            resultSet.getInt("destination_id"),
                            resultSet.getInt("rating"),
                            resultSet.getString("review_text"),
                            resultSet.getTimestamp("created_at").toLocalDateTime()
                    ));
                }

                reviewTable.setItems(reviewList);
            } else {
                System.out.println("Koneksi ke database gagal!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddReview() {
        try (Connection connection = DBConnection.getConnection()) {
            String query = "INSERT INTO Reviews (user_id, destination_id, rating, review_text, created_at) " +
                    "VALUES (?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, Integer.parseInt(userIdField.getText()));
            statement.setInt(2, Integer.parseInt(destinationIdField.getText()));
            statement.setInt(3, Integer.parseInt(ratingField.getText()));
            statement.setString(4, reviewTextField.getText());
            statement.setTimestamp(5, java.sql.Timestamp.valueOf(LocalDateTime.now()));
            statement.executeUpdate();
            loadReviews(); // Memperbarui tabel
            clearFields(); // Bersihkan input setelah menambah
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpdateReview() {
        Review selectedReview = reviewTable.getSelectionModel().getSelectedItem();
        if (selectedReview != null) {
            try (Connection connection = DBConnection.getConnection()) {
                String query = "UPDATE Reviews SET user_id = ?, destination_id = ?, rating = ?, review_text = ?, created_at = ? WHERE review_id = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setInt(1, Integer.parseInt(userIdField.getText()));
                statement.setInt(2, Integer.parseInt(destinationIdField.getText()));
                statement.setInt(3, Integer.parseInt(ratingField.getText()));
                statement.setString(4, reviewTextField.getText());
                statement.setTimestamp(5, java.sql.Timestamp.valueOf(LocalDateTime.now()));
                statement.setInt(6, selectedReview.getReviewId());
                statement.executeUpdate();
                loadReviews(); // Memperbarui tabel
                clearFields(); // Bersihkan input setelah memperbarui
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Silakan pilih review untuk diperbarui.");
        }
    }

    @FXML
    private void handleDeleteReview() {
        Review selectedReview = reviewTable.getSelectionModel().getSelectedItem();
        if (selectedReview != null) {
            try (Connection connection = DBConnection.getConnection()) {
                String query = "DELETE FROM Reviews WHERE review_id = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setInt(1, selectedReview.getReviewId());
                statement.executeUpdate();
                loadReviews(); // Memperbarui tabel
                clearFields(); // Bersihkan input setelah menghapus
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Silakan pilih review untuk dihapus.");
        }
    }

    private void clearFields() {
        userIdField.clear();
        destinationIdField.clear();
        ratingField.clear();
        reviewTextField.clear();
    }
}
