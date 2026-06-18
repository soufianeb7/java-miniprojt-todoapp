package com.todoapp.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Gère la connexion à la base de données MySQL pour l'application.
 *
 * Cette classe applique le pattern Singleton pour garantir qu'une seule
 * instance de gestion de connexion est utilisée dans toute l'application.
 *
 * Fonctionnement général :
 *
 *   La première demande via {@link #getInstance()} crée la connexion JDBC.
 *   Les appels suivants réutilisent la même instance tant que la connexion est ouverte.
 *   La méthode {@link #closeConnection()} ferme explicitement la connexion partagée.
 *
 *
 * Exemple d'utilisation :
 *
 *  Connection conn = DatabaseConnection.getInstance().getConnection();
 *
 */
public class DatabaseConnection {

    // ─── Configuration ────────────────────────────────────────────────────────

    /** Adresse du serveur MySQL (hôte local dans ce projet). */
    private static final String HOST = "localhost";

    /** Port d'écoute MySQL configuré pour l'environnement local. */
    private static final String PORT = "3308";

    /** Nom de la base de données utilisée par l'application. */
    private static final String DATABASE = "todoapp_db";

    /** Identifiant MySQL utilisé pour l'authentification JDBC. */
    private static final String USERNAME = "root";

    /**
     * Mot de passe MySQL utilisé pour l'authentification JDBC.
     */
    private static final String PASSWORD = "931206";

    /**
     * URL JDBC complète construite à partir des paramètres de connexion.
     * Paramètres utilisés :
     *   useSSL=false : désactive SSL en local.
     *   serverTimezone=UTC : fixe le fuseau serveur à UTC.
     *   allowPublicKeyRetrieval=true : autorise la récupération
     *   de clé publique selon la configuration MySQL.
     */
    private static final String URL =
            "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE
                    + "?useSSL=false"
                    + "&serverTimezone=UTC"
                    + "&allowPublicKeyRetrieval=true";

    // ─── Singleton ────────────────────────────────────────────────────────────

    /** Instance unique partagée de {@code DatabaseConnection}. */
    private static DatabaseConnection instance;

    /** Connexion JDBC active vers MySQL. */
    private Connection connection;

    // ─── Constructeur ─────────────────────────────────────────────────────────

    /**
     * Constructeur privé pour empêcher l'instanciation directe depuis l'extérieur.
     *
     * Ce constructeur ouvre immédiatement une connexion JDBC à MySQL.
     * Il est appelé uniquement lors de la création de l'instance Singleton.
     *
     * @throws SQLException si la connexion à MySQL échoue
     */
    private DatabaseConnection() throws SQLException {
        this.connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        System.out.println("[DB] Connected to MySQL database: " + DATABASE);
    }

    // ─── API publique ─────────────────────────────────────────────────────────

    /**
     * Retourne l'instance Singleton de {@code DatabaseConnection}.
     * Si aucune instance n'existe encore, ou si la connexion précédente a été
     * fermée, une nouvelle instance est créée automatiquement (initialisation lazy).
     * @return l'instance unique de {@code DatabaseConnection}
     * @throws SQLException si la création/réouverture de la connexion échoue
     */
    public static DatabaseConnection getInstance() throws SQLException {
        if (instance == null || instance.getConnection().isClosed()) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Retourne la connexion JDBC active pour exécuter des requêtes SQL.
     * Cette méthode expose l'objet {@link Connection} partagé par l'application.
     * @return la connexion MySQL active
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Ferme explicitement la connexion JDBC partagée, si elle est ouverte.
     * À appeler idéalement lors de l'arrêt de l'application pour libérer
     * proprement les ressources base de données.
     *
     * En cas d'erreur SQL durant la fermeture, le message est envoyé
     * à la sortie d'erreur standard.
     */
    public static void closeConnection() {
        try {
            if (instance != null && !instance.getConnection().isClosed()) {
                instance.getConnection().close();
                System.out.println("[DB] Connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error closing connection: " + e.getMessage());
        }
    }
}