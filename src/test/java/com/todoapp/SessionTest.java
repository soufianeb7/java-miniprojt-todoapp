package com.todoapp;

import com.todoapp.models.Session;
import com.todoapp.models.User;
import com.todoapp.models.User.Role;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour la classe Session.
 * Aucune connexion à la base de données requise.
 */
@DisplayName("Tests de la classe Session")
class SessionTest {

    @BeforeEach
    void setUp() {
        Session.clear(); // état propre avant chaque test
    }

    @AfterEach
    void tearDown() {
        Session.clear(); // nettoyage après chaque test
    }

    @Test
    @DisplayName("isLoggedIn — false si aucun utilisateur connecté")
    void testIsLoggedInFalseInitially() {
        assertFalse(Session.isLoggedIn(),
                "isLoggedIn doit retourner false au démarrage");
    }

    @Test
    @DisplayName("isLoggedIn — true après setCurrentUser")
    void testIsLoggedInTrueAfterSet() {
        User user = new User("test", "1234", "Test User");
        Session.setCurrentUser(user);
        assertTrue(Session.isLoggedIn());
    }

    @Test
    @DisplayName("getCurrentUser — retourne null si non connecté")
    void testGetCurrentUserNullInitially() {
        assertNull(Session.getCurrentUser());
    }

    @Test
    @DisplayName("getCurrentUser — retourne l'utilisateur correct")
    void testGetCurrentUserReturnsCorrectUser() {
        User user = new User("alice", "pass", "Alice");
        user.setId(5);
        Session.setCurrentUser(user);

        assertNotNull(Session.getCurrentUser());
        assertEquals("alice", Session.getCurrentUser().getUsername());
        assertEquals(5,       Session.getCurrentUser().getId());
        assertEquals("Alice", Session.getCurrentUser().getFullName());
    }

    @Test
    @DisplayName("clear — efface la session")
    void testClearSession() {
        User user = new User("bob", "1234", "Bob");
        Session.setCurrentUser(user);
        assertTrue(Session.isLoggedIn());

        Session.clear();

        assertFalse(Session.isLoggedIn(), "isLoggedIn doit être false après clear");
        assertNull(Session.getCurrentUser(), "getCurrentUser doit être null après clear");
    }

    @Test
    @DisplayName("Remplacer l'utilisateur connecté")
    void testReplaceCurrentUser() {
        User user1 = new User("user1", "pass1", "User One");
        User user2 = new User("user2", "pass2", "User Two");

        Session.setCurrentUser(user1);
        assertEquals("user1", Session.getCurrentUser().getUsername());

        Session.setCurrentUser(user2);
        assertEquals("user2", Session.getCurrentUser().getUsername(),
                "Le deuxième utilisateur doit remplacer le premier");
    }

    @Test
    @DisplayName("Session admin — isAdmin() accessible via Session")
    void testSessionAdminRole() {
        User admin = new User("admin", "admin123", "Admin");
        admin.setRole(Role.ADMIN);
        Session.setCurrentUser(admin);

        assertTrue(Session.getCurrentUser().isAdmin());
    }

    @Test
    @DisplayName("Session user — isAdmin() false par défaut")
    void testSessionUserRole() {
        User user = new User("john", "1234", "John");
        Session.setCurrentUser(user);

        assertFalse(Session.getCurrentUser().isAdmin());
    }
}