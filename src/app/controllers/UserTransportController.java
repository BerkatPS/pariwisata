package app.controllers;

import app.utils.DBConnection;
import app.utils.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;

public class UserTransportController {

    @FXML
    private TableView<ObservableList<String>> transportTable;

    @FXML
    private TableColumn<ObservableList<String>, String> transportIdColumn;

    @FXML
    private TableColumn<ObservableList<String>, String> transportTypeColumn;

    @FXML
    private TableColumn<ObservableList<String>, String> priceColumn;

    @FXML
    private TableColumn<ObservableList<String>, String> departureTimeColumn;

    @FXML
    private TableColumn<ObservableList<String>, String> arrivalTimeColumn;

    @FXML
    private TableColumn<ObservableList<String>, String> destinationNameColumn;

    @FXML
    private Button backButton;

    @FXML
    private Button addTransportButton;

    private ObservableList<ObservableList<String>> transportData;

    private int destinationId;

    // Set the destination ID
    public void setDestinationId(int destinationId) {
        this.destinationId = destinationId;
    }

    @FXML
    private void initialize() {
        // Initialize the table columns
        transportIdColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(0)));
        transportTypeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(1)));
        priceColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(2)));
        departureTimeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(3)));
        arrivalTimeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(4)));
        destinationNameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(5)));

        loadTransportDataForDestination();

//        addTransportButton.setOnAction(event -> openTransportForm());
        backButton.setOnAction(event -> goBack());
    }

    // Open the transport form
    private void openTransportForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/user/form_visitor_transport.fxml"));
            Parent transportFormRoot = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Transport Form");
            stage.setScene(new Scene(transportFormRoot));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to open transport form.");
        }
    }

    // Load transport data for the specific destination without a model
    private void loadTransportDataForDestination() {
        Integer userId = SessionManager.getInstance().getCurrentUserId();
        if (userId == null) {
            showAlert("Error", "User not logged in.");
            return;
        }

        transportData = FXCollections.observableArrayList();
        String query = """
            SELECT 
                t.transport_id,
                t.transport_type,
                t.price,
                t.departure_time,
                t.arrival_time,
                d.name AS destination_name
            FROM 
                Transport t
            JOIN 
                Destinations d ON t.destination_id = d.destination_id
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {


            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(rs.getString("transport_id"));
                row.add(rs.getString("transport_type"));
                row.add(String.format("%.2f", rs.getDouble("price")));
                row.add(rs.getTimestamp("departure_time").toString());
                row.add(rs.getTimestamp("arrival_time").toString());
                row.add(rs.getString("destination_name"));

                transportData.add(row);
            }
            transportTable.setItems(transportData);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load transport data for the destination.");
        }
    }

    // Go back to the previous page
    private void goBack() {
        try {
            System.out.println("Going back to the previous page...");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Navigation Error", "Unable to go back.");
        }
    }

    // Show alert messages
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
