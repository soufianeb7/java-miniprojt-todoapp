module com.todoapp {

    // Modules JavaFX nécessaires au fonctionnement de l'application.
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;

    // Module JDBC standard pour l'accès à la base de données.
    requires java.sql;

    // Ouvre les packages à JavaFX afin que les fichiers FXML puissent accéder
    // aux contrôleurs et aux modèles via la réflexion.
    opens com.todoapp to javafx.fxml, javafx.graphics;
    opens com.todoapp.controllers to javafx.fxml;
    opens com.todoapp.models to javafx.fxml, javafx.base;

    // Rend le package principal accessible aux autres modules.
    exports com.todoapp;
}

