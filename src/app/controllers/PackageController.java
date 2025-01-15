package app.controllers;

import app.models.Package;
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
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PackageController {

    @FXML private TableView<Package> packageTable;
    @FXML private TableColumn<Package, Integer> packageIdColumn;
    @FXML private TableColumn<Package, String> packageNameColumn;
    @FXML private TableColumn<Package, String> descriptionColumn;
    @FXML private TableColumn<Package, Double> priceColumn;
    @FXML private TableColumn<Package, String> durationColumn;
    @FXML private TableColumn<Package, Void> actionColumn;
    @FXML private TextField searchField;
    @FXML private Button addPackageButton;

    private ObservableList<Package> packageList = FXCollections.observableArrayList();
    private FilteredList<Package> filteredPackages;

    @FXML
    public void initialize() {
        setupTableColumns();
        setupSearchFilter();
        loadPackages();
        setupContextMenu();
        addPackageButton.setOnAction(event -> openAddPackageForm());
    }

    private void setupTableColumns() {
        packageIdColumn.setCellValueFactory(cellData -> cellData.getValue().packageIdProperty().asObject());
        packageNameColumn.setCellValueFactory(cellData -> cellData.getValue().packageNameProperty());
        descriptionColumn.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        priceColumn.setCellValueFactory(cellData -> cellData.getValue().priceProperty().asObject());
        durationColumn.setCellValueFactory(cellData -> cellData.getValue().durationProperty());

        // Format price column
        priceColumn.setCellFactory(column -> new TableCell<Package, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("Rp %,.2f", price));
                }
            }
        });

        setupActionColumn();
    }

    private void setupContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem editItem = new MenuItem("Edit");
        MenuItem deleteItem = new MenuItem("Delete");
        MenuItem refreshItem = new MenuItem("Refresh");

        editItem.setOnAction(e -> {
            Package selectedPackage = packageTable.getSelectionModel().getSelectedItem();
            if (selectedPackage != null) {
            }
        });

        deleteItem.setOnAction(e -> {
            Package selectedPackage = packageTable.getSelectionModel().getSelectedItem();
            if (selectedPackage != null) {
                handleDeletePackage(selectedPackage);
            }
        });

        refreshItem.setOnAction(e -> loadPackages());

        packageTable.setContextMenu(contextMenu);
    }

    private void setupActionColumn() {
        actionColumn.setCellFactory(column -> new TableCell<Package, Void>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");

            {
                editButton.getStyleClass().add("button-secondary");
                deleteButton.getStyleClass().add("button-danger");

                editButton.setOnAction(event -> {
                    Package pkg = getTableView().getItems().get(getIndex());
                });

                deleteButton.setOnAction(event -> {
                    Package pkg = getTableView().getItems().get(getIndex());
                    handleDeletePackage(pkg);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox container = new HBox(5, editButton, deleteButton);
                    setGraphic(container);
                }
            }
        });
    }

    private void setupSearchFilter() {
        filteredPackages = new FilteredList<>(packageList, p -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredPackages.setPredicate(pkg -> {
                if (newValue == null || newValue.isEmpty()) return true;

                String lowerCaseFilter = newValue.toLowerCase();
                return pkg.getPackageName().toLowerCase().contains(lowerCaseFilter) ||
                        pkg.getDescription().toLowerCase().contains(lowerCaseFilter) ||
                        pkg.getDuration().toLowerCase().contains(lowerCaseFilter);
            });
        });
        packageTable.setItems(filteredPackages);
    }


    @FXML
    private void openAddPackageForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/admin/package_form.fxml"));
            Parent packageFormRoot = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Package Form");
            stage.setScene(new Scene(packageFormRoot));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to open Package form.");
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open package form: " + e.getMessage());
        }
    }

    @FXML
    private void loadPackages() {
        packageList.clear();
        String query = """
            SELECT p.*, d.name AS destination_name 
            FROM Packages p
            JOIN Destinations d ON p.destination_id = d.destination_id
            ORDER BY p.package_id DESC
        """;

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Package pkg = new Package(
                        rs.getInt("package_id"),
                        rs.getString("package_name"),
                        rs.getString("description"),
                        rs.getDouble("price"),
                        rs.getString("duration")
                );
                packageList.add(pkg);
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Database Error",
                    "Failed to load packages: " + e.getMessage());
        }
    }

    private void handleDeletePackage(Package pkg) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Deletion");
        confirmDialog.setHeaderText("Delete Package");
        confirmDialog.setContentText("Are you sure you want to delete this package?");

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try (Connection conn = DBConnection.getConnection()) {
                    String query = "DELETE FROM Packages WHERE package_id = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(query)) {
                        stmt.setInt(1, pkg.getPackageId());
                        stmt.executeUpdate();
                        loadPackages();
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Package deleted successfully.");
                    }
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete package: " + e.getMessage());
                }
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}