package com.todoapp.models;

/**
 * Holds the currently logged-in user for the session.
 * Accessible from any controller without passing the user object manually.
 */
public class Session {

    private static User currentUser;

    public static void setCurrentUser(User user) { currentUser = user; }
    public static User getCurrentUser()          { return currentUser; }
    public static void clear()                   { currentUser = null; }
    public static boolean isLoggedIn()           { return currentUser != null; }
}