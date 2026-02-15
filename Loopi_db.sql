-- phpMyAdmin SQL Dump
-- version 4.1.14
-- http://www.phpmyadmin.net
--
-- Host: 127.0.0.1
-- Generation Time: Feb 08, 2026 at 03:20 PM
-- Server version: 5.6.17
-- PHP Version: 5.5.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `loopi_db`
--

-- --------------------------------------------------------

--
-- Table structure for table `category_produit`
--

CREATE TABLE IF NOT EXISTS `category_produit` (
  `id_cat` int(11) NOT NULL AUTO_INCREMENT,
  `nom_cat` varchar(100) NOT NULL,
  `description` text,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_cat`),
  UNIQUE KEY `nom_cat` (`nom_cat`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=5 ;

--
-- Dumping data for table `category_produit`
--

INSERT INTO `category_produit` (`id_cat`, `nom_cat`, `description`, `created_at`) VALUES
(1, 'Mobilier recyclé', 'Meubles fabriqués à partir de matériaux recyclés', '2026-02-08 13:08:00'),
(2, 'Décorations écologiques', 'Objets décoratifs respectueux de l''environnement', '2026-02-08 13:08:00'),
(3, 'Accessoires durables', 'Accessoires mode et utilitaires écologiques', '2026-02-08 13:08:00'),
(4, 'Jouets éducatifs', 'Jouets fabriqués à partir de matériaux recyclés', '2026-02-08 13:08:00');

-- --------------------------------------------------------

--
-- Table structure for table `collection`
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
-- Table structure for table `commande`
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
-- Dumping data for table `commande`
--

INSERT INTO `commande` (`id_feedback`, `id_user`, `note`, `commentaire`, `date_commentaire`) VALUES
(1, 3, 0, '', '2026-02-08 13:08:00'),
(2, 4, 0, '', '2026-02-08 13:08:00');

-- --------------------------------------------------------

--
-- Table structure for table `content`
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
-- Dumping data for table `content`
--

INSERT INTO `content` (`id_content`, `id_commande`, `id_produit`, `quantite`, `prix_unitaire`) VALUES
(1, 1, 1, 1, '120.50'),
(2, 1, 3, 1, '46.00'),
(3, 2, 2, 1, '45.99');

-- --------------------------------------------------------

--
-- Table structure for table `coupon`
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
-- Dumping data for table `coupon`
--

INSERT INTO `coupon` (`id_coupon`, `code`, `discount_percent`, `donation_date`, `used`, `id_user`, `id_donation`, `expiration_date`, `min_amount`, `created_at`) VALUES
(1, 'ECO10', '10.00', '2026-02-08', 0, 3, 1, '2026-03-10', '0.00', '2026-02-08 13:08:00'),
(2, 'GREEN15', '15.00', '2026-02-08', 0, 4, 2, '2026-04-09', '0.00', '2026-02-08 13:08:00');

-- --------------------------------------------------------

--
-- Table structure for table `donation`
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
-- Table structure for table `evenement`
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
-- Dumping data for table `evenement`
--

INSERT INTO `evenement` (`id_evenement`, `titre`, `description`, `date_evenement`, `lieu`, `id_organisateur`, `capacite_max`, `image_evenement`, `created_at`) VALUES
(1, 'Nettoyage de plage', 'Journée de nettoyage', '2024-06-15 09:00:00', 'Plage Sousse', 2, 50, NULL, '2026-02-08 13:08:00'),
(2, 'Atelier recyclage', 'Apprenez à recycler', '2024-06-20 14:00:00', 'Centre Tunis', 2, 30, NULL, '2026-02-08 13:08:00');

-- --------------------------------------------------------

--
-- Table structure for table `genre`
--

CREATE TABLE IF NOT EXISTS `genre` (
  `id_genre` int(11) NOT NULL AUTO_INCREMENT,
  `sexe` varchar(50) NOT NULL,
  PRIMARY KEY (`id_genre`),
  UNIQUE KEY `sexe` (`sexe`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=4 ;

--
-- Dumping data for table `genre`
--

INSERT INTO `genre` (`id_genre`, `sexe`) VALUES
(2, 'Femme'),
(1, 'Homme'),
(3, 'Non spécifié');

-- --------------------------------------------------------

--
-- Table structure for table `participation`
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
-- Dumping data for table `participation`
--

INSERT INTO `participation` (`id`, `id_user`, `id_evenement`, `contact`, `age`, `date_inscription`, `statut`) VALUES
(1, 3, 1, 'participant@loopi.tn', 28, '2026-02-08 13:08:00', 'inscrit'),
(2, 4, 1, 'ben.ali@email.com', 35, '2026-02-08 13:08:00', 'inscrit'),
(3, 5, 2, 'marie.dupont@email.com', 32, '2026-02-08 13:08:00', 'inscrit');

-- --------------------------------------------------------

--
-- Table structure for table `produit`
--

CREATE TABLE IF NOT EXISTS `produit` (
  `id_produit` int(11) NOT NULL AUTO_INCREMENT,
  `nom_produit` varchar(200) NOT NULL,
  `description` text,
  `prix` decimal(10,2) NOT NULL,
  `image_produit` varchar(255) DEFAULT NULL,
  `id_cat` int(11) NOT NULL,
  `id_user` int(11) DEFAULT NULL,
  `is_active` tinyint(1) DEFAULT '1',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_produit`),
  KEY `id_user` (`id_user`),
  KEY `idx_categorie` (`id_cat`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=5 ;

--
-- Dumping data for table `produit`
--

INSERT INTO `produit` (`id_produit`, `nom_produit`, `description`, `prix`, `image_produit`, `id_cat`, `id_user`, `is_active`, `created_at`, `updated_at`) VALUES
(1, 'Table basse en palette', 'Table basse design en palettes recyclées', '120.50', 'table.jpg', 1, 2, 1, '2026-02-08 13:08:00', '2026-02-08 13:08:00'),
(2, 'Lampes en bouteilles', 'Lampes créatives en bouteilles recyclées', '45.99', 'lampe.jpg', 2, 2, 1, '2026-02-08 13:08:00', '2026-02-08 13:08:00'),
(3, 'Sac en pneus recyclés', 'Sac à main en pneus recyclés', '65.00', 'sac.jpg', 3, 2, 1, '2026-02-08 13:08:00', '2026-02-08 13:08:00'),
(4, 'Jouets en bois', 'Jouets éducatifs en bois recyclé', '22.99', 'jouet.jpg', 4, 2, 1, '2026-02-08 13:08:00', '2026-02-08 13:08:00');

-- --------------------------------------------------------

--
-- Table structure for table `users`
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
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `nom`, `prenom`, `email`, `password`, `photo`, `role`, `id_genre`, `created_at`, `updated_at`) VALUES
(1, 'Admin', 'System', 'admin@loopi.tn', 'admin123', 'default.jpg', 'admin', 1, '2026-02-08 13:08:00', '2026-02-08 13:08:00'),
(2, 'Organisateur', 'Eco', 'organisateur@loopi.tn', 'org123', 'default.jpg', 'organisateur', 1, '2026-02-08 13:08:00', '2026-02-08 13:08:00'),
(3, 'Participant', 'Test', 'participant@loopi.tn', 'part123', 'default.jpg', 'participant', 2, '2026-02-08 13:08:00', '2026-02-08 13:08:00'),
(4, 'Ben', 'Ali', 'ben.ali@email.com', 'ben123', 'default.jpg', 'participant', 1, '2026-02-08 13:08:00', '2026-02-08 13:08:00'),
(5, 'Dupont', 'Marie', 'marie.dupont@email.com', 'marie123', 'default.jpg', 'participant', 2, '2026-02-08 13:08:00', '2026-02-08 13:08:00'),
(6, 'Martin', 'Pierre', 'pierre@email.com', 'pierre123', 'default.jpg', 'participant', 1, '2026-02-08 13:08:00', '2026-02-08 13:08:00');

