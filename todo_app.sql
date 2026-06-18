-- ============================================================
--  APPLICATION DE GESTION DE TÂCHES — SCRIPT DE BASE DE DONNÉES
--  Base de données : todoapp_db
--  Auteur : Soufiane bouhasni
--  Date : 2026
--
--  Description:
--  Ce script SQL configure le schéma de base de données pour une
--  application JavaFX de gestion de liste de tâches. Il inclut des
--  tables pour les tâches et les utilisateurs, avec support pour
--  l'authentification utilisateur et le contrôle d'accès basé sur
--  les rôles (RBAC - Role-Based Access Control).
--
--  Fonctionnalités principales:
--    • Gestion multi-utilisateur (chaque tâche appartient à un utilisateur)
--    • Authentification utilisateur avec stockage sécurisé des mots de passe
--    • Système de rôles (USER : utilisateur standard, ADMIN : administrateur)
--    • Support complet des opérations CRUD (Créer, Lire, Mettre à jour, Supprimer)
--    • Intégrité référentielle avec cascade de suppression (ON DELETE CASCADE)
--
--  Tables créées:
--    1. tasks : Stocke toutes les tâches avec leurs propriétés
--    2. users : Stocke les comptes utilisateur et les informations d'authentification
-- ============================================================

CREATE DATABASE IF NOT EXISTS todoapp_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE todoapp_db;

DROP TABLE IF EXISTS tasks;

CREATE TABLE tasks (
       id          INT             NOT NULL AUTO_INCREMENT,
       title       VARCHAR(100)    NOT NULL,
       description TEXT            NULL,
       priority    ENUM('LOW', 'MEDIUM', 'HIGH')
       NOT NULL DEFAULT 'MEDIUM',

       status      ENUM('PENDING', 'IN_PROGRESS', 'COMPLETED')
       NOT NULL DEFAULT 'PENDING',
       due_date    DATE            NULL,
       category    VARCHAR(50)     NULL DEFAULT 'General',
       created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
       updated_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
           ON UPDATE CURRENT_TIMESTAMP,
       CONSTRAINT pk_tasks PRIMARY KEY (id)

) ENGINE=InnoDB;

INSERT INTO tasks (title, description, priority, status, due_date, category)
VALUES
    ('Finish JavaFX project',  'Complete the To-Do List mini project for university', 'HIGH',   'IN_PROGRESS', '2026-06-20', 'School'),
    ('Buy groceries',          'Milk, bread, eggs, and vegetables',                   'LOW',    'PENDING',     '2026-06-08', 'Personal'),
    ('Read Java book',         'Read chapters 10 to 15 on collections',               'MEDIUM', 'PENDING',     '2026-06-15', 'School'),
    ('Team meeting',           'Weekly sync with project group at 10am',              'HIGH',   'COMPLETED',   '2026-06-05', 'Work'),
    ('Exercise',               'Go for a 30-minute run in the park',                  'LOW',    'PENDING',     NULL,         'Personal');


CREATE TABLE IF NOT EXISTS users (
     id         INT          NOT NULL AUTO_INCREMENT,
     username   VARCHAR(50)  NOT NULL UNIQUE,
     password   VARCHAR(255) NOT NULL,
     full_name  VARCHAR(100),
     created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
     PRIMARY KEY (id)
);


-- ============================================================
--  MODIFICATIONS DE SCHÉMA POUR L'AUTHENTIFICATION & AUTORISATION
--
--  NOTE: Les modifications suivantes au schéma de la base de données
--  ont été ajoutées après la création initiale des tables. Ces modifications
--  implémentent un système d'authentification utilisateur et de contrôle
--  d'accès basé sur les rôles (RBAC - Role-Based Access Control).
--
--  Raison: Après le développement initial de l'application, la décision
--  a été prise d'ajouter :
--    1. Support multi-utilisateur (chaque tâche appartient à un utilisateur)
--    2. Authentification utilisateur (connexion/enregistrement)
--    3. Système de rôles (USER vs ADMIN pour les privilèges d'accès)
--
--  Ces modifications permettent à l'application de supporter plusieurs
--  utilisateurs avec leurs propres listes de tâches et niveaux d'accès distincts.
-- ============================================================

ALTER TABLE tasks
    ADD COLUMN user_id INT NOT NULL DEFAULT 1 AFTER category;


ALTER TABLE tasks
    ADD CONSTRAINT fk_tasks_user
        FOREIGN KEY (user_id) REFERENCES users(id)
            ON DELETE CASCADE;

ALTER TABLE users
    ADD COLUMN role ENUM('USER', 'ADMIN') NOT NULL DEFAULT 'USER' AFTER full_name;
UPDATE users SET role = 'ADMIN' WHERE id = 1;