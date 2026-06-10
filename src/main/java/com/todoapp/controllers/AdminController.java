package com.todoapp.controllers;

import com.todoapp.dao.TaskDAO;
import com.todoapp.dao.UserDAO;
import com.todoapp.models.Session;
import com.todoapp.models.User;
import com.todoapp.models.User.Role;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Contrôleur du panneau d'administration.
 * Accessible uniquement aux utilisateurs ayant le rôle ADMIN.
 * Permet de gérer les comptes utilisateurs : créer, supprimer, promouvoir.
 */
public class AdminController implements Initializable {

    // ─── Dashboard ────────────────────────────────────────────────────────────
    @FXML private Label lblTotalUsers;
    @FXML private Label lblTotalAdmins;
    @FXML private Label lblTotalRegular;

    // ─── Table ────────────────────────────────────────────────────────────────
    @FXML private TableView<User>            userTable;
    @FXML private TableColumn<User, Integer> colUserId;
    @FXML private TableColumn<User, String>  colUsername;
    @FXML private TableColumn<User, String>  colFullName;
    @FXML private TableColumn<User, String>  colRole;
    @FXML private TableColumn<User, String>  colCreatedAt;
    @FXML private TableColumn<User, Integer> colTaskCount;

    // ─── Formulaire ───────────────────────────────────────────────────────────
    @FXML private TextField     txtNewFullName;
    @FXML private TextField     txtNewUsername;
    @FXML private PasswordField txtNewPassword;
    @FXML private ComboBox<String> cmbNewRole;
    @FXML private Label         lblAdminError;

