package app.models;

import javafx.beans.property.*;
import java.time.LocalDateTime;

public class Destination {

    private final IntegerProperty destinationId;
    private final StringProperty name;
    private final StringProperty description;
    private final StringProperty location;
    private final StringProperty openingHours;
    private final DoubleProperty pricePerEntry;
    private final StringProperty imageUrl;
    private final ObjectProperty<LocalDateTime> createdAt;
    private final ObjectProperty<LocalDateTime> updatedAt;

    // Constructor
    public Destination(int destinationId, String name, String description, String location, String openingHours, double pricePerEntry, String imageUrl, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.destinationId = new SimpleIntegerProperty(destinationId);
        this.name = new SimpleStringProperty(name);
        this.description = new SimpleStringProperty(description);
        this.location = new SimpleStringProperty(location);
        this.openingHours = new SimpleStringProperty(openingHours);
        this.pricePerEntry = new SimpleDoubleProperty(pricePerEntry);
        this.imageUrl = new SimpleStringProperty(imageUrl);
        this.createdAt = new SimpleObjectProperty<>(createdAt);
        this.updatedAt = new SimpleObjectProperty<>(updatedAt);
    }



    // Getters and Setters using Property methods

    public int getDestinationId() { return destinationId.get(); }
    public void setDestinationId(int destinationId) { this.destinationId.set(destinationId); }
    public IntegerProperty destinationIdProperty() { return destinationId; }

    public String getName() { return name.get(); }
    public void setName(String name) { this.name.set(name); }
    public StringProperty nameProperty() { return name; }

    public String getDescription() { return description.get(); }
    public void setDescription(String description) { this.description.set(description); }
    public StringProperty descriptionProperty() { return description; }

    public String getLocation() { return location.get(); }
    public void setLocation(String location) { this.location.set(location); }
    public StringProperty locationProperty() { return location; }

    public String getOpeningHours() { return openingHours.get(); }
    public void setOpeningHours(String openingHours) { this.openingHours.set(openingHours); }
    public StringProperty openingHoursProperty() { return openingHours; }

    public double getPricePerEntry() { return pricePerEntry.get(); }
    public void setPricePerEntry(double pricePerEntry) { this.pricePerEntry.set(pricePerEntry); }
    public DoubleProperty pricePerEntryProperty() { return pricePerEntry; }

    public String getImageUrl() { return imageUrl.get(); }
    public void setImageUrl(String imageUrl) { this.imageUrl.set(imageUrl); }
    public StringProperty imageUrlProperty() { return imageUrl; }

    public LocalDateTime getCreatedAt() { return createdAt.get(); }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt.set(createdAt); }
    public ObjectProperty<LocalDateTime> createdAtProperty() { return createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt.get(); }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt.set(updatedAt); }
    public ObjectProperty<LocalDateTime> updatedAtProperty() { return updatedAt; }
}
