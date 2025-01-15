package app.controllers;

import app.models.Transport;
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

public class TransportController {
    @FXML private TableView<Transport> transportTable;
    @FXML private TableColumn<Transport, Integer> transportIdColumn;
    @FXML private TableColumn<Transport, String> transportTypeColumn;
    @FXML private TableColumn<Transport, Integer> destinationIdColumn;
    @FXML private TableColumn<Transport, Double> priceColumn;
    @FXML private TableColumn<Transport, LocalDateTime> departureTimeColumn;
    @FXML private TableColumn<Transport, LocalDateTime> arrivalTimeColumn;
    @FXML private TableColumn<Transport, Void> actionColumn;

    @FXML private TextField searchField;
//    @FXML private Button addTransportButton;
//    @FXML private Label totalTransportsLabel;
//    @FXML private Label totalRevenueLabel;

    private ObservableList<Transport> transportList = FXCollections.observableArrayList();
    private FilteredList<Transport> filteredTransports;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @FXML
    public void initialize() {
        setupTableColumns();
        setupSearchFilter();
        setupActionColumn();
        loadTransports();

//        addTransportButton.setOnAction( event -> openAddTransportForm());
    }

    private void setupTableColumns() {
        transportIdColumn.setCellValueFactory(cellData -> cellData.getValue().transportIdProperty().asObject());
        transportTypeColumn.setCellValueFactory(cellData -> cellData.getValue().transportTypeProperty());
        destinationIdColumn.setCellValueFactory(cellData -> cellData.getValue().destinationIdProperty().asObject());
        priceColumn.setCellValueFactory(cellData -> cellData.getValue().priceProperty().asObject());
    }

    private void setupSearchFilter() {
        filteredTransports = new FilteredList<>(transportList, p -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredTransports.setPredicate(transport -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return transport.getTransportType().toLowerCase().contains(lowerCaseFilter) ||
                        String.valueOf(transport.getDestinationId()).contains(lowerCaseFilter) ||
                        String.valueOf(transport.getPrice()).contains(lowerCaseFilter);
            });
        });
        transportTable.setItems(filteredTransports);
    }

    private void setupActionColumn() {
        actionColumn.setCellFactory(col -> new TableCell<Transport, Void>() {
            private final Button updateButton = new Button("Update");
            private final Button deleteButton = new Button("Delete");

            {
                updateButton.getStyleClass().add("button-secondary");
                deleteButton.getStyleClass().add("button-danger");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Transport transport = getTableRow().getItem();
                    updateButton.setOnAction(e -> handleUpdateTransport(transport));
                    deleteButton.setOnAction(e -> handleDeleteTransport(transport));
                    HBox hBox = new HBox(updateButton, deleteButton);
                    setGraphic(hBox);
                }
            }
        });
    }

    private void loadTransports() {
        transportList.clear();
        try (Connection connection = DBConnection.getConnection()) {
            if (connection != null) {
                ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM Transport");
                while (resultSet.next()) {
                    transportList.add(new Transport(
                            resultSet.getInt("transport_id"),
                            resultSet.getString("transport_type"),
                            resultSet.getInt("destination_id"),
                            resultSet.getDouble("price"),
                            resultSet.getTimestamp("departure_time"),
                            resultSet.getTimestamp("arrival_time")
                    ));
                }
                updateStatistics();
                transportTable.setItems(transportList);
            } else {
                showAlert("Koneksi ke database gagal!", Alert.AlertType.ERROR);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error loading transports: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void updateStatistics() {
//        totalTransportsLabel.setText(String.valueOf(transportList.size()));
        double totalRevenue = transportList.stream().mapToDouble(Transport::getPrice).sum();
//        totalRevenueLabel.setText(NumberFormat.getCurrencyInstance(new Locale("id", "ID")).format(totalRevenue));
    }

    private void handleUpdateTransport(Transport transport) {

    }

    private void handleDeleteTransport(Transport transport) {
        try (Connection connection = DBConnection.getConnection()) {
            if (connection != null) {
                PreparedStatement statement = connection.prepareStatement("DELETE FROM Transport WHERE transport_id = ?");
                statement.setInt(1, transport.getTransportId());
                statement.executeUpdate();
                loadTransports();
                showAlert("Transport deleted successfully!", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Koneksi ke database gagal!", Alert.AlertType.ERROR);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error deleting transport: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void openAddTransportForm() {
        // Logic to open add transport form
    }

    private void showAlert(String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}