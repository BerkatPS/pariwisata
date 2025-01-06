package app.controllers;

import app.models.Destination;
import app.utils.DBConnection;
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
import java.sql.Statement;

public class DestinationController {

    @FXML private TableView<Destination> destinationTable;
    @FXML private TableColumn<Destination, Integer> destinationIdColumn;
    @FXML private TableColumn<Destination, String> nameColumn;
    @FXML private TableColumn<Destination, String> locationColumn;
    @FXML private TableColumn<Destination, String> descriptionColumn;
    @FXML private TableColumn<Destination, Double> priceColumn;
    @FXML private TableColumn<Destination, Void> actionColumn;
    @FXML private TextField searchField;
    @FXML private Button addDestinationButton;

    private ObservableList<Destination> destinationList = FXCollections.observableArrayList();
    private FilteredList<Destination> filteredDestinations;

    @FXML
    public void initialize() {
        setupTableColumns();
        setupSearchFilter();
        loadDestinations();
        setupContextMenu();
        addDestinationButton.setOnAction(event -> openAddDestinationForm());
    }

    private void setupTableColumns() {
        destinationIdColumn.setCellValueFactory(cellData -> cellData.getValue().destinationIdProperty().asObject());
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        locationColumn.setCellValueFactory(cellData -> cellData.getValue().locationProperty());
        descriptionColumn.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        priceColumn.setCellValueFactory(cellData -> cellData.getValue().pricePerEntryProperty().asObject());

        // Format price column to show Rupiah
        priceColumn.setCellFactory(column -> new TableCell<Destination, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("Rp%,.2f", price));
                }
            }
        });

        setupActionColumn();
    }

    private void setupActionColumn() {
        actionColumn.setCellFactory(column -> new TableCell<Destination, Void>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            {
                editButton.getStyleClass().add("button-secondary");
                deleteButton.getStyleClass().add("button-danger");

                editButton.setOnAction(event -> {
                    Destination destination = getTableView().getItems().get(getIndex());
                    handleEditDestination(destination);
                });

                deleteButton.setOnAction(event -> {
                    Destination destination = getTableView().getItems().get(getIndex());
                    handleDeleteDestination(destination);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    var container = new HBox(5);
                    container.getChildren().addAll(editButton, deleteButton);
                    setGraphic(container);
                }
            }
        });
    }

    private void setupSearchFilter() {
        filteredDestinations = new FilteredList<>(destinationList, p -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredDestinations.setPredicate(destination -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();
                return destination.getName().toLowerCase().contains(lowerCaseFilter) ||
                        destination.getLocation().toLowerCase().contains(lowerCaseFilter) ||
                        destination.getDescription().toLowerCase().contains(lowerCaseFilter);
            });
        });
        destinationTable.setItems(filteredDestinations);
    }

    private void loadDestinations() {
        destinationList.clear();
        try (Connection connection = DBConnection.getConnection()) {
            if (connection != null) {
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(" SELECT * FROM Destinations ORDER BY destination_id DESC");

                while (resultSet.next()) {
                    destinationList.add(new Destination(
                            resultSet.getInt("destination_id"),
                            resultSet.getString("name"),
                            resultSet.getString("location"),
                            resultSet.getString("description"),
                            resultSet.getString("opening_hours"),
                            resultSet.getDouble("price_per_entry"),
                            resultSet.getString("image_url"),
                            resultSet.getTimestamp("created_at").toLocalDateTime(),
                            resultSet.getTimestamp("updated_at").toLocalDateTime()


                    ));
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to connect to database");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load destinations: " + e.getMessage());
        }
    }

    private void setupContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem editItem = new MenuItem("Edit");
        MenuItem deleteItem = new MenuItem("Delete");
        MenuItem refreshItem = new MenuItem("Refresh");

        editItem.setOnAction(e -> {
            Destination selectedDestination = destinationTable.getSelectionModel().getSelectedItem();
            if (selectedDestination != null) {
                handleEditDestination(selectedDestination);
            }
        });

        deleteItem.setOnAction(e -> {
            Destination selectedDestination = destinationTable.getSelectionModel().getSelectedItem();
            if (selectedDestination != null) {
                handleDeleteDestination(selectedDestination);
            }
        });

        refreshItem.setOnAction(e -> loadDestinations());

        contextMenu.getItems().addAll(editItem, deleteItem, new SeparatorMenuItem(), refreshItem);
        destinationTable.setContextMenu(contextMenu);
    }

    @FXML
    private void openAddDestinationForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/admin/destination_form.fxml"));
            Parent destinationFormRoot = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Destination Form");
            stage.setScene(new Scene(destinationFormRoot));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to open Destination form.");
        }
    }

    private void handleEditDestination(Destination destination) {
        // Implement edit functionality here
    }

    private void handleDeleteDestination(Destination destination) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Delete");
        confirmDialog.setHeaderText("Delete Destination");
        confirmDialog.setContentText("Are you sure you want to delete this destination?");

        if (confirmDialog.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try (Connection connection = DBConnection.getConnection()) {
                String query = "DELETE FROM Destinations WHERE destination_id = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setInt(1, destination.getDestinationId());
                statement.executeUpdate();
                loadDestinations();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Destination deleted successfully");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete destination: " + e.getMessage());
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}