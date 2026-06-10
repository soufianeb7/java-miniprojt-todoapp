package com.todoapp.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a single task in the To-Do List application.
 * Maps directly to one row in the {@code tasks} table in the database.
 */
public class Task {

    // ─── Enums ────────────────────────────────────────────────────────────────

    /**
     * Priority level of the task.
     */
    public enum Priority {
        LOW, MEDIUM, HIGH
    }

    /**
     * Current status of the task.
     */
    public enum Status {
        PENDING, IN_PROGRESS, COMPLETED
    }

    // ─── Fields ───────────────────────────────────────────────────────────────

    /** Unique identifier — matches the AUTO_INCREMENT id in the database. */
    private int id;

    /** Short title describing the task. Cannot be null or empty. */
    private String title;

    /** Optional longer explanation of what the task involves. */
    private String description;

    /** Importance level: LOW, MEDIUM, or HIGH. */
    private Priority priority;

    /** Current state: PENDING, IN_PROGRESS, or COMPLETED. */
    private Status status;

    /** Optional deadline for the task. */
    private LocalDate dueDate;

    /** Group label such as Study, Work, Personal, General. */
    private String category;

    /** Timestamp set automatically when the task is first created. */
    private LocalDateTime createdAt;

    /** Timestamp updated automatically every time the task is modified. */
    private LocalDateTime updatedAt;

    /** Identifiant de l'utilisateur proprietaire de cette tache. */
    private int userId;

    // ─── Constructors ─────────────────────────────────────────────────────────

    /**
     * Default constructor required for JavaFX and DAO operations.
     */
    public Task() {
        this.priority  = Priority.MEDIUM;
        this.status    = Status.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Constructor for creating a new task before saving to the database.
     * The {@code id}, {@code createdAt}, and {@code updatedAt} fields are
     * handled by the database automatically.
     *
     * @param title       the task title
     * @param description optional details about the task
     * @param priority    importance level of the task
     * @param status      current progress status
     * @param dueDate     optional deadline
     * @param category    category label for grouping tasks
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
     * Full constructor used when loading a task from the database.
     * All fields including {@code id} and timestamps are provided.
     *
     * @param id          the database primary key
     * @param title       the task title
     * @param description optional details
     * @param priority    importance level
     * @param status      current progress status
     * @param dueDate     optional deadline
     * @param category    category label
     * @param createdAt   original creation timestamp
     * @param updatedAt   last modification timestamp
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

    // ─── Getters ──────────────────────────────────────────────────────────────

    /** @return the task's unique database ID */
    public int getId() { return id; }

    /** @return the task title */
    public String getTitle() { return title; }

    /** @return the task description */
    public String getDescription() { return description; }

    /** @return the priority level */
    public Priority getPriority() { return priority; }

    /** @return the current status */
    public Status getStatus() { return status; }

    /** @return the due date, or null if not set */
    public LocalDate getDueDate() { return dueDate; }

    /** @return the category label */
    public String getCategory() { return category; }

    /** @return the creation timestamp */
    public LocalDateTime getCreatedAt() { return createdAt; }

    /** @return the last update timestamp */
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public int getUserId()           { return userId; }


    // ─── Setters ──────────────────────────────────────────────────────────────

    /** @param id the database ID to set */
    public void setId(int id) { this.id = id; }

    /** @param title the task title to set */
    public void setTitle(String title) { this.title = title; }

    /** @param description the description to set */
    public void setDescription(String description) { this.description = description; }

    /** @param priority the priority level to set */
    public void setPriority(Priority priority) { this.priority = priority; }

    /** @param status the status to set */
    public void setStatus(Status status) { this.status = status; }

    /** @param dueDate the due date to set */
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    /** @param category the category to set */
    public void setCategory(String category) { this.category = category; }

    /** @param createdAt the creation timestamp to set */
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    /** @param updatedAt the update timestamp to set */
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public void setUserId(int userId){ this.userId = userId; }

    // ─── Utility Methods ──────────────────────────────────────────────────────

    /**
     * Returns true if this task is past its due date and not yet completed.
     *
     * @return true if the task is overdue
     */
    public boolean isOverdue() {
        return dueDate != null
                && dueDate.isBefore(LocalDate.now())
                && status != Status.COMPLETED;
    }

    /**
     * Returns true if this task has been marked as completed.
     *
     * @return true if status is COMPLETED
     */
    public boolean isCompleted() {
        return status == Status.COMPLETED;
    }

    /**
     * Returns a readable summary of the task for debugging.
     *
     * @return formatted string with id, title, priority, and status
     */
    @Override
    public String toString() {
        return String.format("Task{id=%d, title='%s', priority=%s, status=%s}",
                id, title, priority, status);
    }
}