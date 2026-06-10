package com.todoapp;

import com.todoapp.dao.TaskDAO;
import com.todoapp.dao.UserDAO;
import com.todoapp.models.Task;
import com.todoapp.models.Task.Priority;
import com.todoapp.models.Task.Status;
import com.todoapp.models.User;
import org.junit.jupiter.api.*;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests d'intégration pour TaskDAO.
 * Requiert une connexion MySQL active sur todoapp_db.
 * Utilise un utilisateur de test créé et supprimé automatiquement.
 */
@DisplayName("Tests TaskDAO — Intégration BD")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TaskDAOTest {

    private static final TaskDAO taskDAO = new TaskDAO();
    private static final UserDAO userDAO = new UserDAO();

    private static int testUserId   = -1;
    private static int createdTaskId = -1;

    private static final String TEST_USER = "taskdao_test_" + System.currentTimeMillis();

    // ─── Préparation et nettoyage ─────────────────────────────────────────────

    @BeforeAll
    static void createTestUser() {
        User user = new User(TEST_USER, "test1234", "Test TaskDAO");
        userDAO.register(user);
        User created = userDAO.login(TEST_USER, "test1234");
        assertNotNull(created, "L'utilisateur de test doit être créé");
        testUserId = created.getId();
    }

    @AfterAll
    static void deleteTestUser() {
        if (testUserId > 0) {
            userDAO.deleteUser(testUserId, 1);
        }
    }

    // ─── CREATE ───────────────────────────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("createTask — création d'une tâche réussit")
    void testCreateTask() {
        Task task = new Task(
                "Tâche de test JUnit",
                "Description de test",
                Priority.HIGH,
                Status.PENDING,
                LocalDate.now().plusDays(5),
                "Study",
                testUserId
        );
        boolean result = taskDAO.createTask(task);
        assertTrue(result, "La création de tâche doit réussir");
    }

    @Test
    @Order(2)
    @DisplayName("createTask — tâche sans description ni date")
    void testCreateTaskMinimal() {
        Task task = new Task(
                "Tâche minimale",
                null,
                Priority.LOW,
                Status.PENDING,
                null,
                "General",
                testUserId
        );
        boolean result = taskDAO.createTask(task);
        assertTrue(result, "Une tâche sans description ni date doit pouvoir être créée");
    }

    // ─── READ ─────────────────────────────────────────────────────────────────

    @Test
    @Order(3)
    @DisplayName("getAllTasks — retourne les tâches de l'utilisateur")
    void testGetAllTasksNotEmpty() {
        List<Task> tasks = taskDAO.getAllTasks(testUserId);
        assertNotNull(tasks);
        assertFalse(tasks.isEmpty(),
                "La liste doit contenir les tâches créées précédemment");
        createdTaskId = tasks.get(0).getId();
    }

    @Test
    @Order(4)
    @DisplayName("getAllTasks — isolation : ne voit pas les tâches des autres")
    void testGetAllTasksIsolation() {
        List<Task> tasksUser1 = taskDAO.getAllTasks(1);
        List<Task> tasksTest  = taskDAO.getAllTasks(testUserId);

        boolean overlap = tasksUser1.stream()
                .anyMatch(t1 -> tasksTest.stream()
                        .anyMatch(t2 -> t2.getId() == t1.getId()));

        assertFalse(overlap, "Les tâches de deux utilisateurs ne doivent pas se mélanger");
    }

    @Test
    @Order(5)
    @DisplayName("getTaskById — retourne la bonne tâche")
    void testGetTaskById() {
        Assumptions.assumeTrue(createdTaskId > 0, "ID de tâche requis");

        Task task = taskDAO.getTaskById(createdTaskId, testUserId);
        assertNotNull(task, "La tâche doit être trouvée par son ID");
        assertEquals(createdTaskId, task.getId());
        assertEquals(testUserId,    task.getUserId());
    }

    @Test
    @Order(6)
    @DisplayName("getTaskById — retourne null pour ID inexistant")
    void testGetTaskByIdNotFound() {
        Task task = taskDAO.getTaskById(999999, testUserId);
        assertNull(task, "Doit retourner null pour un ID inexistant");
    }

    @Test
    @Order(7)
    @DisplayName("getTaskById — isolation : ne peut pas accéder à la tâche d'un autre")
    void testGetTaskByIdWrongUser() {
        Assumptions.assumeTrue(createdTaskId > 0, "ID de tâche requis");

        // Essayer d'accéder à la tâche testUserId avec l'userId 1
        Task task = taskDAO.getTaskById(createdTaskId, 999);
        assertNull(task, "Ne doit pas accéder à la tâche d'un autre utilisateur");
    }

    // ─── UPDATE ───────────────────────────────────────────────────────────────

    @Test
    @Order(8)
    @DisplayName("updateTask — mise à jour réussit")
    void testUpdateTask() {
        Assumptions.assumeTrue(createdTaskId > 0, "ID de tâche requis");

        Task task = taskDAO.getTaskById(createdTaskId, testUserId);
        assertNotNull(task);

        task.setTitle("Titre mis à jour par JUnit");
        task.setPriority(Priority.MEDIUM);
        task.setCategory("Work");

        boolean result = taskDAO.updateTask(task);
        assertTrue(result, "La mise à jour doit réussir");

        Task updated = taskDAO.getTaskById(createdTaskId, testUserId);
        assertNotNull(updated);
        assertEquals("Titre mis à jour par JUnit", updated.getTitle());
        assertEquals(Priority.MEDIUM, updated.getPriority());
        assertEquals("Work", updated.getCategory());
    }

    @Test
    @Order(9)
    @DisplayName("markTaskAsCompleted — statut passe à COMPLETED")
    void testMarkTaskAsCompleted() {
        Assumptions.assumeTrue(createdTaskId > 0, "ID de tâche requis");

        boolean result = taskDAO.markTaskAsCompleted(createdTaskId, testUserId);
        assertTrue(result, "Le marquage comme terminé doit réussir");

        Task task = taskDAO.getTaskById(createdTaskId, testUserId);
        assertNotNull(task);
        assertEquals(Status.COMPLETED, task.getStatus(),
                "Le statut doit être COMPLETED après markTaskAsCompleted");
        assertTrue(task.isCompleted());
    }

    // ─── DASHBOARD ────────────────────────────────────────────────────────────

    @Test
    @Order(10)
    @DisplayName("getTotalCount — retourne le bon nombre de tâches")
    void testGetTotalCount() {
        int count = taskDAO.getTotalCount(testUserId);
        assertTrue(count >= 2, "Doit avoir au moins les 2 tâches créées dans les tests");
    }

    @Test
    @Order(11)
    @DisplayName("getCountByStatus — compte correctement les COMPLETED")
    void testGetCountByStatusCompleted() {
        int count = taskDAO.getCountByStatus(Status.COMPLETED, testUserId);
        assertTrue(count >= 1,
                "Doit avoir au moins 1 tâche COMPLETED après markTaskAsCompleted");
    }

    @Test
    @Order(12)
    @DisplayName("getCountByStatus — isolation par utilisateur")
    void testGetCountByStatusIsolation() {
        int countUser1 = taskDAO.getCountByStatus(Status.COMPLETED, 1);
        int countTest  = taskDAO.getCountByStatus(Status.COMPLETED, testUserId);

        // Les compteurs doivent être indépendants
        assertNotEquals(
                countUser1 + countTest,
                taskDAO.getCountByStatus(Status.COMPLETED, 0),
                "Les compteurs doivent être isolés par utilisateur"
        );
    }

    // ─── RECHERCHE ET FILTRAGE ────────────────────────────────────────────────

    @Test
    @Order(13)
    @DisplayName("searchTasks — trouve les tâches par mot-clé")
    void testSearchTasksFound() {
        List<Task> results = taskDAO.searchTasks("JUnit", testUserId);
        assertFalse(results.isEmpty(),
                "La recherche 'JUnit' doit trouver la tâche mise à jour");
    }

    @Test
    @Order(14)
    @DisplayName("searchTasks — retourne liste vide si aucun résultat")
    void testSearchTasksNotFound() {
        List<Task> results = taskDAO.searchTasks("XYZ_TERME_INEXISTANT_999", testUserId);
        assertTrue(results.isEmpty(), "Aucun résultat attendu pour ce terme");
    }

    @Test
    @Order(15)
    @DisplayName("searchTasks — isolation par utilisateur")
    void testSearchTasksIsolation() {
        List<Task> results = taskDAO.searchTasks("JUnit", 1);
        assertTrue(results.isEmpty(),
                "L'utilisateur 1 ne doit pas voir les tâches du testUser");
    }

    @Test
    @Order(16)
    @DisplayName("filterTasks — filtre par statut COMPLETED")
    void testFilterByStatusCompleted() {
        List<Task> results = taskDAO.filterTasks(Status.COMPLETED, null, testUserId);
        assertFalse(results.isEmpty(), "Doit trouver au moins 1 tâche COMPLETED");
        results.forEach(t ->
                assertEquals(Status.COMPLETED, t.getStatus(),
                        "Toutes les tâches filtrées doivent être COMPLETED")
        );
    }

    @Test
    @Order(17)
    @DisplayName("filterTasks — filtre par priorité MEDIUM")
    void testFilterByPriorityMedium() {
        List<Task> results = taskDAO.filterTasks(null, Priority.MEDIUM, testUserId);
        results.forEach(t ->
                assertEquals(Priority.MEDIUM, t.getPriority(),
                        "Toutes les tâches filtrées doivent être MEDIUM")
        );
    }

    @Test
    @Order(18)
    @DisplayName("filterTasks — null null retourne toutes les tâches")
    void testFilterNoFilter() {
        List<Task> all      = taskDAO.getAllTasks(testUserId);
        List<Task> filtered = taskDAO.filterTasks(null, null, testUserId);
        assertEquals(all.size(), filtered.size(),
                "Sans filtre, toutes les tâches doivent être retournées");
    }

    // ─── DELETE ───────────────────────────────────────────────────────────────

    @Test
    @Order(19)
    @DisplayName("deleteTask — suppression réussit")
    void testDeleteTask() {
        Assumptions.assumeTrue(createdTaskId > 0, "ID de tâche requis");

        boolean result = taskDAO.deleteTask(createdTaskId, testUserId);
        assertTrue(result, "La suppression doit réussir");
    }

    @Test
    @Order(20)
    @DisplayName("deleteTask — après suppression, tâche introuvable")
    void testDeleteTaskVerify() {
        Assumptions.assumeTrue(createdTaskId > 0, "ID de tâche requis");

        Task task = taskDAO.getTaskById(createdTaskId, testUserId);
        assertNull(task, "La tâche ne doit plus exister après suppression");
    }

    @Test
    @Order(21)
    @DisplayName("deleteTask — isolation : ne peut pas supprimer la tâche d'un autre")
    void testDeleteTaskWrongUser() {
        // Créer une tâche pour testUser
        Task task = new Task("Tâche protégée", null,
                Priority.LOW, Status.PENDING, null, "General", testUserId);
        taskDAO.createTask(task);

        List<Task> tasks = taskDAO.getAllTasks(testUserId);
        Assumptions.assumeFalse(tasks.isEmpty(), "Tâche de test requise");
        int protectedId = tasks.get(0).getId();

        // Tenter de supprimer avec un autre userId
        boolean result = taskDAO.deleteTask(protectedId, 999);
        assertFalse(result,
                "Un utilisateur ne doit pas pouvoir supprimer la tâche d'un autre");

        // Vérifier que la tâche existe encore
        Task stillExists = taskDAO.getTaskById(protectedId, testUserId);
        assertNotNull(stillExists, "La tâche doit encore exister après tentative échouée");
    }
}