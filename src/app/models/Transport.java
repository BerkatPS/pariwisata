package app.models;

import javafx.beans.property.*;

import java.sql.Timestamp;

public class Transport {
    private SimpleIntegerProperty transportId;
    private SimpleStringProperty transportType;
    private SimpleIntegerProperty destinationId;
    private SimpleDoubleProperty price;
    private SimpleStringProperty departureTime;
    private SimpleStringProperty arrivalTime;

    public Transport(int transportId, String transportType, int destinationId, double price, Timestamp departureTime, Timestamp arrivalTime) {
        this.transportId = new SimpleIntegerProperty(transportId);
        this.transportType = new SimpleStringProperty(transportType);
        this.destinationId = new SimpleIntegerProperty(destinationId);
        this.price = new SimpleDoubleProperty(price);
        this.departureTime = new SimpleStringProperty(departureTime.toString());
        this.arrivalTime = new SimpleStringProperty(arrivalTime.toString());
    }

    // Getters and Setters
    public int getTransportId() {
        return transportId.get();
    }

    public void setTransportId(int transportId) {
        this.transportId.set(transportId);
    }

    public String getTransportType() {
        return transportType.get();
    }

    public void setTransportType(String transportType) {
        this.transportType.set(transportType);
    }
    // Set Destination object

    public double getPrice() {
        return price.get();
    }

    public void setPrice(double price) {
        this.price.set(price);
    }

    public int getDestinationId() {
        return destinationId.get();
    }

    public void setDestinationId(int destinationId) {
        this.destinationId.set(destinationId);
    }

    public String getDepartureTime() {
        return departureTime.get();
    }

    public void setDepartureTime(Timestamp departureTime) {
        this.departureTime.set(departureTime.toString());
    }

    public String getArrivalTime() {
        return arrivalTime.get();
    }

    public void setArrivalTime(Timestamp arrivalTime) {
        this.arrivalTime.set(arrivalTime.toString());
    }

    // Property methods for TableView binding
    public SimpleIntegerProperty transportIdProperty() {
        return transportId;
    }

    public SimpleStringProperty transportTypeProperty() {
        return transportType;
    }

    public SimpleDoubleProperty priceProperty() {
        return price;
    }

    public SimpleStringProperty departureTimeProperty() {
        return departureTime;
    }

    public SimpleStringProperty arrivalTimeProperty() {
        return arrivalTime;
    }

    public SimpleIntegerProperty destinationIdProperty() {
        return destinationId;
    }
}
