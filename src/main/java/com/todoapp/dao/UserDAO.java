package com.todoapp.dao;

import com.todoapp.database.DatabaseConnection;
import com.todoapp.models.User;
import com.todoapp.models.User.Role;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object pour la gestion des utilisateurs.
 * Gère la connexion, l'inscription et les opérations administrateur.
 */
public class UserDAO {

    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Convertit une ligne ResultSet en objet User.
     */
    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setFullName(rs.getString("full_name"));
        user.setRole(Role.valueOf(rs.getString("role")));
        Timestamp ts = rs.getTimestamp("created_at");
        user.setCreatedAt(ts != null ? ts.toLocalDateTime() : LocalDateTime.now());
        return user;
    }

    // ─── AUTHENTIFICATION ─────────────────────────────────────────────────────

    /**
     * Vérifie les identifiants et retourne l'utilisateur si valides.
     *
     * @param username le nom d'utilisateur
     * @param password le mot de passe
     * @return l'objet User ou null si identifiants incorrects
     */
    public User login(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("[DAO] login: " + e.getMessage());
        }
        return null;
    }

    /**
     * Crée un nouveau compte utilisateur avec le rôle USER par défaut.
     *
     * @param user l'utilisateur à enregistrer
     * @return true si l'inscription a réussi
     */
    public boolean register(User user) {
        String sql = "INSERT INTO users (username, password, full_name, role) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getFullName());
            stmt.setString(4, user.getRole() != null ? user.getRole().name() : "USER");
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DAO] register: " + e.getMessage());
            return false;
        }
    }

    /**
     * Vérifie si un nom d'utilisateur est déjà pris.
     *
     * @param username le nom à vérifier
     * @return true si le username existe déjà
     */
    public boolean usernameExists(String username) {
        String sql = "SELECT id FROM users WHERE username = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, username);
            return stmt.executeQuery().next();
        } catch (SQLException e) {
            System.err.println("[DAO] usernameExists: " + e.getMessage());
            return false;
        }
    }

    // ─── ADMINISTRATION ───────────────────────────────────────────────────────

    /**
     * Retourne la liste de tous les utilisateurs (réservé à l'admin).
     *
     * @return liste de tous les comptes utilisateurs
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY created_at DESC";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) users.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[DAO] getAllUsers: " + e.getMessage());
        }
        return users;
    }

    /**
     * Supprime un utilisateur et toutes ses tâches (CASCADE).
     * Un admin ne peut pas se supprimer lui-même.
     *
     * @param userId     l'ID de l'utilisateur à supprimer
     * @param adminId    l'ID de l'admin qui effectue la suppression
     * @return true si la suppression a réussi
     */
    public boolean deleteUser(int userId, int adminId) {
        if (userId == adminId) return false; // sécurité
        String sql = "DELETE FROM users WHERE id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DAO] deleteUser: " + e.getMessage());
            return false;
        }
    }

    /**
     * Modifie le rôle d'un utilisateur (USER ↔ ADMIN).
     *
     * @param userId l'ID de l'utilisateur
     * @param role   le nouveau rôle
     * @return true si la mise à jour a réussi
     */
    public boolean updateUserRole(int userId, Role role) {
        String sql = "UPDATE users SET role = ? WHERE id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, role.name());
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DAO] updateUserRole: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retourne le nombre total d'utilisateurs.
     *
     * @return nombre d'utilisateurs enregistrés
     */
    public int getTotalUsers() {
        String sql = "SELECT COUNT(*) FROM users";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[DAO] getTotalUsers: " + e.getMessage());
        }
        return 0;
    }
}