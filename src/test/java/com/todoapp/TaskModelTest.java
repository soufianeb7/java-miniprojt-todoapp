package com.todoapp;

import com.todoapp.models.Task;
import com.todoapp.models.Task.Priority;
import com.todoapp.models.Task.Status;
import org.junit.jupiter.api.*;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour la classe Task.
 * Aucune connexion à la base de données requise.
 */
@DisplayName("Tests du modèle Task")
class TaskModelTest {

    private Task task;

    @BeforeEach
    void setUp() {
        task = new Task(
                "Terminer le projet",
                "Finaliser toutes les étapes",
                Priority.HIGH,
                Status.PENDING,
                LocalDate.now().plusDays(7),
                "Study",
                1
        );
    }

    // ─── Constructeurs ────────────────────────────────────────────────────────

    @Test
    @DisplayName("Constructeur vide — valeurs par défaut correctes")
    void testDefaultConstructor() {
        Task t = new Task();
        assertEquals(Priority.MEDIUM, t.getPriority(),
                "La priorité par défaut doit être MEDIUM");
        assertEquals(Status.PENDING, t.getStatus(),
                "Le statut par défaut doit être PENDING");
        assertNotNull(t.getCreatedAt(), "createdAt ne doit pas être null");
    }

    @Test
    @DisplayName("Constructeur complet — tous les champs initialisés")
    void testFullConstructor() {
        assertEquals("Terminer le projet", task.getTitle());
        assertEquals("Finaliser toutes les étapes", task.getDescription());
        assertEquals(Priority.HIGH, task.getPriority());
        assertEquals(Status.PENDING, task.getStatus());
        assertEquals("Study", task.getCategory());
        assertEquals(1, task.getUserId());
        assertNotNull(task.getDueDate());
    }

    // ─── Getters et Setters ───────────────────────────────────────────────────

    @Test
    @DisplayName("Setter titre — mise à jour correcte")
    void testSetTitle() {
        task.setTitle("Nouveau titre");
        assertEquals("Nouveau titre", task.getTitle());
    }

    @Test
    @DisplayName("Setter priorité — mise à jour correcte")
    void testSetPriority() {
        task.setPriority(Priority.LOW);
        assertEquals(Priority.LOW, task.getPriority());
    }

    @Test
    @DisplayName("Setter statut — mise à jour correcte")
    void testSetStatus() {
        task.setStatus(Status.COMPLETED);
        assertEquals(Status.COMPLETED, task.getStatus());
    }

    @Test
    @DisplayName("Setter date d'échéance null — autorisé")
    void testSetDueDateNull() {
        task.setDueDate(null);
        assertNull(task.getDueDate());
    }

    // ─── isCompleted() ────────────────────────────────────────────────────────

    @Test
    @DisplayName("isCompleted — retourne false si PENDING")
    void testIsCompletedFalseWhenPending() {
        task.setStatus(Status.PENDING);
        assertFalse(task.isCompleted());
    }

    @Test
    @DisplayName("isCompleted — retourne false si IN_PROGRESS")
    void testIsCompletedFalseWhenInProgress() {
        task.setStatus(Status.IN_PROGRESS);
        assertFalse(task.isCompleted());
    }

    @Test
    @DisplayName("isCompleted — retourne true si COMPLETED")
    void testIsCompletedTrue() {
        task.setStatus(Status.COMPLETED);
        assertTrue(task.isCompleted());
    }

    // ─── isOverdue() ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("isOverdue — false si date future et PENDING")
    void testIsOverdueFalseFutureDate() {
        task.setDueDate(LocalDate.now().plusDays(10));
        task.setStatus(Status.PENDING);
        assertFalse(task.isOverdue());
    }

    @Test
    @DisplayName("isOverdue — true si date passée et PENDING")
    void testIsOverdueTruePastDate() {
        task.setDueDate(LocalDate.now().minusDays(1));
        task.setStatus(Status.PENDING);
        assertTrue(task.isOverdue());
    }

    @Test
    @DisplayName("isOverdue — false si date passée mais COMPLETED")
    void testIsOverdueFalseWhenCompleted() {
        task.setDueDate(LocalDate.now().minusDays(5));
        task.setStatus(Status.COMPLETED);
        assertFalse(task.isOverdue(), "Une tâche terminée ne peut pas être en retard");
    }

    @Test
    @DisplayName("isOverdue — false si aucune date d'échéance")
    void testIsOverdueFalseNullDate() {
        task.setDueDate(null);
        task.setStatus(Status.PENDING);
        assertFalse(task.isOverdue(), "Sans date, la tâche ne peut pas être en retard");
    }

    @Test
    @DisplayName("isOverdue — false si date passée et IN_PROGRESS")
    void testIsOverdueTrueInProgress() {
        task.setDueDate(LocalDate.now().minusDays(2));
        task.setStatus(Status.IN_PROGRESS);
        assertTrue(task.isOverdue());
    }

    // ─── Enums ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Enum Priority — 3 valeurs disponibles")
    void testPriorityEnumValues() {
        Priority[] values = Priority.values();
        assertEquals(3, values.length);
        assertEquals(Priority.LOW,    values[0]);
        assertEquals(Priority.MEDIUM, values[1]);
        assertEquals(Priority.HIGH,   values[2]);
    }

    @Test
    @DisplayName("Enum Status — 3 valeurs disponibles")
    void testStatusEnumValues() {
        Status[] values = Status.values();
        assertEquals(3, values.length);
        assertEquals(Status.PENDING,     values[0]);
        assertEquals(Status.IN_PROGRESS, values[1]);
        assertEquals(Status.COMPLETED,   values[2]);
    }

    @Test
    @DisplayName("Priority.valueOf — conversion depuis String")
    void testPriorityValueOf() {
        assertEquals(Priority.HIGH,   Priority.valueOf("HIGH"));
        assertEquals(Priority.MEDIUM, Priority.valueOf("MEDIUM"));
        assertEquals(Priority.LOW,    Priority.valueOf("LOW"));
    }

    @Test
    @DisplayName("Status.valueOf — conversion depuis String")
    void testStatusValueOf() {
        assertEquals(Status.PENDING,     Status.valueOf("PENDING"));
        assertEquals(Status.IN_PROGRESS, Status.valueOf("IN_PROGRESS"));
        assertEquals(Status.COMPLETED,   Status.valueOf("COMPLETED"));
    }

    // ─── toString ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("toString — contient les informations essentielles")
    void testToString() {
        task.setId(42);
        String str = task.toString();
        assertTrue(str.contains("42"),               "Doit contenir l'ID");
        assertTrue(str.contains("Terminer le projet"),"Doit contenir le titre");
        assertTrue(str.contains("HIGH"),             "Doit contenir la priorité");
        assertTrue(str.contains("PENDING"),          "Doit contenir le statut");
    }
}