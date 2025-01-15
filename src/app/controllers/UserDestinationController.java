package app.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class UserDestinationController implements Initializable {

    @FXML private ImageView heroImage;
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private HBox featuredDestinationsContainer;
    @FXML private GridPane destinationGrid;
    @FXML private HBox recommendedExperiencesContainer;

    private List<String> destinationImages = Arrays.asList(
            "kawah_ijen.jpg",
            "kelingking_beach.jpg",
            "madakaripura-waterfall.jpg",
            "mountain_bromo.jpg",
            "ulun-danu.jpg"
    );

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadHeroImage();
        loadFeaturedDestinations();
        loadDestinationGrid();
        loadRecommendedExperiences();
        setupSearchfunctionality();
    }

    private void loadHeroImage() {
        try {
            ImageView image = new ImageView(new Image(getClass().getResource("/resources/images/kelingking_beach.jpg").toExternalForm()));
            heroImage.setImage(image.getImage());
            heroImage.setFitWidth(1200);
            heroImage.setFitHeight(500);
            heroImage.setPreserveRatio(false);
        } catch (Exception e) {
            System.err.println("Error loading hero image: " + e.getMessage());
        }
    }

    private void loadFeaturedDestinations() {
        destinationImages.forEach(imageName -> {
            try {
                VBox destinationCard = createDestinationCard(imageName);
                featuredDestinationsContainer.getChildren().add(destinationCard);
            } catch (Exception e) {
                System.err.println("Error loading featured destination: " + imageName);
            }
        });
    }

    private void loadDestinationGrid() {
        int row = 0, col = 0;
        for (String imageName : destinationImages) {
            try {
                VBox destinationCard = createDestinationCard(imageName);
                destinationGrid.add(destinationCard, col, row);

                col++;
                if (col > 2) {
                    col = 0;
                    row++;
                }
            } catch (Exception e) {
                System.err.println("Error loading destination image: " + imageName);
            }
        }
    }

    private void loadRecommendedExperiences() {
        for (String imageName : destinationImages) {
            try {
                VBox experienceCard = createDestinationCard(imageName);
                recommendedExperiencesContainer.getChildren().add(experienceCard);
            } catch (Exception e) {
                System.err.println("Error loading recommended experience: " + imageName);
            }
        }
    }

    private VBox createDestinationCard(String imageName) {
        // Buat ImageView untuk setiap gambar
        ImageView imageView = new ImageView();

        try {
            // Gunakan URL untuk memuat gambar spesifik
            URL imageUrl = getClass().getResource("/resources/images/" + imageName);

            if (imageUrl != null) {
                ImageView tempImageView = new ImageView(new Image(imageUrl.toExternalForm()));
                imageView.setImage(tempImageView.getImage());
            } else {
                System.err.println("Image not found: " + imageName);
                // Opsional: Set gambar placeholder jika gambar tidak ditemukan
                // imageView.setImage(new Image("/resources/images/placeholder.jpg"));
            }
        } catch (Exception e) {
            System.err.println("Error loading image " + imageName + ": " + e.getMessage());
        }

        // Atur properti ImageView
        imageView.setFitWidth(250);
        imageView.setFitHeight(200);
        imageView.setPreserveRatio(true);
        imageView.setStyle("-fx-background-radius: 15px;");

        // Buat judul dari nama file
        Text title = new Text(formatImageName(imageName));
        title.getStyleClass().add("destination-title");

        // Buat kartu destinasi
        VBox card = new VBox(imageView, title);
        card.getStyleClass().add("destination-card");
        card.setSpacing(5);
        return card;
    }

    private String formatImageName(String imageName) {
        // Hapus ekstensi .jpg dan ganti underscore dengan spasi
        return imageName.replace("_", " ")
                .replace(".jpg", "")
                .substring(0, 1).toUpperCase() +
                imageName.replace("_", " ")
                        .replace(".jpg", "")
                        .substring(1)
                        .toLowerCase();
    }

    private void setupSearchfunctionality() {
        searchButton.setOnAction(event -> {
            String query = searchField.getText().toLowerCase();
            // Implementasikan logika pencarian di sini
            System.out.println("Searching for: " + query);
        });
    }
}