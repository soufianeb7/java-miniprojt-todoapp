package com.todoapp.dao;

import com.todoapp.database.DatabaseConnection;
import com.todoapp.models.Task;
import com.todoapp.models.Task.Priority;
import com.todoapp.models.Task.Status;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object pour la gestion des taches.
 * Chaque operation est filtree par user_id pour isoler les donnees de chaque utilisateur.
 */
public class TaskDAO {


    private static final String INSERT_TASK =
            "INSERT INTO tasks (title, description, priority, status, due_date, category, user_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String SELECT_ALL =
            "SELECT * FROM tasks WHERE user_id = ? ORDER BY created_at DESC";

    private static final String SELECT_BY_ID =
            "SELECT * FROM tasks WHERE id = ? AND user_id = ?";

    private static final String UPDATE_TASK =
            "UPDATE tasks SET title=?, description=?, priority=?, status=?, " +
                    "due_date=?, category=? WHERE id=? AND user_id=?";

    private static final String DELETE_TASK =
            "DELETE FROM tasks WHERE id=? AND user_id=?";

    private static final String MARK_COMPLETED =
            "UPDATE tasks SET status='COMPLETED' WHERE id=? AND user_id=?";

    private static final String SEARCH_TASKS =
            "SELECT * FROM tasks WHERE user_id=? AND title LIKE ? ORDER BY created_at DESC";

    private static final String FILTER_TASKS =
            "SELECT * FROM tasks WHERE user_id=? " +
                    "AND (? IS NULL OR status = ?) " +
                    "AND (? IS NULL OR priority = ?) " +
                    "ORDER BY created_at DESC";

    private static final String COUNT_ALL =
            "SELECT COUNT(*) FROM tasks WHERE user_id=?";

    private static final String COUNT_BY_STATUS =
            "SELECT COUNT(*) FROM tasks WHERE user_id=? AND status=?";


    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Convertit une ligne ResultSet en objet Task.
     */
    private Task mapRow(ResultSet rs) throws SQLException {
        Task task = new Task();
        task.setId(rs.getInt("id"));
        task.setTitle(rs.getString("title"));
        task.setDescription(rs.getString("description"));
        task.setPriority(Priority.valueOf(rs.getString("priority")));
        task.setStatus(Status.valueOf(rs.getString("status")));
        task.setCategory(rs.getString("category"));
        task.setUserId(rs.getInt("user_id"));

        Date dueDate = rs.getDate("due_date");
        task.setDueDate(dueDate != null ? dueDate.toLocalDate() : null);

        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        task.setCreatedAt(createdAt != null ? createdAt.toLocalDateTime() : LocalDateTime.now());
        task.setUpdatedAt(updatedAt != null ? updatedAt.toLocalDateTime() : LocalDateTime.now());

        return task;
    }

    // ─── CREATE ───────────────────────────────────────────────────────────────

    /**
     * Insere une nouvelle tache pour un utilisateur specifique.
     *
     * @param task la tache a creer (doit avoir un userId valide)
     * @return true si l'insertion a reussi
     */
    public boolean createTask(Task task) {
        try (PreparedStatement stmt = getConnection().prepareStatement(INSERT_TASK)) {
            stmt.setString(1, task.getTitle());
            stmt.setString(2, task.getDescription());
            stmt.setString(3, task.getPriority().name());
            stmt.setString(4, task.getStatus().name());
            stmt.setObject(5, task.getDueDate());
            stmt.setString(6, task.getCategory());
            stmt.setInt(7, task.getUserId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DAO] createTask: " + e.getMessage());
            return false;
        }
    }

    // ─── READ ─────────────────────────────────────────────────────────────────

