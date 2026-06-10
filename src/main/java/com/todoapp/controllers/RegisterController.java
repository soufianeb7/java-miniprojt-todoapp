package com.todoapp.controllers;

import com.todoapp.dao.UserDAO;
import com.todoapp.models.User;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Contrôleur de l'écran d'inscription.
 * Il valide les saisies et crée un nouveau compte utilisateur.
 */
public class RegisterController {

    @FXML private TextField     txtFullName;
    @FXML private TextField     txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirm;
    @FXML private Label         lblError;

    private final UserDAO userDAO = new UserDAO();

    /**
     * Gère le bouton Créer un compte.
     * Valide tous les champs et enregistre le nouvel utilisateur.
     */
    @FXML
    private void handleRegister() {
        String fullName = txtFullName.getText().trim();
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();
        String confirm  = txtConfirm.getText().trim();

        if (fullName.isEmpty() || username.isEmpty() ||
                password.isEmpty() || confirm.isEmpty()) {
            lblError.setText("All fields are required.");
            return;
        }
        if (username.length() < 3) {
            lblError.setText("Username must be at least 3 characters.");
            return;
        }
        if (password.length() < 4) {
            lblError.setText("Password must be at least 4 characters.");
            return;
        }
        if (!password.equals(confirm)) {
            lblError.setText("Passwords do not match.");
            txtConfirm.clear();
            return;
        }
        if (userDAO.usernameExists(username)) {
            lblError.setText("Username already taken. Choose another.");
            return;
        }

        User newUser = new User(username, password, fullName);
        boolean ok = userDAO.register(newUser);

        if (ok) {
            goToLogin();
        } else {
            lblError.setText("Registration failed. Please try again.");
        }
    }

    /**
     * Retourne à l'écran de connexion.
     */
    @FXML
    private void handleGoToLogin() {
        goToLogin();
    }

    private void goToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/todoapp/views/login-view.fxml"));
            Stage stage = (Stage) txtUsername.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("To-Do List Manager — Login");
        } catch (IOException e) {
            lblError.setText("Could not open login screen.");
        }
    }
}
