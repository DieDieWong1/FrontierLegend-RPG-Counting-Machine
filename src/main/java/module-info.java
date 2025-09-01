module MaterialCount {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;
    exports MaterialCount;
    opens MaterialCount to javafx.fxml, com.google.gson;
}