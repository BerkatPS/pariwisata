package app.models;

import javafx.beans.property.*;

import java.time.LocalDateTime;

public class Review {
    private IntegerProperty reviewId;
    private IntegerProperty userId;
    private IntegerProperty destinationId;
    private IntegerProperty rating;
    private StringProperty reviewText;
    private ObjectProperty<LocalDateTime> createdAt;

    // Constructor
    public Review(int reviewId, int userId, int destinationId, int rating, String reviewText, LocalDateTime createdAt) {
        this.reviewId = new SimpleIntegerProperty(reviewId);
        this.userId = new SimpleIntegerProperty(userId);
        this.destinationId = new SimpleIntegerProperty(destinationId);
        this.rating = new SimpleIntegerProperty(rating);
        this.reviewText = new SimpleStringProperty(reviewText);
        this.createdAt = new SimpleObjectProperty<>(createdAt);
    }

    // Getters and Setters using Property methods

    public int getReviewId() { return reviewId.get(); }
    public void setReviewId(int reviewId) { this.reviewId.set(reviewId); }
    public IntegerProperty reviewIdProperty() { return reviewId; }

    public int getUserId() { return userId.get(); }
    public void setUserId(int userId) { this.userId.set(userId); }
    public IntegerProperty userIdProperty() { return userId; }

    public int getDestinationId() { return destinationId.get(); }
    public void setDestinationId(int destinationId) { this.destinationId.set(destinationId); }
    public IntegerProperty destinationIdProperty() { return destinationId; }

    public int getRating() { return rating.get(); }
    public void setRating(int rating) { this.rating.set(rating); }
    public IntegerProperty ratingProperty() { return rating; }

    public String getReviewText() { return reviewText.get(); }
    public void setReviewText(String reviewText) { this.reviewText.set(reviewText); }
    public StringProperty reviewTextProperty() { return reviewText; }

    public LocalDateTime getCreatedAt() { return createdAt.get(); }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt.set(createdAt); }
    public ObjectProperty<LocalDateTime> createdAtProperty() { return createdAt; }
}
