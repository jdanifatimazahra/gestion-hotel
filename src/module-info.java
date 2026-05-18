module gestion_hotel {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires javafx.graphics;
    requires java.desktop;
    requires pdfbox.app;

    opens application to javafx.graphics, javafx.fxml;
    opens controller to javafx.fxml;
    opens model to javafx.base;
    opens util to javafx.fxml;
}
