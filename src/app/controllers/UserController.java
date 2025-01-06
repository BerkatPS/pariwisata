package app.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import app.models.User;
import app.utils.DBConnection;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class UserController {

    @FXML
    private TableView<User> userTable;

    @FXML
    private TableColumn<User, Integer> userIdColumn;
    @FXML
    private TableColumn<User, String> nameColumn, emailColumn, phoneColumn, roleColumn;

    @FXML
    private TextField nameField, emailField, phoneField, roleField;

    @FXML
    private Button addUserButton, updateUserButton, deleteUserButton;

    private ObservableList<User> userList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        // Set up the TableView columns
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));

        // Load user data from the database
        loadUsers();
    }

    @FXML
    private void loadUsers() {
        userList.clear(); // Kosongkan daftar pengguna saat memuat ulang data
        try (Connection connection = DBConnection.getConnection()) {
            // Memeriksa jika koneksi berhasil
            if (connection != null) {
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT * FROM Users");

                // Iterasi melalui hasil query dan tambahkan setiap pengguna ke ObservableList
                while (resultSet.next()) {
                    userList.add(new User(
                            resultSet.getInt("user_id"),
                            resultSet.getString("name"),
                            resultSet.getString("email"),
                            resultSet.getString("phone_number"),
                            resultSet.getString("role")
                    ));
                }

                // Set ObservableList ke TableView
                userTable.setItems(userList);
            } else {
                // Tampilkan pesan kepada pengguna jika koneksi gagal
                System.out.println("Koneksi ke database gagal!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddUser() {
        // Logika untuk menambah pengguna baru ke database
        try (Connection connection = DBConnection.getConnection()) {
            String query = "INSERT INTO Users (name, email, phone_number, role) VALUES (?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, nameField.getText());
            statement.setString(2, emailField.getText());
            statement.setString(3, phoneField.getText());
            statement.setString(4, roleField.getText());
            statement.executeUpdate();
            loadUsers(); // Refresh data
            clearFields(); // Clear input fields
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpdateUser() {
        // Mendapatkan pengguna yang dipilih dari TableView
        User selectedUser = userTable.getSelectionModel().getSelectedItem();

        if (selectedUser != null) {
            try (Connection connection = DBConnection.getConnection()) {
                String query = "UPDATE Users SET name = ?, email = ?, phone_number = ?, role = ? WHERE user_id = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, nameField.getText());
                statement.setString(2, emailField.getText());
                statement.setString(3, phoneField.getText());
                statement.setString(4, roleField.getText());
                statement.setInt(5, selectedUser.getUserId());
                statement.executeUpdate();
                loadUsers(); // Refresh data setelah memperbarui
                clearFields(); // Clear input fields
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleDeleteUser() {
        // Mendapatkan pengguna yang dipilih dari TableView
        User selectedUser = userTable.getSelectionModel().getSelectedItem();

        if (selectedUser != null) {
            try (Connection connection = DBConnection.getConnection()) {
                String query = "DELETE FROM Users WHERE user_id = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setInt(1, selectedUser.getUserId());
                statement.executeUpdate();
                loadUsers(); // Refresh data setelah menghapus
                clearFields(); // Clear input fields
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Clear input fields after any operation
    private void clearFields() {
        nameField.clear();
        emailField.clear();
        phoneField.clear();
        roleField.clear();
    }
}
