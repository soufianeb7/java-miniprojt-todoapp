package com.todoapp;

import com.todoapp.dao.UserDAO;
import com.todoapp.models.User;
import com.todoapp.models.User.Role;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests d'intégration pour UserDAO.
 * Requiert une connexion MySQL active sur todoapp_db.
 */
@DisplayName("Tests UserDAO — Intégration BD")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserDAOTest {

    private static final UserDAO userDAO = new UserDAO();
    private static final String TEST_USERNAME = "testuser_junit_" + System.currentTimeMillis();
    private static int createdUserId = -1;

    // ─── Inscription ──────────────────────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("register — inscription d'un nouvel utilisateur réussit")
    void testRegisterSuccess() {
        User user = new User(TEST_USERNAME, "password123", "Test JUnit");
        boolean result = userDAO.register(user);
        assertTrue(result, "L'inscription doit réussir");
    }

    @Test
    @Order(2)
    @DisplayName("usernameExists — retourne true pour username existant")
    void testUsernameExistsTrue() {
        assertTrue(userDAO.usernameExists(TEST_USERNAME),
                "Le username doit exister après inscription");
    }

    @Test
    @Order(3)
    @DisplayName("usernameExists — retourne false pour username inexistant")
    void testUsernameExistsFalse() {
        assertFalse(userDAO.usernameExists("username_qui_nexiste_pas_99999"),
                "Un username inexistant doit retourner false");
    }

    @Test
    @Order(4)
    @DisplayName("register — échec si username déjà pris")
    void testRegisterDuplicateUsername() {
        User duplicate = new User(TEST_USERNAME, "autrepass", "Autre Nom");
        boolean result = userDAO.register(duplicate);
        assertFalse(result, "L'inscription doit échouer avec un username déjà pris");
    }

    // ─── Connexion ────────────────────────────────────────────────────────────

    @Test
    @Order(5)
    @DisplayName("login — identifiants corrects retournent un User")
    void testLoginSuccess() {
        User user = userDAO.login(TEST_USERNAME, "password123");
        assertNotNull(user, "Login avec bons identifiants doit retourner un User");
        assertEquals(TEST_USERNAME, user.getUsername());
        assertEquals("Test JUnit",  user.getFullName());
        assertEquals(Role.USER,     user.getRole());
        createdUserId = user.getId();
    }

    @Test
    @Order(6)
    @DisplayName("login — mauvais mot de passe retourne null")
    void testLoginWrongPassword() {
        User user = userDAO.login(TEST_USERNAME, "mauvaismdp");
        assertNull(user, "Login avec mauvais mot de passe doit retourner null");
    }

    @Test
    @Order(7)
    @DisplayName("login — username inexistant retourne null")
    void testLoginUnknownUsername() {
        User user = userDAO.login("utilisateur_inexistant_xyz", "mdp");
        assertNull(user, "Login avec username inexistant doit retourner null");
    }

    // ─── Administration ───────────────────────────────────────────────────────

    @Test
    @Order(8)
    @DisplayName("getAllUsers — retourne une liste non vide")
    void testGetAllUsersNotEmpty() {
        var users = userDAO.getAllUsers();
        assertNotNull(users);
        assertFalse(users.isEmpty(), "La liste doit contenir au moins un utilisateur");
    }

    @Test
    @Order(9)
    @DisplayName("getAllUsers — contient le compte admin")
    void testGetAllUsersContainsAdmin() {
        boolean hasAdmin = userDAO.getAllUsers().stream()
                .anyMatch(u -> u.getRole() == Role.ADMIN);
        assertTrue(hasAdmin, "La liste doit contenir au moins un admin");
    }

    @Test
    @Order(10)
    @DisplayName("getTotalUsers — retourne un nombre positif")
    void testGetTotalUsersPositive() {
        int total = userDAO.getTotalUsers();
        assertTrue(total > 0, "Le total doit être supérieur à 0");
    }

    @Test
    @Order(11)
    @DisplayName("updateUserRole — promotion USER → ADMIN réussit")
    void testUpdateUserRoleToAdmin() {
        if (createdUserId == -1) {
            User u = userDAO.login(TEST_USERNAME, "password123");
            if (u != null) createdUserId = u.getId();
        }
        Assumptions.assumeTrue(createdUserId > 0, "ID utilisateur requis");

        boolean result = userDAO.updateUserRole(createdUserId, Role.ADMIN);
        assertTrue(result, "La promotion en ADMIN doit réussir");

        User updated = userDAO.login(TEST_USERNAME, "password123");
        assertNotNull(updated);
        assertEquals(Role.ADMIN, updated.getRole(), "Le rôle doit être ADMIN");
    }

    @Test
    @Order(12)
    @DisplayName("updateUserRole — rétrogradation ADMIN → USER réussit")
    void testUpdateUserRoleToUser() {
        Assumptions.assumeTrue(createdUserId > 0, "ID utilisateur requis");

        boolean result = userDAO.updateUserRole(createdUserId, Role.USER);
        assertTrue(result, "La rétrogradation en USER doit réussir");
    }

    // ─── Suppression ──────────────────────────────────────────────────────────

    @Test
    @Order(13)
    @DisplayName("deleteUser — suppression d'un autre utilisateur réussit")
    void testDeleteUserSuccess() {
        Assumptions.assumeTrue(createdUserId > 0, "ID utilisateur requis");

        // L'admin (id=1) supprime l'utilisateur de test
        boolean result = userDAO.deleteUser(createdUserId, 1);
        assertTrue(result, "La suppression doit réussir");
    }

    @Test
    @Order(14)
    @DisplayName("deleteUser — auto-suppression impossible")
    void testDeleteUserSelfBlocked() {
        // L'admin ne peut pas se supprimer lui-même
        boolean result = userDAO.deleteUser(1, 1);
        assertFalse(result, "Un utilisateur ne peut pas se supprimer lui-même");
    }

    @Test
    @Order(15)
    @DisplayName("login — après suppression, compte inaccessible")
    void testLoginAfterDeletion() {
        User user = userDAO.login(TEST_USERNAME, "password123");
        assertNull(user, "Le login doit échouer après suppression du compte");
    }
}