    private final UserDAO userDAO = new UserDAO();
    private final TaskDAO taskDAO = new TaskDAO();
    private final ObservableList<User> userList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTableColumns();
        cmbNewRole.setItems(FXCollections.observableArrayList("USER", "ADMIN"));
        cmbNewRole.setValue("USER");
        loadAllUsers();
    }

    // ─── Setup Table ──────────────────────────────────────────────────────────

    private void setupTableColumns() {
        colUserId.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().getId()).asObject());
        colUsername.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getUsername()));
        colFullName.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getFullName()));
        colCreatedAt.setCellValueFactory(data -> {
            if (data.getValue().getCreatedAt() != null) {
                return new SimpleStringProperty(
                        data.getValue().getCreatedAt()
                                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            }
            return new SimpleStringProperty("");
        });

        // Rôle avec couleur
        colRole.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getRole().name()));
        colRole.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String role, boolean empty) {
                super.updateItem(role, empty);
                if (empty || role == null) {
                    setText(null); setStyle("");
                } else {
                    setText(role);
                    if (role.equals("ADMIN")) {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #2980b9; -fx-font-weight: bold;");
                    }
                }
            }
        });

        // Nombre de tâches par utilisateur
        colTaskCount.setCellValueFactory(data ->
                new SimpleIntegerProperty(
                        taskDAO.getTotalCount(data.getValue().getId())).asObject());

        // Marquer la ligne de l'admin connecté
        userTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (!empty && user != null &&
                        user.getId() == Session.getCurrentUser().getId()) {
                    setStyle("-fx-background-color: #fef9e7;");
                } else {
                    setStyle("");
                }
            }
        });

        userTable.setItems(userList);
    }

    // ─── Chargement ───────────────────────────────────────────────────────────

    private void loadAllUsers() {
        userList.clear();
        userList.addAll(userDAO.getAllUsers());
        updateStats();
    }

    private void updateStats() {
        long admins  = userList.stream().filter(User::isAdmin).count();
        long regular = userList.size() - admins;
        lblTotalUsers.setText(String.valueOf(userList.size()));
        lblTotalAdmins.setText(String.valueOf(admins));
        lblTotalRegular.setText(String.valueOf(regular));
    }

    // ─── Actions ──────────────────────────────────────────────────────────────

    /**
     * Crée un nouvel utilisateur depuis le formulaire admin.
     */
    @FXML
    private void handleCreateUser() {
        String fullName = txtNewFullName.getText().trim();
        String username = txtNewUsername.getText().trim();
        String password = txtNewPassword.getText().trim();
        String roleStr  = cmbNewRole.getValue();

        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty()) {
            lblAdminError.setText("Tous les champs sont obligatoires.");
            return;
        }
        if (username.length() < 3) {
            lblAdminError.setText("Username : minimum 3 caractères.");
            return;
        }
        if (password.length() < 4) {
            lblAdminError.setText("Mot de passe : minimum 4 caractères.");
            return;
        }
        if (userDAO.usernameExists(username)) {
            lblAdminError.setText("Ce username est déjà utilisé.");
            return;
        }

        User newUser = new User(username, password, fullName);
        newUser.setRole(Role.valueOf(roleStr));

        boolean ok = userDAO.register(newUser);
        if (ok) {
            lblAdminError.setStyle("-fx-text-fill: #27ae60;");
            lblAdminError.setText("Utilisateur créé avec succès !");
            handleClearAdminForm();
            loadAllUsers();
        } else {
            lblAdminError.setStyle("-fx-text-fill: #e74c3c;");
            lblAdminError.setText("Échec de la création.");
        }
    }

    /**
     * Supprime l'utilisateur sélectionné après confirmation.
     * Un admin ne peut pas se supprimer lui-même.
     */
    @FXML
    private void handleDeleteUser() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Sélectionnez un utilisateur à supprimer.");
            return;
        }
        if (selected.getId() == Session.getCurrentUser().getId()) {
            showWarning("Vous ne pouvez pas supprimer votre propre compte.");
            return;
        }

        Optional<ButtonType> result = showConfirmation(
                "Supprimer l'utilisateur",
                "Supprimer « " + selected.getUsername() + " » ?\n" +
                        "Toutes ses tâches seront également supprimées."
        );

        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean ok = userDAO.deleteUser(
                    selected.getId(),
                    Session.getCurrentUser().getId()
            );
            if (ok) {
                showInfo("Utilisateur supprimé.");
                loadAllUsers();
            } else {
                showError("Échec de la suppression.");
            }
        }
    }

    /**
     * Promeut l'utilisateur sélectionné au rôle ADMIN.
     */
    @FXML
    private void handlePromoteUser() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Sélectionnez un utilisateur.");
            return;
        }
        if (selected.getRole() == Role.ADMIN) {
            showWarning("Cet utilisateur est déjà admin.");
            return;
        }

        boolean ok = userDAO.updateUserRole(selected.getId(), Role.ADMIN);
        if (ok) {
            showInfo(selected.getUsername() + " est maintenant ADMIN.");
            loadAllUsers();
        } else {
            showError("Échec de la promotion.");
        }
    }

    /**
     * Rétrograde l'utilisateur sélectionné au rôle USER.
     */
    @FXML
    private void handleDemoteUser() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Sélectionnez un utilisateur.");
            return;
        }
        if (selected.getId() == Session.getCurrentUser().getId()) {
            showWarning("Vous ne pouvez pas vous rétrograder vous-même.");
            return;
        }
        if (selected.getRole() == Role.USER) {
            showWarning("Cet utilisateur est déjà USER.");
            return;
        }

        boolean ok = userDAO.updateUserRole(selected.getId(), Role.USER);
        if (ok) {
            showInfo(selected.getUsername() + " est maintenant USER.");
            loadAllUsers();
        } else {
            showError("Échec de la rétrogradation.");
        }
    }

    /** Efface le formulaire de création. */
    @FXML
    private void handleClearAdminForm() {
        txtNewFullName.clear();
        txtNewUsername.clear();
        txtNewPassword.clear();
        cmbNewRole.setValue("USER");
        lblAdminError.setText("");
        lblAdminError.setStyle("-fx-text-fill: #e74c3c;");
    }

    /** Retourne à l'écran principal de l'application. */
    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/todoapp/views/main-view.fxml"));
            Stage stage = (Stage) userTable.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("To-Do List Manager — " +
                    Session.getCurrentUser().getFullName());
        } catch (IOException e) {
            showError("Erreur de navigation : " + e.getMessage());
        }
    }

    // ─── Alertes ──────────────────────────────────────────────────────────────

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Succès"); a.setHeaderText(null);
        a.setContentText(msg); a.showAndWait();
    }
    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Erreur"); a.setHeaderText(null);
        a.setContentText(msg); a.showAndWait();
    }
    private void showWarning(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("Attention"); a.setHeaderText(null);
        a.setContentText(msg); a.showAndWait();
    }
    private Optional<ButtonType> showConfirmation(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle(title); a.setHeaderText(null);
        a.setContentText(msg); return a.showAndWait();
    }
}
