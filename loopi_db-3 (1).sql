-- phpMyAdmin SQL Dump
-- version 4.1.14
-- http://www.phpmyadmin.net
--
-- Client :  127.0.0.1
-- Généré le :  Lun 02 Mars 2026 à 01:57
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
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=4 ;

--
-- Contenu de la table `collection`
--

INSERT INTO `collection` (`id_collection`, `title`, `material_type`, `image_collection`, `goal_amount`, `current_amount`, `unit`, `status`, `id_user`, `created_at`, `updated_at`) VALUES
(2, 'piece mario', 'Verre', 'C:\\Users\\MSI\\Pictures\\image_de_produit_loopi\\Mario.png', 123, 24.6, 'kg', 'active', 2, '2026-02-16 08:44:11', '2026-02-16 08:47:45'),
(3, 'PIECE', 'Métal', '', 1267, 0, 'kg', 'active', 2, '2026-02-16 08:45:22', '2026-02-16 08:45:22');

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
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

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
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=4 ;

--
-- Contenu de la table `donation`
--

INSERT INTO `donation` (`id_donation`, `id_user`, `id_collection`, `amount`, `donation_date`, `status`) VALUES
(1, 3, 1, 2.4, '2026-02-11 18:07:31', 'confirmé'),
(2, 3, 1, 5.8, '2026-02-11 20:20:38', 'confirmé'),
(3, 3, 2, 24.6, '2026-02-16 08:47:45', 'confirmé');

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
-- Structure de la table `favoris`
--

