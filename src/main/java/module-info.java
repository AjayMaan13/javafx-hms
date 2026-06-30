module org.example.javafxhms {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.javafxhms to javafx.fxml;
    exports org.example.javafxhms;
}