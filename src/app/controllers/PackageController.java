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
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class PackageController {

    @FXML
    private TableView<Package> packageTable;
    @FXML
    private TableColumn<Package, Integer> packageIdColumn;
    @FXML
    private TableColumn<Package, String> packageNameColumn;
    @FXML
    private TableColumn<Package, String> descriptionColumn;
    @FXML
    private TableColumn<Package, Double> priceColumn;
    @FXML
    private TableColumn<Package, String> durationColumn;
    @FXML
    private TableColumn<Package, Void> actionColumn;
    @FXML
    private TextField searchField;

    @FXML
    private Button addPackageButton;

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

        // Format price column to show Rupiah
        priceColumn.setCellFactory(column -> new TableCell<Package, Double>() {
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
        actionColumn.setCellFactory(column -> new TableCell<Package, Void>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            {
                editButton.getStyleClass().add("button-secondary");
                deleteButton.getStyleClass().add("button-danger");

                editButton.setOnAction(event -> {
                    Package pkg = getTableView().getItems().get(getIndex());
//                    handleEditPackage(pkg);
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
                    var container = new HBox(5);
                    container.getChildren().addAll(editButton, deleteButton);
                    setGraphic(container);
                }
            }
        });
    }

    private void setupSearchFilter() {
        filteredPackages = new FilteredList<>(packageList, p -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredPackages.setPredicate(package_ -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();
                return package_.getPackageName().toLowerCase().contains(lowerCaseFilter) ||
                        package_.getDescription().toLowerCase().contains(lowerCaseFilter) ||
                        package_.getDuration().toLowerCase().contains(lowerCaseFilter);
            });
        });
        packageTable.setItems(filteredPackages);
    }

    @FXML
    private void loadPackages() {
        packageList.clear();
        try (Connection connection = DBConnection.getConnection()) {
            if (connection != null) {
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT * FROM Packages ORDER BY package_id DESC");

                while (resultSet.next()) {
                    packageList.add(new Package(
                            resultSet.getInt("package_id"),
                            resultSet.getString("package_name"),
                            resultSet.getString("description"),
                            resultSet.getDouble("price"),
                            resultSet.getString("duration")
                    ));
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to connect to database");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load packages: " + e.getMessage());
        }
    }

    private void setupContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem editItem = new MenuItem("Edit");
        MenuItem deleteItem = new MenuItem("Delete");
        MenuItem refreshItem = new MenuItem("Refresh");

        editItem.setOnAction(e -> {
            Package selectedPackage = packageTable.getSelectionModel().getSelectedItem();
//

        });

        deleteItem.setOnAction(e -> {
            Package selectedPackage = packageTable.getSelectionModel().getSelectedItem();
            if (selectedPackage != null) {
                handleDeletePackage(selectedPackage);
            }
        });

        refreshItem.setOnAction(e -> loadPackages());

        contextMenu.getItems().addAll(editItem, deleteItem, new SeparatorMenuItem(), refreshItem);
        packageTable.setContextMenu(contextMenu);
    }

    @FXML
    private void openAddPackageForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/admin/package_form.fxml"));
            Parent transportFormRoot = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Package Form");
            stage.setScene(new Scene(transportFormRoot));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to open Package form.");
        }
    }

//    private void handleEditPackage(Package pkg) {
//        try {
//            PackageFormController formController = (PackageFormController) SceneManager.getInstance()
//                    .loadScene("package-form", "/views/package/package-form.fxml");
//            formController.initForEdit(pkg);
//        } catch (Exception e) {
//            showAlert(Alert.AlertType.ERROR, "Error", "Could not open edit package form: " + e.getMessage());
//        }
//    }

    private void handleDeletePackage(Package pkg) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Delete");
        confirmDialog.setHeaderText("Delete Package");
        confirmDialog.setContentText("Are you sure you want to delete this package?");

        if (confirmDialog.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try (Connection connection = DBConnection.getConnection()) {
                String query = "DELETE FROM Packages WHERE package_id = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setInt(1, pkg.getPackageId());
                statement.executeUpdate();
                loadPackages();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Package deleted successfully");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete package: " + e.getMessage());
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