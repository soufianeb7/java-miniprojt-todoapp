package com.todoapp.controllers;

import com.todoapp.dao.UserDAO;
import com.todoapp.models.Session;
import com.todoapp.models.User;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Contrôleur de l'écran de connexion.
 * Il valide les identifiants et ouvre la vue principale en cas de succès.
 */
public class LoginController {

    @FXML private TextField     txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label         lblError;

    private final UserDAO userDAO = new UserDAO();

    /**
     * Gère le clic sur le bouton de connexion.
     * Vérifie les champs saisis, contrôle les identifiants et affiche la vue principale.
     */
    @FXML
    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            lblError.setText("Please enter your username and password.");
            return;
        }

        User user = userDAO.login(username, password);

        if (user != null) {
            Session.setCurrentUser(user);
            openMainView();
        } else {
            lblError.setText("Invalid username or password. Please try again.");
            txtPassword.clear();
        }
    }

    /**
     * Ouvre l'écran d'inscription.
     */
    @FXML
    private void handleGoToRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/todoapp/views/register-view.fxml"));
            Stage stage = (Stage) txtUsername.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Create Account");
        } catch (IOException e) {
            lblError.setText("Could not open register screen.");
        }
    }

    /**
     * Charge la vue principale de l'application après une connexion réussie.
     */
    private void openMainView() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/todoapp/views/main-view.fxml"));
            Stage stage = (Stage) txtUsername.getScene().getWindow();
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
            stage.setTitle("To-Do List Manager — " +
                    Session.getCurrentUser().getFullName());
            stage.setResizable(true);
            stage.setMinWidth(900);
            stage.setMinHeight(600);
        } catch (IOException e) {
            lblError.setText("Could not open main view: " + e.getMessage());
        }
    }
}