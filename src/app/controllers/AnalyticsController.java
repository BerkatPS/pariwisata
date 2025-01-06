package app.controllers;

import app.models.Booking;
import app.models.Payment;
import app.models.User;
import app.utils.DBConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

public class AnalyticsController {

    private Connection connection;

    @FXML
    private Label totalRevenue;
    @FXML
    private Label totalBookings;
    @FXML
    private Label todaysBookings;
    @FXML
    private Label totalUsers;

    public AnalyticsController() throws SQLException {
        this.connection = DBConnection.getConnection(); // Inisialisasi koneksi
    }

    @FXML
    public void initialize() {
        totalRevenue.setText(getTotalRevenue());
        totalBookings.setText(String.valueOf(getTotalBookings()));
        todaysBookings.setText(String.valueOf(getTodaysBookings()));
        totalUsers.setText(String.valueOf(getTotalUsers()));
    }


    public String getTotalRevenue() {
        BigDecimal totalRevenue = BigDecimal.ZERO;
        String query = """
        SELECT SUM(total_amount) AS total 
        FROM Bookings 
        WHERE status = 'Confirmed'
    """;

        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                totalRevenue = resultSet.getBigDecimal("total");
                if (totalRevenue == null) {
                    totalRevenue = BigDecimal.ZERO;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return formatCurrency(totalRevenue);
    }

    // Metode bantuan untuk format mata uang Rupiah
    private String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            amount = BigDecimal.ZERO;
        }

        Locale indonesia = new Locale("id", "ID");
        NumberFormat rupiahFormat = NumberFormat.getCurrencyInstance(indonesia);
        rupiahFormat.setCurrency(Currency.getInstance("IDR"));

        return rupiahFormat.format(amount);
    }

    public int getTotalBookings() {
        int totalBookings = 0;
        String query = "SELECT COUNT(*) AS total FROM Bookings";

        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                totalBookings = resultSet.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return totalBookings;
    }

    public int getTodaysBookings() {
        int todaysBookings = 0;
        String query = "SELECT COUNT(*) AS total FROM Bookings WHERE DATE(booking_date) = CURDATE()";

        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                todaysBookings = resultSet.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return todaysBookings;
    }

    // Method to fetch user statistics if needed
    public int getTotalUsers() {
        int totalUsers = 0;
        String query = "SELECT COUNT(*) AS total FROM Users";

        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                totalUsers = resultSet.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return totalUsers;
    }
}
