package com.todoapp.models;

/**
 * Classe utilitaire conservant l'utilisateur actuellement connecté pour la session.
 *
 * Permet d'accéder facilement à l'utilisateur courant depuis n'importe quel
 * contrôleur sans devoir transmettre l'objet explicitement.
 *
 * API fournie :
 * - {@link #setCurrentUser(User)} : enregistre l'utilisateur connecté
 * - {@link #getCurrentUser()} : retourne l'utilisateur courant
 * - {@link #clear()} : vide la session (déconnexion)
 * - {@link #isLoggedIn()} : indique si un utilisateur est connecté
 */
public class Session {

    /** Référence statique vers l'utilisateur courant (ou {@code null}). */
    private static User currentUser;

    /**
     * Enregistre l'utilisateur courant dans la session.
     *
     * @param user instance de {@link User} représentant l'utilisateur connecté
     */
    public static void setCurrentUser(User user) { currentUser = user; }

    /**
     * Retourne l'utilisateur actuellement enregistré en session.
     *
     * @return l'utilisateur courant, ou {@code null} si personne n'est connecté
     */
    public static User getCurrentUser() { return currentUser; }

    /**
     * Efface l'utilisateur stocké (déconnexion).
     */
    public static void clear() { currentUser = null; }

    /**
     * Indique si un utilisateur est actuellement connecté.
     *
     * @return {@code true} si {@link #getCurrentUser()} != {@code null}
     */
    public static boolean isLoggedIn() { return currentUser != null; }
}