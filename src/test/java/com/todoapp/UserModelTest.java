package com.todoapp;

import com.todoapp.models.User;
import com.todoapp.models.User.Role;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour la classe User.
 * Aucune connexion à la base de données requise.
 */
@DisplayName("Tests du modèle User")
class UserModelTest {

    // ─── Constructeurs ────────────────────────────────────────────────────────

    @Test
    @DisplayName("Constructeur inscription — rôle USER par défaut")
    void testRegisterConstructorDefaultRole() {
        User user = new User("alice", "pass123", "Alice Martin");
        assertEquals(Role.USER, user.getRole(),
                "Un nouveau compte doit avoir le rôle USER par défaut");
    }

    @Test
    @DisplayName("Constructeur inscription — champs correctement initialisés")
    void testRegisterConstructorFields() {
        User user = new User("alice", "pass123", "Alice Martin");
        assertEquals("alice",        user.getUsername());
        assertEquals("pass123",      user.getPassword());
        assertEquals("Alice Martin", user.getFullName());
        assertNotNull(user.getCreatedAt());
    }

    @Test
    @DisplayName("Constructeur vide — champs null par défaut")
    void testDefaultConstructorNullFields() {
        User user = new User();
        assertNull(user.getUsername());
        assertNull(user.getPassword());
        assertEquals(0, user.getId());
    }

    // ─── Setters ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("setRole ADMIN — isAdmin() retourne true")
    void testSetRoleAdmin() {
        User user = new User("bob", "1234", "Bob");
        user.setRole(Role.ADMIN);
        assertTrue(user.isAdmin());
        assertEquals(Role.ADMIN, user.getRole());
    }

    @Test
    @DisplayName("setRole USER — isAdmin() retourne false")
    void testSetRoleUser() {
        User user = new User("bob", "1234", "Bob");
        user.setRole(Role.USER);
        assertFalse(user.isAdmin());
    }

    @Test
    @DisplayName("setId — mise à jour correcte")
    void testSetId() {
        User user = new User();
        user.setId(99);
        assertEquals(99, user.getId());
    }

    // ─── isAdmin() ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("isAdmin — false pour nouveau utilisateur")
    void testIsAdminFalseByDefault() {
        User user = new User("charlie", "abcd", "Charlie");
        assertFalse(user.isAdmin(),
                "Un utilisateur créé via le constructeur doit être USER");
    }

    @Test
    @DisplayName("isAdmin — true après promotion")
    void testIsAdminTrueAfterPromotion() {
        User user = new User("dave", "1234", "Dave");
        user.setRole(Role.ADMIN);
        assertTrue(user.isAdmin());
    }

    // ─── Enum Role ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Enum Role — 2 valeurs disponibles")
    void testRoleEnumValues() {
        Role[] values = Role.values();
        assertEquals(2, values.length);
        assertEquals(Role.USER,  values[0]);
        assertEquals(Role.ADMIN, values[1]);
    }

    @Test
    @DisplayName("Role.valueOf — conversion depuis String")
    void testRoleValueOf() {
        assertEquals(Role.USER,  Role.valueOf("USER"));
        assertEquals(Role.ADMIN, Role.valueOf("ADMIN"));
    }

    @Test
    @DisplayName("Role.valueOf — valeur invalide lève une exception")
    void testRoleValueOfInvalid() {
        assertThrows(IllegalArgumentException.class,
                () -> Role.valueOf("SUPERADMIN"),
                "Une valeur de rôle invalide doit lever IllegalArgumentException");
    }
}