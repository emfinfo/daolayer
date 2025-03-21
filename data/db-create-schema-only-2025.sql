CREATE DATABASE  IF NOT EXISTS `parlement` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `parlement`;
-- MySQL dump 10.13  Distrib 8.0.40, for macos14 (x86_64)
--
-- Host: localhost    Database: strittjc_parlement
-- ------------------------------------------------------
-- Server version	5.5.5-10.3.35-MariaDB-1:10.3.35+maria~stretch-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `t_activite`
--

DROP TABLE IF EXISTS `t_activite`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_activite` (
  `pkActivite` int(11) NOT NULL AUTO_INCREMENT,
  `dateEntree` date DEFAULT NULL,
  `dateSortie` date DEFAULT NULL,
  `fkConseiller` int(11) NOT NULL,
  `fkConseil` int(11) NOT NULL,
  `fkGroupe` int(11) NOT NULL,
  PRIMARY KEY (`pkActivite`),
  KEY `activite_conseiller_idx` (`fkConseiller`),
  KEY `activite_conseil_idx` (`fkConseil`),
  KEY `activite_groupe_idx` (`fkGroupe`),
  CONSTRAINT `conseil_constraint` FOREIGN KEY (`fkConseil`) REFERENCES `t_conseil` (`pkConseil`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `conseiller_constraint` FOREIGN KEY (`fkConseiller`) REFERENCES `t_conseiller` (`pkConseiller`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `groupe_constraint` FOREIGN KEY (`fkGroupe`) REFERENCES `t_groupe` (`pkGroupe`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=5691 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_canton`
--

DROP TABLE IF EXISTS `t_canton`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_canton` (
  `pkCanton` int(11) NOT NULL AUTO_INCREMENT,
  `abrev` varchar(2) NOT NULL,
  `nom` varchar(30) NOT NULL,
  PRIMARY KEY (`pkCanton`),
  UNIQUE KEY `abrev_UNIQUE` (`abrev`)
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_conseil`
--

DROP TABLE IF EXISTS `t_conseil`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_conseil` (
  `pkConseil` int(11) NOT NULL AUTO_INCREMENT,
  `abrev` varchar(2) NOT NULL,
  `nom` varchar(30) NOT NULL,
  PRIMARY KEY (`pkConseil`),
  UNIQUE KEY `abrev_UNIQUE` (`abrev`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_conseiller`
--

DROP TABLE IF EXISTS `t_conseiller`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_conseiller` (
  `pkConseiller` int(11) NOT NULL AUTO_INCREMENT,
  `actif` int(1) NOT NULL,
  `dateNaissance` date DEFAULT NULL,
  `dateDeces` date DEFAULT NULL,
  `nom` varchar(30) NOT NULL,
  `prenom` varchar(30) NOT NULL,
  `sexe` varchar(1) NOT NULL,
  `citoyennete` varchar(100) DEFAULT NULL,
  `lieuNaissance` varchar(60) DEFAULT NULL,
  `cantonNaissance` varchar(30) DEFAULT NULL,
  `mandats` longtext DEFAULT NULL,
  `fkEtatCivil` int(11) NOT NULL,
  `fkCanton` int(11) NOT NULL,
  `fkParti` int(11) NOT NULL,
  PRIMARY KEY (`pkConseiller`),
  KEY `conseiller_parti_idx` (`fkParti`),
  KEY `conseiller_canton_idx` (`fkCanton`),
  KEY `conseiller_etat_civil_idx` (`fkEtatCivil`),
  CONSTRAINT `canton_constraint` FOREIGN KEY (`fkCanton`) REFERENCES `t_canton` (`pkCanton`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_t_conseiller_t_etat_civil1` FOREIGN KEY (`fkEtatCivil`) REFERENCES `t_etat_civil` (`pkEtatCivil`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `parti_constraint` FOREIGN KEY (`fkParti`) REFERENCES `t_parti` (`pkParti`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=3644 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_etat_civil`
--

DROP TABLE IF EXISTS `t_etat_civil`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_etat_civil` (
  `pkEtatCivil` int(11) NOT NULL AUTO_INCREMENT,
  `abrev` varchar(1) NOT NULL,
  `nom` varchar(40) NOT NULL,
  PRIMARY KEY (`pkEtatCivil`),
  UNIQUE KEY `abrev_UNIQUE` (`abrev`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_groupe`
--

DROP TABLE IF EXISTS `t_groupe`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_groupe` (
  `pkGroupe` int(11) NOT NULL AUTO_INCREMENT,
  `abrev` varchar(10) NOT NULL,
  `nom` varchar(60) NOT NULL,
  PRIMARY KEY (`pkGroupe`),
  UNIQUE KEY `abrev_UNIQUE` (`abrev`)
) ENGINE=InnoDB AUTO_INCREMENT=36 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_login`
--

DROP TABLE IF EXISTS `t_login`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_login` (
  `pkLogin` int(11) NOT NULL AUTO_INCREMENT,
  `nom` varchar(50) NOT NULL,
  `domaine` varchar(45) NOT NULL DEFAULT 'STUDENTFR',
  `motDePasse` varchar(128) NOT NULL,
  `profil` varchar(64) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `initiales` varchar(4) DEFAULT NULL,
  `langue` varchar(2) DEFAULT NULL,
  `timestamp` timestamp NULL DEFAULT NULL,
  `version` int(11) DEFAULT NULL,
  PRIMARY KEY (`pkLogin`)
) ENGINE=InnoDB AUTO_INCREMENT=40 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_parti`
--

DROP TABLE IF EXISTS `t_parti`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_parti` (
  `pkParti` int(11) NOT NULL AUTO_INCREMENT,
  `abrev` varchar(10) NOT NULL,
  `nom` varchar(60) NOT NULL,
  PRIMARY KEY (`pkParti`),
  UNIQUE KEY `abrev_UNIQUE` (`abrev`)
) ENGINE=InnoDB AUTO_INCREMENT=83 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-03-17 22:38:42
