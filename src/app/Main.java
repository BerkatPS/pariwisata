package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/auth/login.fxml"));
        Scene scene = new Scene(loader.load());

//        // Tambahkan CSS dengan path yang benar
          scene.getStylesheets().add(getClass().getResource("/resources/fxml/auth/login.css").toExternalForm());

        // Mengatur judul jendela aplikasi
        primaryStage.setTitle("Travel Booking App");

        // Mengatur scene dan menampilkan jendela
        primaryStage.setScene(scene);
        primaryStage.setResizable(false); // Membuat jendela tidak dapat diubah ukurannya
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
