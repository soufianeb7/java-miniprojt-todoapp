package com.todoapp.controllers;

import com.todoapp.dao.TaskDAO;
import com.todoapp.models.Task;
import com.todoapp.models.Task.Priority;
import com.todoapp.models.Task.Status;
import com.todoapp.models.Session;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Contrôleur principal de l'application de liste de tâches.
 *
 * Gère les interactions utilisateur : ajout, modification, suppression,
 * recherche, filtrage des tâches et mise à jour des indicateurs du tableau de bord.
 */
public class TaskController implements Initializable {

    @FXML private Label lblTotal;
    @FXML private Label lblCompleted;
    @FXML private Label lblPending;
    @FXML private Label lblInProgress;
    @FXML private Label lblWelcome;

    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cmbFilterStatus;
    @FXML private ComboBox<String> cmbFilterPriority;

    @FXML private TableView<Task> taskTable;
    @FXML private TableColumn<Task, Integer>   colId;
    @FXML private TableColumn<Task, String>    colTitle;
    @FXML private TableColumn<Task, Priority>  colPriority;
    @FXML private TableColumn<Task, Status>    colStatus;
    @FXML private TableColumn<Task, LocalDate> colDueDate;
    @FXML private TableColumn<Task, String>    colCategory;
    @FXML private TableColumn<Task, String>    colCreated;

    @FXML private Label      lblFormTitle;
    @FXML private TextField  txtTitle;
    @FXML private TextArea   txtDescription;
    @FXML private ComboBox<String> cmbPriority;
    @FXML private ComboBox<String> cmbStatus;
    @FXML private DatePicker dateDueDate;
    @FXML private ComboBox<String> cmbCategory;
    @FXML private Button     btnSave;
    @FXML private Button btnAdminPanel;
    private final TaskDAO taskDAO = new TaskDAO();
    private final ObservableList<Task> taskList = FXCollections.observableArrayList();

    /** Tâche actuellement en cours de modification. Valeur nulle lors de l'ajout. */
    private Task selectedTask = null;

