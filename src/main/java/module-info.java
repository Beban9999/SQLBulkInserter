module com.example.sqlbulker {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.sqlbulker to javafx.fxml;
    exports com.example.sqlbulker;
}