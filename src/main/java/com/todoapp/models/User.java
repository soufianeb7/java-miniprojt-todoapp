package com.todoapp.models;

import java.time.LocalDateTime;

/**
 * Représente un utilisateur inscrit de l'application de gestion de tâches.
 * Cette classe correspond à une ligne de la table {@code users} et contient
 * les informations nécessaires pour l'authentification, l'affichage et la
 * gestion des droits (rôle) d'un utilisateur.
 * Remarques de sécurité : dans ce projet, j'ai stocké le mot de passe en clair.
 * En production, il faut impérativement stocker un hachage sécurisé et ne jamais
 * conserver de mot de passe en clair dans le code ou en base.
 */
public class User {

    /**
     * Enumération des rôles possibles pour un utilisateur.
     * USER : utilisateur standard.
     * ADMIN : administrateur avec droits supplémentaires.
     */
    public enum Role {
        USER, ADMIN
    }

    /** Identifiant unique de l'utilisateur (clé primaire en base). */
    private int id;

    /** Nom d'utilisateur utilisé pour la connexion (doit être unique). */
    private String username;

    /** Mot de passe en clair pour ce projet (en production, ne pas stocker en clair). */
    private String password;

    /** Nom complet affiché de l'utilisateur. */
    private String fullName;

    /** Date/heure de création du compte. */
    private LocalDateTime createdAt;

    /** Rôle courant de l'utilisateur (USER ou ADMIN). */
    private Role role;

    /** Constructeur par défaut (utile pour les frameworks et DAO). */
    public User() {}

    /**
     * Constructeur utilisé lors de l'inscription d'un nouvel utilisateur.
     * Le rôle est initialisé à {@link Role#USER} par défaut et
     * {@code createdAt} à l'instant courant.
     * @param username nom de connexion unique
     * @param password mot de passe en clair (pour simplfie les choses dans ce projet)
     * @param fullName nom affiché de l'utilisateur
     */
    public User(String username, String password, String fullName) {
        this.username  = username;
        this.password  = password;
        this.fullName  = fullName;
        this.role      = Role.USER; // par défaut USER
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Retourne l'identifiant de l'utilisateur.
     *
     * @return id unique en base de données
     */
    public int getId() { return id; }

    /**
     * Retourne le nom d'utilisateur utilisé pour la connexion.
     *
     * @return username (login)
     */
    public String getUsername() { return username; }

    /**
     * Retourne le mot de passe de l'utilisateur.
     * @return mot de passe (en clair dans ce projet)
     */
    public String getPassword() { return password; }

    /**
     * Retourne le nom complet affiché de l'utilisateur.
     *
     * @return fullName
     */
    public String getFullName() { return fullName; }

    /**
     * Retourne la date et l'heure de création du compte.
     *
     * @return createdAt (LocalDateTime) ou {@code null} si non défini
     */
    public LocalDateTime getCreatedAt() { return createdAt; }

    /**
     * Retourne le rôle de l'utilisateur.
     *
     * @return role (USER ou ADMIN)
     */
    public Role getRole() { return role; }

    /**
     * Définit l'identifiant de l'utilisateur (généralement attribué par la base).
     *
     * @param id id à définir
     */
    public void setId(int id) { this.id = id; }

    /**
     * Définit le nom d'utilisateur (login).
     *
     * @param username nouveau nom d'utilisateur
     */
    public void setUsername(String username) { this.username = username; }

    /**
     * Définit le mot de passe de l'utilisateur.
     * @param password mot de passe (en clair pour ce projet)
     */
    public void setPassword(String password) { this.password = password; }

    /**
     * Définit le nom complet affiché de l'utilisateur.
     *
     * @param fullName nom complet
     */
    public void setFullName(String fullName) { this.fullName = fullName; }

    /**
     * Définit la date/heure de création du compte.
     *
     * @param t date/heure de création
     */
    public void setCreatedAt(LocalDateTime t) { this.createdAt = t; }

    /**
     * Définit le rôle de l'utilisateur.
     *
     * @param role rôle à attribuer (USER ou ADMIN)
     */
    public void setRole(Role role) { this.role = role; }

    /**
     * Indique si l'utilisateur possède le rôle administrateur.
     *
     * @return {@code true} si le rôle est {@link Role#ADMIN}, sinon {@code false}
     */
    public boolean isAdmin() { return role == Role.ADMIN; }
}