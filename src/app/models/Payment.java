package app.models;

import javafx.beans.property.*;
import java.time.LocalDateTime;

public class Payment {

    private final IntegerProperty paymentId;
    private final IntegerProperty bookingId;
    private final ObjectProperty<LocalDateTime> paymentDate;
    private final DoubleProperty paymentAmount;
    private final StringProperty paymentMethod;
    private final StringProperty status;
    private final ObjectProperty<LocalDateTime> createdAt;

    // Constructor
    public Payment(int paymentId, int bookingId, LocalDateTime paymentDate, double paymentAmount, String paymentMethod, String status, LocalDateTime createdAt) {
        this.paymentId = new SimpleIntegerProperty(paymentId);
        this.bookingId = new SimpleIntegerProperty(bookingId);
        this.paymentDate = new SimpleObjectProperty<>(paymentDate);
        this.paymentAmount = new SimpleDoubleProperty(paymentAmount);
        this.paymentMethod = new SimpleStringProperty(paymentMethod);
        this.status = new SimpleStringProperty(status);
        this.createdAt = new SimpleObjectProperty<>(createdAt);
    }

    // Getters and Setters using Property methods

    // Payment ID
    public int getPaymentId() { return paymentId.get(); }
    public void setPaymentId(int paymentId) { this.paymentId.set(paymentId); }
    public IntegerProperty paymentIdProperty() { return paymentId; }

    // Booking ID
    public int getBookingId() { return bookingId.get(); }
    public void setBookingId(int bookingId) { this.bookingId.set(bookingId); }
    public IntegerProperty bookingIdProperty() { return bookingId; }

    // Payment Date
    public LocalDateTime getPaymentDate() { return paymentDate.get(); }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate.set(paymentDate); }
    public ObjectProperty<LocalDateTime> paymentDateProperty() { return paymentDate; }

    // Payment Amount
    public double getPaymentAmount() { return paymentAmount.get(); }
    public void setPaymentAmount(double paymentAmount) { this.paymentAmount.set(paymentAmount); }
    public DoubleProperty paymentAmountProperty() { return paymentAmount; }

    // Payment Method
    public String getPaymentMethod() { return paymentMethod.get(); }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod.set(paymentMethod); }
    public StringProperty paymentMethodProperty() { return paymentMethod; }

    // Status
    public String getStatus() { return status.get(); }
    public void setStatus(String status) { this.status.set(status); }
    public StringProperty statusProperty() { return status; }

    // Created At
    public LocalDateTime getCreatedAt() { return createdAt.get(); }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt.set(createdAt); }
    public ObjectProperty<LocalDateTime> createdAtProperty() { return createdAt; }
}