    /**
     * Appelé automatiquement par JavaFX après le chargement du FXML.
     * Configure les colonnes du tableau, remplit les listes déroulantes et charge les tâches.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTableColumns();
        setupComboBoxes();
        setupTableSelection();
        loadAllTasks();
        if (Session.isLoggedIn()) {
            lblWelcome.setText("👤  " + Session.getCurrentUser().getFullName());
        }
        // Afficher le bouton Admin uniquement si l'utilisateur est admin
        btnAdminPanel.setVisible(Session.getCurrentUser().isAdmin());
    }

    /**
     * Associe chaque colonne du tableau au champ correspondant du modèle `Task`.
     */
    private void setupTableColumns() {
        colId.setCellValueFactory(data ->
                new javafx.beans.property.SimpleIntegerProperty(data.getValue().getId()).asObject());
        colTitle.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getTitle()));
        colCategory.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getCategory()));
        colDueDate.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getDueDate()));

        colCreated.setCellValueFactory(data -> {
            if (data.getValue().getCreatedAt() != null) {
                String formatted = data.getValue().getCreatedAt()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                return new javafx.beans.property.SimpleStringProperty(formatted);
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });

        colPriority.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Priority priority, boolean empty) {
                super.updateItem(priority, empty);
                if (empty || priority == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(priority.name());
                    switch (priority) {
                        case HIGH   -> setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                        case MEDIUM -> setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;");
                        case LOW    -> setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    }
                }
            }
        });

        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Status status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status.name().replace("_", " "));
                    switch (status) {
                        case COMPLETED  -> setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                        case IN_PROGRESS -> setStyle("-fx-text-fill: #2980b9; -fx-font-weight: bold;");
                        case PENDING    -> setStyle("-fx-text-fill: #7f8c8d;");
                    }
                }
            }
        });

        taskTable.setItems(taskList);
    }

    /**
     * Remplit toutes les listes déroulantes avec leurs valeurs disponibles.
     */
    private void setupComboBoxes() {
        cmbPriority.setItems(FXCollections.observableArrayList("LOW", "MEDIUM", "HIGH"));
        cmbPriority.setValue("MEDIUM");

        cmbStatus.setItems(FXCollections.observableArrayList(
                "PENDING", "IN_PROGRESS", "COMPLETED"));
        cmbStatus.setValue("PENDING");

        cmbCategory.setItems(FXCollections.observableArrayList(
                "General", "Work", "Study", "Personal", "Health", "Finance"));
        cmbCategory.setValue("General");

        cmbFilterStatus.setItems(FXCollections.observableArrayList(
                "ALL", "PENDING", "IN_PROGRESS", "COMPLETED"));
        cmbFilterStatus.setValue("ALL");

        cmbFilterPriority.setItems(FXCollections.observableArrayList(
                "ALL", "LOW", "MEDIUM", "HIGH"));
        cmbFilterPriority.setValue("ALL");
    }

    /**
     * Écoute la sélection des lignes du tableau et remplit le formulaire.
     */
    private void setupTableSelection() {
        taskTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        populateForm(newVal);
                    }
                }
        );
    }

    /**
     * Charge toutes les tâches depuis la base de données dans la TableView et
     * actualise les indicateurs du tableau de bord.
     */
    private void loadAllTasks() {
        int uid = Session.getCurrentUser().getId();
        taskList.clear();
        taskList.addAll(taskDAO.getAllTasks(uid));
        updateDashboard();
    }

    /**
     * Met à jour les quatre indicateurs du tableau de bord.
     */
    private void updateDashboard() {
        int uid = Session.getCurrentUser().getId();
        lblTotal.setText(String.valueOf(taskDAO.getTotalCount(uid)));
        lblCompleted.setText(String.valueOf(taskDAO.getCountByStatus(Status.COMPLETED, uid)));
        lblPending.setText(String.valueOf(taskDAO.getCountByStatus(Status.PENDING, uid)));
        lblInProgress.setText(String.valueOf(taskDAO.getCountByStatus(Status.IN_PROGRESS, uid)));
    }

    /**
     * Remplit le formulaire avec les valeurs de la tâche sélectionnée.
     * Le formulaire passe également en mode modification.
     *
     * @param task tâche sélectionnée dans le tableau
     */
    private void populateForm(Task task) {
        selectedTask = task;
        lblFormTitle.setText("Edit Task");
        btnSave.setText("💾  Update Task");

        txtTitle.setText(task.getTitle());
        txtDescription.setText(task.getDescription());
        cmbPriority.setValue(task.getPriority().name());
        cmbStatus.setValue(task.getStatus().name());
        dateDueDate.setValue(task.getDueDate());
        cmbCategory.setValue(task.getCategory());
    }

    /**
     * Efface tous les champs du formulaire et revient au mode ajout.
     */
    @FXML
    private void handleClearForm() {
        selectedTask = null;
        lblFormTitle.setText("Add New Task");
        btnSave.setText("💾  Save Task");

        txtTitle.clear();
        txtDescription.clear();
        cmbPriority.setValue("MEDIUM");
        cmbStatus.setValue("PENDING");
        dateDueDate.setValue(null);
        cmbCategory.setValue("General");

        taskTable.getSelectionModel().clearSelection();
    }

    /**
     * Gère le clic sur le bouton Enregistrer / Mettre à jour.
     * Valide le formulaire, puis crée ou modifie une tâche.
     */
    @FXML
    private void handleSaveTask() {
        if (!validateForm()) return;

        String title       = txtTitle.getText().trim();
        String description = txtDescription.getText().trim();
        Priority priority  = Priority.valueOf(cmbPriority.getValue());
        Status status      = Status.valueOf(cmbStatus.getValue());
        LocalDate dueDate  = dateDueDate.getValue();
        String category    = cmbCategory.getValue();

        if (selectedTask == null) {
            Task newTask = new Task(title, description, priority, status, dueDate, category,
                    Session.getCurrentUser().getId());
            boolean ok = taskDAO.createTask(newTask);
            if (ok) {
                showInfo("Task added successfully!");
                handleClearForm();
                loadAllTasks();
            } else {
                showError("Failed to add task. Please try again.");
            }

        } else {
            selectedTask.setTitle(title);
            selectedTask.setDescription(description);
            selectedTask.setPriority(priority);
            selectedTask.setStatus(status);
            selectedTask.setDueDate(dueDate);
            selectedTask.setCategory(category);

            boolean ok = taskDAO.updateTask(selectedTask);
            if (ok) {
                showInfo("Task updated successfully!");
                handleClearForm();
                loadAllTasks();
            } else {
                showError("Failed to update task. Please try again.");
            }
        }
    }

    /**
     * Gère le bouton Modifier : sélectionne la ligne active et l'ouvre dans le formulaire.
     */
    @FXML
    private void handleEditTask() {
        Task task = taskTable.getSelectionModel().getSelectedItem();
        if (task == null) {
            showWarning("Please select a task to edit.");
            return;
        }
        populateForm(task);
    }

    /**
     * Gère le bouton Supprimer : demande une confirmation puis supprime la tâche.
     */
    @FXML
    private void handleDeleteTask() {
        Task task = taskTable.getSelectionModel().getSelectedItem();
        if (task == null) {
            showWarning("Please select a task to delete.");
            return;
        }

        Optional<ButtonType> result = showConfirmation(
                "Delete Task",
                "Are you sure you want to delete:\n\"" + task.getTitle() + "\"?"
        );

        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean ok = taskDAO.deleteTask(task.getId(), Session.getCurrentUser().getId());
            if (ok) {
                showInfo("Task deleted.");
                handleClearForm();
                loadAllTasks();
            } else {
                showError("Failed to delete task.");
            }
        }
    }

    /**
     * Marque la tâche sélectionnée comme terminée dans la base de données.
     */
    @FXML
    private void handleMarkCompleted() {
        Task task = taskTable.getSelectionModel().getSelectedItem();
        if (task == null) {
            showWarning("Please select a task to mark as completed.");
            return;
        }
        if (task.getStatus() == Status.COMPLETED) {
            showWarning("This task is already completed.");
            return;
        }

        boolean ok = taskDAO.markTaskAsCompleted(task.getId(), Session.getCurrentUser().getId());
        if (ok) {
            showInfo("Task marked as completed!");
            handleClearForm();
            loadAllTasks();
        } else {
            showError("Failed to update task status.");
        }
    }

    /**
     * Déclenché à chaque saisie dans le champ de recherche.
     * Recherche les tâches par titre en temps réel.
     */
    @FXML
    private void handleSearch() {
        String keyword = txtSearch.getText().trim();
        int uid = Session.getCurrentUser().getId();
        taskList.clear();
        if (keyword.isEmpty()) {
            taskList.addAll(taskDAO.getAllTasks(uid));
        } else {
            taskList.addAll(taskDAO.searchTasks(keyword, uid));
        }
    }

    /**
     * Filtre la liste des tâches selon le statut et/ou la priorité sélectionnés.
     */
    @FXML
    private void handleFilter() {
        String statusVal   = cmbFilterStatus.getValue();
        String priorityVal = cmbFilterPriority.getValue();
        int uid = Session.getCurrentUser().getId();

        Status   status   = (statusVal   == null || statusVal.equals("ALL"))
                ? null : Status.valueOf(statusVal);
        Priority priority = (priorityVal == null || priorityVal.equals("ALL"))
                ? null : Priority.valueOf(priorityVal);

        taskList.clear();
        taskList.addAll(taskDAO.filterTasks(status, priority, uid));
    }

    /**
     * Réinitialise la recherche et les filtres, puis recharge toutes les tâches.
     */
    @FXML
    private void handleClearFilters() {
        txtSearch.clear();
        cmbFilterStatus.setValue("ALL");
        cmbFilterPriority.setValue("ALL");
        loadAllTasks();
    }

    /**
     * Valide le formulaire avant l'enregistrement.
     *
     * @return true si tous les champs obligatoires sont correctement renseignés
     */
    private boolean validateForm() {
        if (txtTitle.getText().trim().isEmpty()) {
            showError("Task title is required.");
            txtTitle.requestFocus();
            return false;
        }
        if (txtTitle.getText().trim().length() > 255) {
            showError("Title must be 255 characters or less.");
            return false;
        }
        if (cmbPriority.getValue() == null) {
            showError("Please select a priority.");
            return false;
        }
        if (cmbStatus.getValue() == null) {
            showError("Please select a status.");
            return false;
        }
        return true;
    }

    /** Affiche une alerte d'information. */
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /** Affiche une alerte d'erreur. */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /** Affiche une alerte d'avertissement. */
    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Affiche une boîte de dialogue de confirmation et retourne le choix de l'utilisateur.
     *
     * @param title titre de la fenêtre de dialogue
     * @param message question posée à l'utilisateur
     * @return Optional contenant le bouton cliqué par l'utilisateur
     */
    private Optional<ButtonType> showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait();
    }

    /**
     * Déconnecte l'utilisateur et retourne à l'écran de connexion.
     */
    @FXML
    private void handleLogout() {
        Optional<ButtonType> result = showConfirmation(
                "Deconnexion",
                "Voulez-vous vraiment vous deconnecter ?"
        );

        if (result.isPresent() && result.get() == ButtonType.OK) {
            Session.clear();
            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                        getClass().getResource("/com/todoapp/views/login-view.fxml")
                );
                javafx.stage.Stage stage =
                        (javafx.stage.Stage) taskTable.getScene().getWindow();
                stage.setScene(new javafx.scene.Scene(loader.load()));
                stage.setTitle("To-Do List Manager — Connexion");
                stage.setResizable(false);
            } catch (Exception e) {
                showError("Erreur lors de la deconnexion : " + e.getMessage());
            }
        }
    }

    /**
     * Ouvre le panneau d'administration (réservé aux admins).
     */
    @FXML
    private void handleOpenAdmin() {
        if (!Session.getCurrentUser().isAdmin()) return;
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/todoapp/views/admin-view.fxml"));
            Stage stage = (Stage) taskTable.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Admin Panel — " +
                    Session.getCurrentUser().getFullName());
        } catch (Exception e) {
            showError("Impossible d'ouvrir le panneau admin.");
        }
    }
}