module travelbook {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;

    // Mengekspor package 'app' agar dapat diakses oleh JavaFX
    exports app;

    // Jika Anda menggunakan fxml untuk controller, tambahkan juga package controllers
    opens app.controllers to javafx.fxml;
    opens app.models to javafx.base;
    opens app.utils to javafx.base;
}
