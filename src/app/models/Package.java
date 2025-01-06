package app.models;

import javafx.beans.property.*;

import java.time.LocalDateTime;

public class Package {
    private final IntegerProperty packageId;
    private final StringProperty packageName;
    private final StringProperty description;
    private final DoubleProperty price;
    private final StringProperty duration;
    private final ObjectProperty<LocalDateTime> createdAt;
    private final ObjectProperty<LocalDateTime> updatedAt;

    // Constructor
    public Package(int packageId, String packageName, String description, double price, String duration) {
        this.packageId = new SimpleIntegerProperty(packageId);
        this.packageName = new SimpleStringProperty(packageName);
        this.description = new SimpleStringProperty(description);
        this.price = new SimpleDoubleProperty(price);
        this.duration = new SimpleStringProperty(duration);
        this.createdAt = new SimpleObjectProperty<>(LocalDateTime.now());
        this.updatedAt = new SimpleObjectProperty<>(LocalDateTime.now());
    }

    // Getters and Setters for Package ID
    public int getPackageId() { return packageId.get(); }  // Use .get() to retrieve value
    public void setPackageId(int packageId) { this.packageId.set(packageId); }  // Use .set() to set value
    public IntegerProperty packageIdProperty() { return packageId; }

    // Getters and Setters for Package Name
    public String getPackageName() { return packageName.get(); }  // Use .get() to retrieve value
    public void setPackageName(String packageName) { this.packageName.set(packageName); }  // Use .set() to set value
    public StringProperty packageNameProperty() { return packageName; }

    // Getters and Setters for Description
    public String getDescription() { return description.get(); }  // Use .get() to retrieve value
    public void setDescription(String description) { this.description.set(description); }  // Use .set() to set value
    public StringProperty descriptionProperty() { return description; }

    // Getters and Setters for Price
    public double getPrice() { return price.get(); }  // Use .get() to retrieve value
    public void setPrice(double price) { this.price.set(price); }  // Use .set() to set value
    public DoubleProperty priceProperty() { return price; }

    // Getters and Setters for Duration
    public String getDuration() { return duration.get(); }  // Use .get() to retrieve value
    public void setDuration(String duration) { this.duration.set(duration); }  // Use .set() to set value
    public StringProperty durationProperty() { return duration; }

    // Getters and Setters for Created At
    public LocalDateTime getCreatedAt() { return createdAt.get(); }  // Use .get() to retrieve value
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt.set(createdAt); }  // Use .set() to set value
    public ObjectProperty<LocalDateTime> createdAtProperty() { return createdAt; }

    // Getters and Setters for Updated At
    public LocalDateTime getUpdatedAt() { return updatedAt.get(); }  // Use .get() to retrieve value
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt.set(updatedAt); }  // Use .set() to set value
    public ObjectProperty<LocalDateTime> updatedAtProperty() { return updatedAt; }
}
