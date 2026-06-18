package com.todoapp.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Représente une tâche (élément de la liste) de l'application.
 * Chaque instance correspond à une ligne de la table {@code tasks} en base
 * et contient les propriétés courantes : titre, description, priorité, statut,
 * date d'échéance, catégorie, timestamps et identifiant de l'utilisateur
 * propriétaire.
 */
public class Task {

    // ─── Enums ───────────────────────────────────────────────────────────────

    /**
     * Niveau de priorité d'une tâche.
     * - LOW : priorité faible
     * - MEDIUM : priorité moyenne
     * - HIGH : priorité élevée
     */
    public enum Priority {
        LOW, MEDIUM, HIGH
    }

    /**
     * État d'avancement d'une tâche.
     * - PENDING : en attente
     * - IN_PROGRESS : en cours
     * - COMPLETED : terminée
     */
    public enum Status {
        PENDING, IN_PROGRESS, COMPLETED
    }

    // ─── Champs ─────────────────────────────────────────────────────────────

    /** Identifiant unique en base (AUTO_INCREMENT). */
    private int id;

    /** Titre court décrivant la tâche (champ obligatoire). */
    private String title;

    /** Description optionnelle fournissant plus de détails. */
    private String description;

    /** Priorité de la tâche (LOW, MEDIUM, HIGH). */
    private Priority priority;

    /** Statut courant (PENDING, IN_PROGRESS, COMPLETED). */
    private Status status;

    /** Date limite (peut être {@code null}). */
    private LocalDate dueDate;

    /** Catégorie textuelle (ex : Study, Work, Personal, General). */
    private String category;

    /** Timestamp de création (initialisé à la création). */
    private LocalDateTime createdAt;

    /** Timestamp de la dernière modification. */
    private LocalDateTime updatedAt;

    /** Identifiant de l'utilisateur propriétaire de la tâche. */
    private int userId;

    // ─── Constructeurs ───────────────────────────────────────────────────────

    /**
     * Constructeur par défaut utilisé par JavaFX/DAO.
     * Initialise la priorité à MEDIUM, le statut à PENDING et les timestamps.
     */
    public Task() {
        this.priority  = Priority.MEDIUM;
        this.status    = Status.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Constructeur pour créer une nouvelle tâche avant insertion en base.
     * Les champs {@code id}, {@code createdAt} et {@code updatedAt} sont gérés
     * par la base de données ou initialisés ici.
     *
     * @param title       titre de la tâche
     * @param description description optionnelle
     * @param priority    niveau de priorité
     * @param status      statut initial
     * @param dueDate     date d'échéance (ou {@code null})
     * @param category    catégorie de la tâche
     * @param userId      identifiant de l'utilisateur propriétaire
     */
    public Task(String title, String description, Priority priority,
                Status status, LocalDate dueDate, String category, int userId) {
        this.title       = title;
        this.description = description;
        this.priority    = priority;
        this.status      = status;
        this.dueDate     = dueDate;
        this.category    = category;
        this.userId      = userId;
        this.createdAt   = LocalDateTime.now();
        this.updatedAt   = LocalDateTime.now();
    }

    /**
     * Constructeur complet utilisé lors du chargement depuis la base.
     *
     * @param id          identifiant en base
     * @param title       titre
     * @param description description
     * @param priority    priorité
     * @param status      statut
     * @param dueDate     date d'échéance
     * @param category    catégorie
     * @param createdAt   timestamp de création
     * @param updatedAt   timestamp de mise à jour
     */
    public Task(int id, String title, String description, Priority priority,
                Status status, LocalDate dueDate, String category,
                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id          = id;
        this.title       = title;
        this.description = description;
        this.priority    = priority;
        this.status      = status;
        this.dueDate     = dueDate;
        this.category    = category;
        this.createdAt   = createdAt;
        this.updatedAt   = updatedAt;
    }

    // ─── Getters ───────────────────────────────────────────────────────────

    /**
     * @return l'identifiant unique en base
     */
    public int getId() { return id; }

    /**
     * @return le titre de la tâche
     */
    public String getTitle() { return title; }

    /**
     * @return la description (peut être {@code null})
     */
    public String getDescription() { return description; }

    /**
     * @return la priorité courante
     */
    public Priority getPriority() { return priority; }

    /**
     * @return le statut courant
     */
    public Status getStatus() { return status; }

    /**
     * @return la date d'échéance, ou {@code null} si non définie
     */
    public LocalDate getDueDate() { return dueDate; }

    /**
     * @return la catégorie de la tâche
     */
    public String getCategory() { return category; }

    /**
     * @return la date/heure de création
     */
    public LocalDateTime getCreatedAt() { return createdAt; }

    /**
     * @return la date/heure de la dernière mise à jour
     */
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    /**
     * @return l'identifiant de l'utilisateur propriétaire
     */
    public int getUserId() { return userId; }

    // ─── Setters ───────────────────────────────────────────────────────────

    /**
     * Définit l'identifiant en base.
     *
     * @param id identifiant
     */
    public void setId(int id) { this.id = id; }

    /**
     * Définit le titre de la tâche.
     *
     * @param title nouveau titre
     */
    public void setTitle(String title) { this.title = title; }

    /**
     * Définit la description.
     *
     * @param description texte descriptif
     */
    public void setDescription(String description) { this.description = description; }

    /**
     * Définit la priorité.
     *
     * @param priority niveau de priorité
     */
    public void setPriority(Priority priority) { this.priority = priority; }

    /**
     * Définit le statut.
     *
     * @param status nouveau statut
     */
    public void setStatus(Status status) { this.status = status; }

    /**
     * Définit la date d'échéance.
     *
     * @param dueDate date limite (ou {@code null})
     */
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    /**
     * Définit la catégorie.
     *
     * @param category libellé de la catégorie
     */
    public void setCategory(String category) { this.category = category; }

    /**
     * Définit le timestamp de création.
     *
     * @param createdAt date/heure de création
     */
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    /**
     * Définit le timestamp de mise à jour.
     *
     * @param updatedAt date/heure de la dernière modification
     */
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    /**
     * Définit l'identifiant de l'utilisateur propriétaire.
     *
     * @param userId identifiant de l'utilisateur
     */
    public void setUserId(int userId) { this.userId = userId; }

    // ─── Méthodes utilitaires ───────────────────────────────────────────────

    /**
     * Indique si la tâche est en retard par rapport à sa date d'échéance
     * et n'est pas encore marquée comme complétée.
     *
     * @return {@code true} si la tâche est en retard, sinon {@code false}
     */
    public boolean isOverdue() {
        return dueDate != null
                && dueDate.isBefore(LocalDate.now())
                && status != Status.COMPLETED;
    }

    /**
     * Indique si la tâche a été marquée comme complétée.
     *
     * @return {@code true} si {@link Status#COMPLETED}, sinon {@code false}
     */
    public boolean isCompleted() {
        return status == Status.COMPLETED;
    }

    /**
     * Représentation texte synthétique de la tâche, utile pour le débogage.
     *
     * @return chaîne formatée contenant id, titre, priorité et statut
     */
    @Override
    public String toString() {
        return String.format("Task{id=%d, title='%s', priority=%s, status=%s}",
                id, title, priority, status);
    }
}