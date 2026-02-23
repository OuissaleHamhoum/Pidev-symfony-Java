-- phpMyAdmin SQL Dump
-- version 4.1.14
-- http://www.phpmyadmin.net
--
-- Client :  127.0.0.1
-- Généré le :  Mer 11 Février 2026 à 13:08
-- Version du serveur :  5.6.17
-- Version de PHP :  5.5.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Base de données :  `loopi_db`
--

-- --------------------------------------------------------

--
-- Structure de la table `category_produit`
--

CREATE TABLE IF NOT EXISTS `category_produit` (
  `id_cat` int(11) NOT NULL AUTO_INCREMENT,
  `nom_cat` varchar(100) NOT NULL,
  PRIMARY KEY (`id_cat`),
  UNIQUE KEY `nom_cat` (`nom_cat`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=5 ;

--
-- Contenu de la table `category_produit`
--

INSERT INTO `category_produit` (`id_cat`, `nom_cat`) VALUES
(2, 'Art mural '),
(4, 'Installations artistiques'),
(3, 'Mobilier artistique'),
(1, 'Objets décoratifs');

-- --------------------------------------------------------

--
-- Structure de la table `collection`
--

CREATE TABLE IF NOT EXISTS `collection` (
  `id_collection` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL,
  `material_type` varchar(255) NOT NULL,
  `image_collection` varchar(255) NOT NULL,
  `goal_amount` double NOT NULL,
  `current_amount` double DEFAULT '0',
  `unit` varchar(50) NOT NULL,
  `status` varchar(50) DEFAULT 'active',
  `id_user` int(11) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_collection`),
  KEY `idx_user` (`id_user`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Structure de la table `commande`
--

CREATE TABLE IF NOT EXISTS `commande` (
  `id_feedback` int(11) NOT NULL AUTO_INCREMENT,
  `id_user` int(11) NOT NULL,
  `note` int(11) NOT NULL,
  `commentaire` varchar(255) NOT NULL,
  `date_commentaire` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_feedback`),
  KEY `idx_user` (`id_user`),
  KEY `idx_date` (`date_commentaire`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=3 ;

--
-- Contenu de la table `commande`
--

INSERT INTO `commande` (`id_feedback`, `id_user`, `note`, `commentaire`, `date_commentaire`) VALUES
(1, 3, 0, '', '2026-02-08 13:08:00'),
(2, 4, 0, '', '2026-02-08 13:08:00');

-- --------------------------------------------------------

--
-- Structure de la table `content`
--

CREATE TABLE IF NOT EXISTS `content` (
  `id_content` int(11) NOT NULL AUTO_INCREMENT,
  `id_commande` int(11) NOT NULL,
  `id_produit` int(11) NOT NULL,
  `quantite` int(11) NOT NULL,
  `prix_unitaire` decimal(10,2) NOT NULL,
  PRIMARY KEY (`id_content`),
  UNIQUE KEY `unique_commande_produit` (`id_commande`,`id_produit`),
  KEY `id_produit` (`id_produit`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=4 ;

--
-- Contenu de la table `content`
--

INSERT INTO `content` (`id_content`, `id_commande`, `id_produit`, `quantite`, `prix_unitaire`) VALUES
(1, 1, 1, 1, '120.50'),
(2, 1, 3, 1, '46.00'),
(3, 2, 2, 1, '45.99');

-- --------------------------------------------------------

--
-- Structure de la table `coupon`
--

CREATE TABLE IF NOT EXISTS `coupon` (
  `id_coupon` int(11) NOT NULL AUTO_INCREMENT,
  `code` varchar(50) NOT NULL,
  `discount_percent` decimal(5,2) NOT NULL,
  `donation_date` date DEFAULT NULL,
  `used` tinyint(1) DEFAULT '0',
  `id_user` int(11) DEFAULT NULL,
  `id_donation` int(11) DEFAULT NULL,
  `expiration_date` date DEFAULT NULL,
  `min_amount` decimal(10,2) DEFAULT '0.00',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_coupon`),
  UNIQUE KEY `code` (`code`),
  KEY `id_user` (`id_user`),
  KEY `id_donation` (`id_donation`),
  KEY `idx_code` (`code`),
  KEY `idx_used` (`used`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=3 ;

--
-- Contenu de la table `coupon`
--

INSERT INTO `coupon` (`id_coupon`, `code`, `discount_percent`, `donation_date`, `used`, `id_user`, `id_donation`, `expiration_date`, `min_amount`, `created_at`) VALUES
(1, 'ECO10', '10.00', '2026-02-08', 0, 3, 1, '2026-03-10', '0.00', '2026-02-08 13:08:00'),
(2, 'GREEN15', '15.00', '2026-02-08', 0, 4, 2, '2026-04-09', '0.00', '2026-02-08 13:08:00');

-- --------------------------------------------------------

--
-- Structure de la table `donation`
--

CREATE TABLE IF NOT EXISTS `donation` (
  `id_donation` int(11) NOT NULL AUTO_INCREMENT,
  `id_user` int(11) NOT NULL,
  `id_collection` int(11) NOT NULL,
  `amount` double NOT NULL,
  `donation_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `status` enum('en_attente','confirmé','annulé') NOT NULL DEFAULT 'en_attente',
  PRIMARY KEY (`id_donation`),
  KEY `fk_donation_user` (`id_user`),
  KEY `fk_donation_collection` (`id_collection`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Structure de la table `evenement`
--

CREATE TABLE IF NOT EXISTS `evenement` (
  `id_evenement` int(11) NOT NULL AUTO_INCREMENT,
  `titre` varchar(200) NOT NULL,
  `description` text,
  `date_evenement` datetime NOT NULL,
  `lieu` varchar(200) DEFAULT NULL,
  `id_organisateur` int(11) NOT NULL,
  `capacite_max` int(11) DEFAULT NULL,
  `image_evenement` varchar(255) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_evenement`),
  KEY `id_organisateur` (`id_organisateur`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=3 ;

--
-- Contenu de la table `evenement`
--

INSERT INTO `evenement` (`id_evenement`, `titre`, `description`, `date_evenement`, `lieu`, `id_organisateur`, `capacite_max`, `image_evenement`, `created_at`) VALUES
(1, 'Nettoyage de plage', 'Journée de nettoyage', '2024-06-15 09:00:00', 'Plage Sousse', 2, 50, NULL, '2026-02-08 13:08:00'),
(2, 'Atelier recyclage', 'Apprenez à recycler', '2024-06-20 14:00:00', 'Centre Tunis', 2, 30, NULL, '2026-02-08 13:08:00');

-- --------------------------------------------------------

--
-- Structure de la table `genre`
--

CREATE TABLE IF NOT EXISTS `genre` (
  `id_genre` int(11) NOT NULL AUTO_INCREMENT,
  `sexe` varchar(50) NOT NULL,
  PRIMARY KEY (`id_genre`),
  UNIQUE KEY `sexe` (`sexe`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=4 ;

--
-- Contenu de la table `genre`
--

INSERT INTO `genre` (`id_genre`, `sexe`) VALUES
(2, 'Femme'),
(1, 'Homme'),
(3, 'Non spécifié');

-- --------------------------------------------------------

--
-- Structure de la table `participation`
--

CREATE TABLE IF NOT EXISTS `participation` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `id_user` int(11) NOT NULL,
  `id_evenement` int(11) NOT NULL,
  `contact` varchar(100) NOT NULL,
  `age` int(11) DEFAULT NULL,
  `date_inscription` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `statut` enum('inscrit','present','absent') DEFAULT 'inscrit',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_participation` (`id_user`,`id_evenement`),
  KEY `id_evenement` (`id_evenement`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=4 ;

--
-- Contenu de la table `participation`
--

INSERT INTO `participation` (`id`, `id_user`, `id_evenement`, `contact`, `age`, `date_inscription`, `statut`) VALUES
(1, 3, 1, 'participant@loopi.tn', 28, '2026-02-08 13:08:00', 'inscrit'),
(2, 4, 1, 'ben.ali@email.com', 35, '2026-02-08 13:08:00', 'inscrit'),
(3, 5, 2, 'marie.dupont@email.com', 32, '2026-02-08 13:08:00', 'inscrit');

-- --------------------------------------------------------

--
-- Structure de la table `produit`
--

CREATE TABLE IF NOT EXISTS `produit` (
  `id_produit` int(11) NOT NULL AUTO_INCREMENT,
  `nom_produit` varchar(200) NOT NULL,
  `description` text,
  `image_produit` varchar(255) DEFAULT NULL,
  `id_cat` int(11) NOT NULL,
  `id_user` int(11) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_produit`),
  KEY `id_user` (`id_user`),
  KEY `idx_categorie` (`id_cat`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=11 ;

--
-- Contenu de la table `produit`
--

INSERT INTO `produit` (`id_produit`, `nom_produit`, `description`, `image_produit`, `id_cat`, `id_user`, `created_at`, `updated_at`) VALUES
(1, 'chaise artisanale', 'Cette photographie met en avant une série de chaises artisanales au design unique et audacieux. La structure principale est constituée d''un cadre en métal noir minimaliste, mais c''est le revêtement qui attire immédiatement l''œil :', 'C:\\Users\\MSI\\Pictures\\image_de_produit_loopi\\chaise artisanale.png', 3, 2, '2026-02-08 13:08:00', '2026-02-10 14:39:25'),
(2, 'glass artwork', 'Much of the glass we throw out is not recycled. To reuse your old glass, all you need is access to a kiln and some glass bottles.', 'C:\\Users\\MSI\\Pictures\\image_de_produit_loopi\\glass artwork.png', 4, 2, '2026-02-08 13:08:00', '2026-02-10 14:40:15'),
(3, 'Mushrooms Sculpture', 'Hand Crafted Copper Mushrooms Sculpture Wood Base Recycled Art Unique', 'C:\\Users\\MSI\\Pictures\\image_de_produit_loopi\\Hand Crafted Copper Mushrooms.png', 1, 2, '2026-02-08 13:08:00', '2026-02-10 14:47:25'),
(5, '3D Mosaic', 'These 3D Wood, Metal, and Stone Mosaics have been Daniel Potampa''s tribute to natural beauty and a way to responsibly use these rare and precious materials. No artificial dyes or stains have been used, each piece remains it''s natural color.', 'C:\\Users\\MSI\\Pictures\\image_de_produit_loopi\\3D Mosaic.png', 2, 2, '2026-02-08 17:46:58', '2026-02-10 16:03:27'),
(8, 'Tin Fish', 'Look at the intricate detail of this tin fish. Each scale is made from recycled metal and carefully placed to replicate natural patterns.', 'C:\\Users\\MSI\\Pictures\\image_de_produit_loopi\\Tin Fish.png', 1, 2, '2026-02-08 20:18:19', '2026-02-10 14:43:10'),
(9, 'side table', 'Metal end or side table made with a twist of art can be used for steampunk, industrial or artsy flair, a great conversation item will definitely be one of a kind. This one has been sold tell me what you are wanting or let me design one for you.', 'C:\\Users\\MSI\\Pictures\\image_de_produit_loopi\\side table.jpg', 3, 2, '2026-02-10 00:14:42', '2026-02-10 14:44:28'),
(10, 'org THENI', 'theni organizateur yhabat', 'C:\\Users\\MSI\\Pictures\\Screenshots\\Capture d''écran 2026-01-21 141734.png', 4, 6, '2026-02-10 00:19:25', '2026-02-10 00:19:25');

-- --------------------------------------------------------

--
-- Structure de la table `users`
--

CREATE TABLE IF NOT EXISTS `users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `nom` varchar(100) NOT NULL,
  `prenom` varchar(100) NOT NULL,
  `email` varchar(100) NOT NULL,
  `password` varchar(100) NOT NULL,
  `photo` varchar(255) DEFAULT 'default.jpg',
  `role` enum('admin','organisateur','participant') DEFAULT 'participant',
  `id_genre` int(11) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`),
  KEY `id_genre` (`id_genre`),
  KEY `idx_email` (`email`),
  KEY `idx_role` (`role`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=7 ;

--
-- Contenu de la table `users`
--

INSERT INTO `users` (`id`, `nom`, `prenom`, `email`, `password`, `photo`, `role`, `id_genre`, `created_at`, `updated_at`) VALUES
(1, 'Admin', 'System', 'admin@loopi.tn', 'admin123', 'default.jpg', 'admin', 1, '2026-02-08 13:08:00', '2026-02-08 13:08:00'),
(2, 'Organisateur', 'Eco', 'organisateur@loopi.tn', 'org123', 'default.jpg', 'organisateur', 1, '2026-02-08 13:08:00', '2026-02-08 13:08:00'),
(3, 'Participant', 'Test', 'participant@loopi.tn', 'part123', 'default.jpg', 'participant', 2, '2026-02-08 13:08:00', '2026-02-08 13:08:00'),
(4, 'Ben', 'Ali', 'ben.ali@email.com', 'ben123', 'default.jpg', 'participant', 1, '2026-02-08 13:08:00', '2026-02-08 13:08:00'),
(5, 'Dupont', 'Marie', 'marie.dupont@email.com', 'marie123', 'default.jpg', 'participant', 2, '2026-02-08 13:08:00', '2026-02-08 13:08:00'),
(6, 'Martin', 'Pierre', 'pierre@email.com', 'pierre123', 'default.jpg', 'organisateur', 1, '2026-02-08 13:08:00', '2026-02-10 00:16:58');

-- --------------------------------------------------------

--
-- Doublure de structure pour la vue `v_users_passwords`
--
CREATE TABLE IF NOT EXISTS `v_users_passwords` (
`id` int(11)
,`nom` varchar(100)
,`prenom` varchar(100)
,`email` varchar(100)
,`password` varchar(100)
,`role` enum('admin','organisateur','participant')
,`photo` varchar(255)
,`created_at` timestamp
);
-- --------------------------------------------------------

--
-- Structure de la vue `v_users_passwords`
--
DROP TABLE IF EXISTS `v_users_passwords`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `v_users_passwords` AS select `users`.`id` AS `id`,`users`.`nom` AS `nom`,`users`.`prenom` AS `prenom`,`users`.`email` AS `email`,`users`.`password` AS `password`,`users`.`role` AS `role`,`users`.`photo` AS `photo`,`users`.`created_at` AS `created_at` from `users`;

--
-- Contraintes pour les tables exportées
--

--
-- Contraintes pour la table `collection`
--
ALTER TABLE `collection`
  ADD CONSTRAINT `collection_ibfk_1` FOREIGN KEY (`id_user`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `commande`
--
ALTER TABLE `commande`
  ADD CONSTRAINT `commande_ibfk_1` FOREIGN KEY (`id_user`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `content`
--
ALTER TABLE `content`
  ADD CONSTRAINT `content_ibfk_1` FOREIGN KEY (`id_commande`) REFERENCES `commande` (`id_feedback`) ON DELETE CASCADE,
  ADD CONSTRAINT `content_ibfk_2` FOREIGN KEY (`id_produit`) REFERENCES `produit` (`id_produit`) ON DELETE CASCADE;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
-- Ajouter cette table à votre base de données loopi_db

CREATE TABLE IF NOT EXISTS `notifications` (
                                               `id` int(11) NOT NULL AUTO_INCREMENT,
    `id_user` int(11) NOT NULL,
    `type` varchar(50) NOT NULL,
    `titre` varchar(200) NOT NULL,
    `message` text NOT NULL,
    `is_read` tinyint(1) DEFAULT '0',
    `id_evenement` int(11) DEFAULT NULL,
    `id_participation` int(11) DEFAULT NULL,
    `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `id_user` (`id_user`),
    KEY `id_evenement` (`id_evenement`),
    KEY `idx_user_read` (`id_user`,`is_read`),
    KEY `idx_created` (`created_at`),
    CONSTRAINT `notifications_ibfk_1` FOREIGN KEY (`id_user`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `notifications_ibfk_2` FOREIGN KEY (`id_evenement`) REFERENCES `evenement` (`id_evenement`) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ajouter
-- Modifier la table evenement pour ajouter le statut de validation
ALTER TABLE `evenement`
    ADD COLUMN `statut_validation` ENUM('en_attente', 'approuve', 'refuse') DEFAULT 'en_attente',
ADD COLUMN `date_soumission` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN `date_validation` timestamp NULL DEFAULT NULL,
ADD COLUMN `commentaire_validation` text DEFAULT NULL,
ADD INDEX `idx_statut_validation` (`statut_validation`),
ADD INDEX `idx_date_soumission` (`date_soumission`);

-- Mettre à jour les événements existants comme approuvés (optionnel)
UPDATE `evenement` SET `statut_validation` = 'approuve', `date_validation` = CURRENT_TIMESTAMP WHERE `statut_validation` IS NULL;

--
-- Ajouter des colonnes pour les coordonnées géographiques
ALTER TABLE `evenement`
    ADD COLUMN `latitude` DOUBLE NULL,
ADD COLUMN `longitude` DOUBLE NULL,
ADD INDEX `idx_coordinates` (`latitude`, `longitude`);

-- Mettre à jour avec des coordonnées exemple
UPDATE `evenement` SET
                       `latitude` = 36.8065 + (RAND() * 2 - 1),
                       `longitude` = 10.1815 + (RAND() * 2 - 1)
WHERE `latitude` IS NULL;