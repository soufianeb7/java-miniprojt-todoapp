module com.todoapp {

    // JavaFX modules needed by the app
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;
    // MySQL JDBC driver
    requires java.sql;

    // Open packages to JavaFX so FXML can access controllers and models
    opens com.todoapp             to javafx.fxml, javafx.graphics;
    opens com.todoapp.controllers to javafx.fxml;
    opens com.todoapp.models      to javafx.fxml , javafx.base;

    // Export main package
    exports com.todoapp;
}

