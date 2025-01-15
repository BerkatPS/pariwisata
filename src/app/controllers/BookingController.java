package app.controllers;

import app.models.Booking;
import app.utils.DBConnection;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class BookingController {
    @FXML private TableView<Booking> bookingTable;
    @FXML private TableColumn<Booking, Integer> bookingIdColumn;
    @FXML private TableColumn<Booking, Integer> userIdColumn;
    @FXML private TableColumn<Booking, Integer> packageIdColumn;
    @FXML private TableColumn<Booking, String> bookingDateColumn;
    @FXML private TableColumn<Booking, Double> totalAmountColumn;
    @FXML private TableColumn<Booking, String> statusColumn;
    @FXML private TableColumn<Booking, Void> actionColumn;

    @FXML private TextField searchField;
    @FXML private Label totalBookingsLabel;
    @FXML private Label totalRevenueLabel;

    private ObservableList<Booking> bookingList = FXCollections.observableArrayList();
    private FilteredList<Booking> filteredBookings;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @FXML
    public void initialize() {
        setupTableColumns();
        setupSearchFilter();
        setupActionColumn();
        loadBookings();

    }

    private void setupTableColumns() {
        bookingIdColumn.setCellValueFactory(cellData -> cellData.getValue().bookingIdProperty().asObject());
        userIdColumn.setCellValueFactory(cellData -> cellData.getValue().userIdProperty().asObject());
        packageIdColumn.setCellValueFactory(cellData -> cellData.getValue().packageIdProperty().asObject());
        bookingDateColumn.setCellValueFactory(cellData -> cellData.getValue().bookingDateProperty().asString());
        totalAmountColumn.setCellValueFactory(cellData -> cellData.getValue().totalAmountProperty().asObject());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
    }

    private void setupSearchFilter() {
        filteredBookings = new FilteredList<>(bookingList, p -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredBookings.setPredicate(booking -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return String.valueOf(booking.getBookingId()).contains(lowerCaseFilter) ||
                        String.valueOf(booking.getUserId()).contains(lowerCaseFilter) ||
                        booking.getStatus().toLowerCase().contains(lowerCaseFilter);
            });
        });
        bookingTable.setItems(filteredBookings);
    }

    private void setupActionColumn() {
        actionColumn.setCellFactory(col -> new TableCell<Booking, Void>() {
            private final Button confirmButton = new Button("Confirm");
            private final Button cancelButton = new Button("Cancel");

            {
                confirmButton.getStyleClass().add("button-secondary");
                cancelButton.getStyleClass().add("button-danger");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Booking booking = getTableRow().getItem();
                    confirmButton.setOnAction(e -> handleConfirmBooking(booking));
                    cancelButton.setOnAction(e -> handleCancelBooking(booking));
                    HBox hBox = new HBox(confirmButton, cancelButton);
                    setGraphic(hBox);
                }
            }
        });
    }

    private void loadBookings() {
        bookingList.clear();
        try (Connection connection = DBConnection.getConnection()) {
            if (connection != null) {
                ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM Bookings");
                while (resultSet.next()) {
                    LocalDateTime bookingDate = LocalDateTime.parse(resultSet.getString("booking_date"), DATE_FORMATTER);
                    bookingList.add(new Booking(
                            resultSet.getInt("booking_id"),
                            resultSet.getInt("user_id"),
                            resultSet.getInt("package_id"),
                            bookingDate,
                            resultSet.getDouble("total_amount"),
                            resultSet.getString("status"),
                            resultSet.getTimestamp("created_at").toLocalDateTime(),
                            resultSet.getTimestamp("updated_at").toLocalDateTime()

                    ));
                }
                updateStatistics();
                bookingTable.setItems(bookingList);
            } else {
                showAlert("Koneksi ke database gagal!", Alert.AlertType.ERROR);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error loading bookings: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void updateStatistics() {
        totalBookingsLabel.setText(String.valueOf(bookingList.size()));
        double totalRevenue = bookingList.stream().mapToDouble(Booking::getTotalAmount).sum();
        totalRevenueLabel.setText(NumberFormat.getCurrencyInstance(new Locale("id", "ID")).format(totalRevenue));
    }

    private void handleConfirmBooking(Booking booking) {
        // Implement confirmation logic
    }

    private void handleCancelBooking(Booking booking) {
        // Implement cancellation logic
    }

    @FXML
    private void openBookingForm() {
        // Logic to open booking form
    }

    private void showAlert(String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}