-- --------------------------------------------------------

--
-- Stand-in structure for view `v_users_passwords`
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
-- Structure for view `v_users_passwords`
--
DROP TABLE IF EXISTS `v_users_passwords`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `v_users_passwords` AS select `users`.`id` AS `id`,`users`.`nom` AS `nom`,`users`.`prenom` AS `prenom`,`users`.`email` AS `email`,`users`.`password` AS `password`,`users`.`role` AS `role`,`users`.`photo` AS `photo`,`users`.`created_at` AS `created_at` from `users`;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `collection`
--
ALTER TABLE `collection`
  ADD CONSTRAINT `collection_ibfk_1` FOREIGN KEY (`id_user`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `commande`
--
ALTER TABLE `commande`
  ADD CONSTRAINT `commande_ibfk_1` FOREIGN KEY (`id_user`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `content`
--
ALTER TABLE `content`
  ADD CONSTRAINT `content_ibfk_1` FOREIGN KEY (`id_commande`) REFERENCES `commande` (`id_feedback`) ON DELETE CASCADE,
  ADD CONSTRAINT `content_ibfk_2` FOREIGN KEY (`id_produit`) REFERENCES `produit` (`id_produit`) ON DELETE CASCADE;

--
-- Constraints for table `coupon`
--
ALTER TABLE `coupon`
  ADD CONSTRAINT `coupon_ibfk_1` FOREIGN KEY (`id_user`) REFERENCES `users` (`id`) ON DELETE SET NULL,
  ADD CONSTRAINT `coupon_ibfk_2` FOREIGN KEY (`id_donation`) REFERENCES `donation` (`id_donation`) ON DELETE SET NULL;

--
-- Constraints for table `donation`
--
ALTER TABLE `donation`
  ADD CONSTRAINT `fk_donation_user` FOREIGN KEY (`id_user`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_donation_collection` FOREIGN KEY (`id_collection`) REFERENCES `collection` (`id_collection`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `evenement`
--
ALTER TABLE `evenement`
  ADD CONSTRAINT `evenement_ibfk_1` FOREIGN KEY (`id_organisateur`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `participation`
--
ALTER TABLE `participation`
  ADD CONSTRAINT `participation_ibfk_1` FOREIGN KEY (`id_user`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `participation_ibfk_2` FOREIGN KEY (`id_evenement`) REFERENCES `evenement` (`id_evenement`) ON DELETE CASCADE;

--
-- Constraints for table `produit`
--
ALTER TABLE `produit`
  ADD CONSTRAINT `produit_ibfk_1` FOREIGN KEY (`id_cat`) REFERENCES `category_produit` (`id_cat`) ON DELETE CASCADE,
  ADD CONSTRAINT `produit_ibfk_2` FOREIGN KEY (`id_user`) REFERENCES `users` (`id`) ON DELETE SET NULL;

--
-- Constraints for table `users`
--
ALTER TABLE `users`
  ADD CONSTRAINT `users_ibfk_1` FOREIGN KEY (`id_genre`) REFERENCES `genre` (`id_genre`) ON DELETE SET NULL;

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