CREATE TABLE IF NOT EXISTS `favoris` (
  `id_favoris` int(11) NOT NULL AUTO_INCREMENT,
  `id_user` int(11) NOT NULL,
  `id_produit` int(11) NOT NULL,
  `date_ajout` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_favoris`),
  UNIQUE KEY `unique_favoris` (`id_user`,`id_produit`),
  KEY `id_produit` (`id_produit`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=33 ;

--
-- Contenu de la table `favoris`
--

INSERT INTO `favoris` (`id_favoris`, `id_user`, `id_produit`, `date_ajout`) VALUES
(20, 7, 5, '2026-02-19 16:38:01'),
(24, 3, 2, '2026-02-20 14:23:00'),
(25, 7, 2, '2026-02-20 14:25:47'),
(28, 7, 11, '2026-02-20 15:03:08'),
(29, 3, 5, '2026-02-20 17:58:55'),
(30, 3, 3, '2026-02-20 17:59:14'),
(31, 3, 11, '2026-02-28 05:17:23'),
(32, 3, 8, '2026-03-02 00:28:19');

-- --------------------------------------------------------

--
-- Structure de la table `feedback`
--

CREATE TABLE IF NOT EXISTS `feedback` (
  `id_feedback` int(11) NOT NULL AUTO_INCREMENT,
  `id_user` int(11) NOT NULL,
  `note` int(11) NOT NULL,
  `commentaire` varchar(255) NOT NULL,
  `date_commentaire` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `id_produit` int(11) NOT NULL,
  PRIMARY KEY (`id_feedback`),
  KEY `idx_user` (`id_user`),
  KEY `idx_date` (`date_commentaire`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=39 ;

--
-- Contenu de la table `feedback`
--

INSERT INTO `feedback` (`id_feedback`, `id_user`, `note`, `commentaire`, `date_commentaire`, `id_produit`) VALUES
(14, 4, 5, 'BEHYA BARCHA', '2026-02-15 21:45:05', 1),
(17, 3, 4, 'hlouwa <3 <3', '2026-02-15 21:48:05', 2),
(19, 3, 4, 'GOOD', '2026-02-17 15:40:48', 3),
(20, 3, 4, 'JOOOOOLIE', '2026-02-20 14:09:32', 11),
(21, 3, 4, 'GOODDDD', '2026-02-20 14:12:09', 3),
(22, 7, 3, 'BEHYA', '2026-02-20 14:19:43', 5),
(23, 3, 4, 'azeaze', '2026-02-20 14:55:03', 1),
(24, 3, 3, 'hlou', '2026-02-20 14:56:58', 11),
(25, 7, 5, 'WOOWWWWWWWWWW', '2026-02-20 15:03:17', 11),
(26, 3, 4, 'yaahah', '2026-02-20 15:21:05', 8),
(27, 3, 4, 'AZEAEZ', '2026-02-20 16:29:00', 8),
(28, 3, 3, 'joli', '2026-02-21 20:23:25', 11),
(29, 3, 5, 'tres tres joli et magnefique produit', '2026-02-21 20:57:48', 11),
(31, 3, 1, 'très mauvais produit', '2026-02-21 21:04:03', 11),
(33, 3, 5, 'tres bon produit', '2026-02-21 21:04:59', 11),
(34, 3, 5, 'super', '2026-02-21 21:06:24', 11),
(36, 3, 2, 'shit', '2026-02-28 06:08:08', 5),
(37, 3, 4, 'fuck', '2026-02-28 06:08:21', 11),
(38, 3, 3, 'what', '2026-03-02 00:52:25', 5);

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
-- Structure de la table `notifications`

---modification

CREATE TABLE notifications (
                               id INT AUTO_INCREMENT PRIMARY KEY,
                               id_user INT NOT NULL,
                               type VARCHAR(50) NOT NULL,
                               titre VARCHAR(200) NOT NULL,
                               message TEXT NOT NULL,
                               is_read BOOLEAN DEFAULT FALSE,
                               id_evenement INT,
                               id_participation INT,
                               nom_organisateur VARCHAR(200),
                               email_organisateur VARCHAR(200),
                               nom_participant VARCHAR(200),
                               email_participant VARCHAR(200),
                               nom_admin VARCHAR(200),
                               email_admin VARCHAR(200),
                               commentaire TEXT,
                               event_titre VARCHAR(200),
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               INDEX idx_user_read (id_user, is_read),
                               INDEX idx_created (created_at),
                               INDEX idx_type (type),
                               FOREIGN KEY (id_user) REFERENCES users(id) ON DELETE CASCADE,
                               FOREIGN KEY (id_evenement) REFERENCES evenement(id_evenement) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
--
/*
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
  KEY `id_evenement` (`id_evenement`),
  KEY `idx_user_read` (`id_user`,`is_read`),
  KEY `idx_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 AUTO_INCREMENT=1 ;
*/
-- --------------------------------------------------------

--
-- Structure de la table `notification_galerie`
--

CREATE TABLE IF NOT EXISTS `notification_galerie` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `id_organisateur` int(11) NOT NULL,
  `id_participant` int(11) NOT NULL,
  `id_produit` int(11) NOT NULL,
  `nom_participant` varchar(100) NOT NULL,
  `nom_produit` varchar(255) NOT NULL,
  `date_notification` datetime NOT NULL,
  `is_read` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `id_organisateur` (`id_organisateur`),
  KEY `id_participant` (`id_participant`),
  KEY `id_produit` (`id_produit`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

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
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=13 ;

--
-- Contenu de la table `produit`
--

INSERT INTO `produit` (`id_produit`, `nom_produit`, `description`, `image_produit`, `id_cat`, `id_user`, `created_at`, `updated_at`) VALUES
(1, 'chaise artisanale', 'Cette photographie met en avant une série de chaises artisanales au design unique et audacieux. La structure principale est constituée d''un cadre en métal noir minimaliste, mais c''est le revêtement qui attire immédiatement l''œil :', 'C:\\Users\\MSI\\Pictures\\image_de_produit_loopi\\chaise artisanale.png', 3, 2, '2026-02-08 13:08:00', '2026-02-10 14:39:25'),
(2, 'glass artwork', 'Much of the glass we throw out is not recycled. To reuse your old glass, all you need is access to a kiln and some glass bottles.', 'C:\\Users\\MSI\\Pictures\\image_de_produit_loopi\\glass artwork.png', 4, 2, '2026-02-08 13:08:00', '2026-02-10 14:40:15'),
(3, 'Mushrooms Sculpture', 'Hand Crafted Copper Mushrooms Sculpture Wood Base Recycled Art Unique', 'C:\\Users\\MSI\\Pictures\\image_de_produit_loopi\\Hand Crafted Copper Mushrooms.png', 1, 2, '2026-02-08 13:08:00', '2026-02-10 14:47:25'),
(5, '3D Mosaic', 'These 3D Wood, Metal, and Stone Mosaics have been Daniel Potampa''s tribute to natural beauty and a way to responsibly use these rare and precious materials. No artificial dyes or stains have been used, each piece remains it''s natural color.', 'C:\\Users\\MSI\\Pictures\\image_de_produit_loopi\\3D Mosaic.png', 2, 2, '2026-02-08 17:46:58', '2026-02-10 16:03:27'),
(8, 'Tin Fish', 'Look at the intricate detail of this tin fish. Each scale is made from recycled metal and carefully placed to replicate natural patterns.', 'C:\\Users\\MSI\\Pictures\\image_de_produit_loopi\\Tin Fish.png', 1, 2, '2026-02-08 20:18:19', '2026-02-10 14:43:10'),
(10, 'org THENI', 'theni organizateur yhabat', 'C:\\Users\\MSI\\Pictures\\Screenshots\\Capture d''écran 2026-01-21 141734.png', 4, 6, '2026-02-10 00:19:25', '2026-02-10 00:19:25'),
(11, 'esprit etudient', 'Cette œuvre d''art contemporaine se distingue par son originalité et sa qualité d''exécution. Les textures et les couleurs s''harmonisent pour créer une pièce unique qui captivera les amateurs d''art.', 'C:\\Users\\MSI\\IdeaProjects\\Loopi-Project\\src\\main\\resources\\images\\galerie_image\\Hippopotamus.png', 1, 2, '2026-02-16 08:32:27', '2026-02-28 23:36:48');

-- --------------------------------------------------------

--
-- Structure de la table `social_shares`
--

CREATE TABLE IF NOT EXISTS `social_shares` (
  `id_share` int(11) NOT NULL AUTO_INCREMENT,
  `id_produit` int(11) NOT NULL,
  `id_user` int(11) DEFAULT NULL,
  `network` varchar(50) NOT NULL,
  `share_date` datetime NOT NULL,
  PRIMARY KEY (`id_share`),
  KEY `id_user` (`id_user`),
  KEY `idx_produit` (`id_produit`),
  KEY `idx_network` (`network`),
  KEY `idx_date` (`share_date`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 AUTO_INCREMENT=5 ;

--
-- Contenu de la table `social_shares`
--

INSERT INTO `social_shares` (`id_share`, `id_produit`, `id_user`, `network`, `share_date`) VALUES
(1, 1, 1, 'facebook', '2026-02-21 01:36:40'),
(2, 1, 2, 'twitter', '2026-02-21 01:36:40'),
(3, 2, 1, 'whatsapp', '2026-02-21 01:36:40'),
(4, 3, 3, 'linkedin', '2026-02-21 01:36:40');

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
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=8 ;

--
-- Contenu de la table `users`
--

INSERT INTO `users` (`id`, `nom`, `prenom`, `email`, `password`, `photo`, `role`, `id_genre`, `created_at`, `updated_at`) VALUES
(1, 'Admin', 'System', 'admin@loopi.tn', 'admin123', 'default.jpg', 'admin', 1, '2026-02-08 13:08:00', '2026-03-02 00:30:28'),
(2, 'Organisateur', 'Eco', 'organisateur@loopi.tn', 'org123', 'default.jpg', 'organisateur', 1, '2026-02-08 13:08:00', '2026-03-02 00:29:36'),
(3, 'Participant', 'Test', 'participant@loopi.tn', 'part123', 'default.jpg', 'participant', 2, '2026-02-08 13:08:00', '2026-03-02 00:38:22'),
(4, 'Ben', 'Ali', 'ben.ali@email.com', 'ben123', 'default.jpg', 'participant', 1, '2026-02-08 13:08:00', '2026-02-08 13:08:00'),
(5, 'Dupont', 'Marie', 'marie.dupont@email.com', 'marie123', 'default.jpg', 'participant', 2, '2026-02-08 13:08:00', '2026-02-08 13:08:00'),
(6, 'Martin', 'Pierre', 'pierre@email.com', 'pierre123', 'default.jpg', 'organisateur', 1, '2026-02-08 13:08:00', '2026-02-10 00:16:58'),
(7, 'majdoub', 'yassine', 'yassinemajdoub@loopi.tn', 'yassine123', 'default.jpg', 'participant', 1, '2026-02-19 16:36:49', '2026-02-19 16:36:49');

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
-- Contraintes pour la table `content`
--
ALTER TABLE `content`
  ADD CONSTRAINT `content_ibfk_1` FOREIGN KEY (`id_commande`) REFERENCES `feedback` (`id_feedback`) ON DELETE CASCADE,
  ADD CONSTRAINT `content_ibfk_2` FOREIGN KEY (`id_produit`) REFERENCES `produit` (`id_produit`) ON DELETE CASCADE;

--
-- Contraintes pour la table `favoris`
--
ALTER TABLE `favoris`
  ADD CONSTRAINT `favoris_ibfk_1` FOREIGN KEY (`id_user`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `favoris_ibfk_2` FOREIGN KEY (`id_produit`) REFERENCES `produit` (`id_produit`);

--
-- Contraintes pour la table `feedback`
--
ALTER TABLE `feedback`
  ADD CONSTRAINT `feedback_ibfk_1` FOREIGN KEY (`id_user`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `notifications`
--
ALTER TABLE `notifications`
  ADD CONSTRAINT `notifications_ibfk_1` FOREIGN KEY (`id_user`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `notifications_ibfk_2` FOREIGN KEY (`id_evenement`) REFERENCES `evenement` (`id_evenement`) ON DELETE CASCADE;

--
-- Contraintes pour la table `notification_galerie`
--
ALTER TABLE `notification_galerie`
  ADD CONSTRAINT `notification_galerie_ibfk_1` FOREIGN KEY (`id_organisateur`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `notification_galerie_ibfk_2` FOREIGN KEY (`id_participant`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `notification_galerie_ibfk_3` FOREIGN KEY (`id_produit`) REFERENCES `produit` (`id_produit`) ON DELETE CASCADE;

--
-- Contraintes pour la table `social_shares`
--
ALTER TABLE `social_shares`
  ADD CONSTRAINT `social_shares_ibfk_1` FOREIGN KEY (`id_produit`) REFERENCES `produit` (`id_produit`) ON DELETE CASCADE,
  ADD CONSTRAINT `social_shares_ibfk_2` FOREIGN KEY (`id_user`) REFERENCES `users` (`id`) ON DELETE SET NULL;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
-- 1. AJOUT DES COLONNES À EVENEMENT
ALTER TABLE evenement ADD COLUMN statut_validation VARCHAR(20) DEFAULT 'en_attente';
ALTER TABLE evenement ADD COLUMN date_soumission TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE evenement ADD COLUMN date_validation TIMESTAMP NULL;
ALTER TABLE evenement ADD COLUMN commentaire_validation TEXT;
ALTER TABLE evenement ADD COLUMN latitude DOUBLE;
ALTER TABLE evenement ADD COLUMN longitude DOUBLE;

-- 2. AJOUT DES COLONNES À LA TABLE USERS (pour les badges)
/*ALTER TABLE users ADD COLUMN total_plastic DOUBLE DEFAULT 0;
ALTER TABLE users ADD COLUMN total_paper DOUBLE DEFAULT 0;
ALTER TABLE users ADD COLUMN total_glass DOUBLE DEFAULT 0;
ALTER TABLE users ADD COLUMN total_metal DOUBLE DEFAULT 0;
ALTER TABLE users ADD COLUMN total_cardboard DOUBLE DEFAULT 0;
ALTER TABLE users ADD COLUMN has_donated_first_time BOOLEAN DEFAULT FALSE;
ALTER TABLE users ADD COLUMN total_impact_collected DOUBLE DEFAULT 0;
ALTER TABLE users ADD COLUMN is_certified BOOLEAN DEFAULT FALSE;
*/

-- 3. AJOUT DES INDEX
ALTER TABLE participation ADD INDEX idx_statut (statut);
ALTER TABLE participation ADD INDEX idx_date_inscription (date_inscription);
ALTER TABLE evenement ADD INDEX idx_statut_validation (statut_validation);
ALTER TABLE evenement ADD INDEX idx_date_evenement (date_evenement);
ALTER TABLE notifications ADD INDEX idx_type (type);

-- 4. MISE À JOUR DES DONNÉES EXISTANTES
UPDATE evenement SET statut_validation = 'approuve' WHERE statut_validation IS NULL;
UPDATE evenement SET date_soumission = created_at WHERE date_soumission IS NULL;