    /**
     * Retourne toutes les taches appartenant a un utilisateur.
     *
     * @param userId l'identifiant de l'utilisateur connecte
     * @return liste des taches de cet utilisateur
     */
    public List<Task> getAllTasks(int userId) {
        List<Task> tasks = new ArrayList<>();
        try (PreparedStatement stmt = getConnection().prepareStatement(SELECT_ALL)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) tasks.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[DAO] getAllTasks: " + e.getMessage());
        }
        return tasks;
    }

    /**
     * Retourne une tache par son ID, uniquement si elle appartient a l'utilisateur.
     *
     * @param id     l'identifiant de la tache
     * @param userId l'identifiant de l'utilisateur connecte
     * @return la tache ou null si introuvable
     */
    public Task getTaskById(int id, int userId) {
        try (PreparedStatement stmt = getConnection().prepareStatement(SELECT_BY_ID)) {
            stmt.setInt(1, id);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("[DAO] getTaskById: " + e.getMessage());
        }
        return null;
    }

    // ─── UPDATE ───────────────────────────────────────────────────────────────

    /**
     * Met a jour une tache, uniquement si elle appartient a l'utilisateur.
     *
     * @param task la tache avec les nouvelles valeurs
     * @return true si la mise a jour a reussi
     */
    public boolean updateTask(Task task) {
        try (PreparedStatement stmt = getConnection().prepareStatement(UPDATE_TASK)) {
            stmt.setString(1, task.getTitle());
            stmt.setString(2, task.getDescription());
            stmt.setString(3, task.getPriority().name());
            stmt.setString(4, task.getStatus().name());
            stmt.setObject(5, task.getDueDate());
            stmt.setString(6, task.getCategory());
            stmt.setInt(7, task.getId());
            stmt.setInt(8, task.getUserId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DAO] updateTask: " + e.getMessage());
            return false;
        }
    }

    /**
     * Marque une tache comme terminee, uniquement si elle appartient a l'utilisateur.
     *
     * @param id     l'identifiant de la tache
     * @param userId l'identifiant de l'utilisateur connecte
     * @return true si la mise a jour a reussi
     */
    public boolean markTaskAsCompleted(int id, int userId) {
        try (PreparedStatement stmt = getConnection().prepareStatement(MARK_COMPLETED)) {
            stmt.setInt(1, id);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DAO] markTaskAsCompleted: " + e.getMessage());
            return false;
        }
    }

    // ─── DELETE ───────────────────────────────────────────────────────────────

    /**
     * Supprime une tache, uniquement si elle appartient a l'utilisateur.
     *
     * @param id     l'identifiant de la tache
     * @param userId l'identifiant de l'utilisateur connecte
     * @return true si la suppression a reussi
     */
    public boolean deleteTask(int id, int userId) {
        try (PreparedStatement stmt = getConnection().prepareStatement(DELETE_TASK)) {
            stmt.setInt(1, id);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DAO] deleteTask: " + e.getMessage());
            return false;
        }
    }

    // ─── SEARCH & FILTER ──────────────────────────────────────────────────────

    /**
     * Recherche les taches d'un utilisateur par mot-cle dans le titre.
     *
     * @param keyword le mot-cle de recherche
     * @param userId  l'identifiant de l'utilisateur connecte
     * @return liste des taches correspondantes
     */
    public List<Task> searchTasks(String keyword, int userId) {
        List<Task> tasks = new ArrayList<>();
        try (PreparedStatement stmt = getConnection().prepareStatement(SEARCH_TASKS)) {
            stmt.setInt(1, userId);
            stmt.setString(2, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) tasks.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[DAO] searchTasks: " + e.getMessage());
        }
        return tasks;
    }

    /**
     * Filtre les taches d'un utilisateur par statut et/ou priorite.
     *
     * @param status   statut a filtrer, ou null pour ignorer
     * @param priority priorite a filtrer, ou null pour ignorer
     * @param userId   l'identifiant de l'utilisateur connecte
     * @return liste des taches filtrees
     */
    public List<Task> filterTasks(Status status, Priority priority, int userId) {
        List<Task> tasks = new ArrayList<>();
        try (PreparedStatement stmt = getConnection().prepareStatement(FILTER_TASKS)) {
            String s = status   != null ? status.name()   : null;
            String p = priority != null ? priority.name() : null;
            stmt.setInt(1, userId);
            stmt.setString(2, s);
            stmt.setString(3, s);
            stmt.setString(4, p);
            stmt.setString(5, p);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) tasks.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[DAO] filterTasks: " + e.getMessage());
        }
        return tasks;
    }

    // ─── DASHBOARD ────────────────────────────────────────────────────────────

    /**
     * Retourne le nombre total de taches d'un utilisateur.
     *
     * @param userId l'identifiant de l'utilisateur
     * @return nombre total de taches
     */
    public int getTotalCount(int userId) {
        try (PreparedStatement stmt = getConnection().prepareStatement(COUNT_ALL)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[DAO] getTotalCount: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Retourne le nombre de taches d'un utilisateur avec un statut donne.
     *
     * @param status le statut a compter
     * @param userId l'identifiant de l'utilisateur
     * @return nombre de taches avec ce statut
     */
    public int getCountByStatus(Status status, int userId) {
        try (PreparedStatement stmt = getConnection().prepareStatement(COUNT_BY_STATUS)) {
            stmt.setInt(1, userId);
            stmt.setString(2, status.name());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[DAO] getCountByStatus: " + e.getMessage());
        }
        return 0;
    }
}