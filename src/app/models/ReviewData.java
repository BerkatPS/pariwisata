package app.models;

import java.time.LocalDateTime;

public record ReviewData(
        String username,
        String destinationName,
        int rating,
        String reviewText,
        LocalDateTime createdAt
) {}