package com.todoapp.models;

import java.time.LocalDateTime;

/**
 * Représente un utilisateur inscrit de l'application de liste de tâches.
 * Correspond à une ligne de la table {@code users}.
 */
public class User {

    /** Rôle de l'utilisateur dans l'application. */
    public enum Role {
        USER, ADMIN
    }
    private int           id;
    private String        username;
    private String        password;
    private String        fullName;
    private LocalDateTime createdAt;
    private Role role;

    /** Constructeur par défaut. */
    public User() {}

    /**
     * Constructeur utilisé lors de l'inscription d'un nouvel utilisateur.
     *
     * @param username nom de connexion unique
     * @param password mot de passe en clair (stocké tel quel pour ce projet)
     * @param fullName nom affiché de l'utilisateur
     */
    public User(String username, String password, String fullName) {
        this.username  = username;
        this.password  = password;
        this.fullName  = fullName;
        this.role      = Role.USER; // par défaut USER
        this.createdAt = LocalDateTime.now();
    }

    public int           getId()        { return id; }
    public String        getUsername()  { return username; }
    public String        getPassword()  { return password; }
    public String        getFullName()  { return fullName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Role getRole()          { return role; }

    public void setId(int id)                    { this.id = id; }
    public void setUsername(String username)     { this.username = username; }
    public void setPassword(String password)     { this.password = password; }
    public void setFullName(String fullName)     { this.fullName = fullName; }
    public void setCreatedAt(LocalDateTime t)    { this.createdAt = t; }
    public void setRole(Role role) { this.role = role; }

    /** Retourne true si l'utilisateur est administrateur. */
    public boolean isAdmin() { return role == Role.ADMIN; }
}