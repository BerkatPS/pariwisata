package app.models;

import javafx.beans.property.*;

import java.time.LocalDateTime;

public class Booking {
    private final IntegerProperty bookingId;
    private final IntegerProperty userId;
    private final IntegerProperty packageId;
    private final ObjectProperty<LocalDateTime> bookingDate;
    private final DoubleProperty totalAmount;
    private final StringProperty status;

    private final ObjectProperty<LocalDateTime> createdAt;
    private final ObjectProperty<LocalDateTime> updatedAt;

    // Constructor
    public Booking(int bookingId, int userId, int packageId, LocalDateTime bookingDate, double totalAmount, String status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.bookingId = new SimpleIntegerProperty(bookingId);
        this.userId = new SimpleIntegerProperty(userId);
        this.packageId = new SimpleIntegerProperty(packageId);
        this.bookingDate = new SimpleObjectProperty<>(bookingDate);
        this.totalAmount = new SimpleDoubleProperty(totalAmount);
        this.status = new SimpleStringProperty(status);
        this.createdAt = new SimpleObjectProperty<>(createdAt);
        this.updatedAt = new SimpleObjectProperty<>(updatedAt);
    }

    // Getters and Setters for Booking ID
    public int getBookingId() { return bookingId.get(); }
    public void setBookingId(int bookingId) { this.bookingId.set(bookingId); }
    public IntegerProperty bookingIdProperty() { return bookingId; }

    // Getters and Setters for User ID
    public int getUserId() { return userId.get(); }
    public void setUserId(int userId) { this.userId.set(userId); }
    public IntegerProperty userIdProperty() { return userId; }

    // Getters and Setters for Package ID
    public int getPackageId() { return packageId.get(); }
    public void setPackageId(int packageId) { this.packageId.set(packageId); }
    public IntegerProperty packageIdProperty() { return packageId; }

    // Getters and Setters for Booking Date
    public LocalDateTime getBookingDate() { return bookingDate.get(); }
    public void setBookingDate(LocalDateTime bookingDate) { this.bookingDate.set(bookingDate); }
    public ObjectProperty<LocalDateTime> bookingDateProperty() { return bookingDate; }

    // Getters and Setters for Total Amount
    public double getTotalAmount() { return totalAmount.get(); }
    public void setTotalAmount(double totalAmount) { this.totalAmount.set(totalAmount); }
    public DoubleProperty totalAmountProperty() { return totalAmount; }

    // Getters and Setters for Status
    public String getStatus() { return status.get(); }
    public void setStatus(String status) { this.status.set(status); }
    public StringProperty statusProperty() { return status; }

    // Getters and Setters for Created At
    public LocalDateTime getCreatedAt() { return createdAt.get(); }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt.set(createdAt); }
    public ObjectProperty<LocalDateTime> createdAtProperty() { return createdAt; }

    // Getters and Setters for Updated At
    public LocalDateTime getUpdatedAt() { return updatedAt.get(); }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt.set(updatedAt); }
    public ObjectProperty<LocalDateTime> updatedAtProperty() { return updatedAt; }
}
