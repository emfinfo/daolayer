CREATE DATABASE  IF NOT EXISTS `parlement` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `parlement`;
-- MySQL dump 10.13  Distrib 5.6.19, for osx10.7 (i386)
--
-- Host: localhost    Database: parlement
-- ------------------------------------------------------
-- Server version	5.1.51

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
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
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_activite` (
  `pkActivite` int(11) NOT NULL AUTO_INCREMENT,
  `dateEntree` date DEFAULT NULL,
  `dateSortie` date DEFAULT NULL,
  `fkConseiller` int(11) NOT NULL,
  `fkConseil` int(11) NOT NULL,
  `fkGroupe` int(11) DEFAULT NULL,
  `fkFonction` int(11) DEFAULT NULL,
  PRIMARY KEY (`pkActivite`),
  KEY `activite_conseiller_idx` (`fkConseiller`),
  KEY `activite_conseil_idx` (`fkConseil`),
  KEY `activite_groupe_idx` (`fkGroupe`),
  KEY `activite_fonction_idx` (`fkFonction`),
  CONSTRAINT `conseiller_constraint` FOREIGN KEY (`fkConseiller`) REFERENCES `t_conseiller` (`pkConseiller`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `conseil_constraint` FOREIGN KEY (`fkConseil`) REFERENCES `t_conseil` (`pkConseil`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fonction_constraint` FOREIGN KEY (`fkFonction`) REFERENCES `t_fonction` (`pkFonction`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `groupe_constraint` FOREIGN KEY (`fkGroupe`) REFERENCES `t_groupe` (`pkGroupe`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=5893 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;


--
-- Table structure for table `t_canton`
--

DROP TABLE IF EXISTS `t_canton`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_canton` (
  `pkCanton` int(11) NOT NULL AUTO_INCREMENT,
  `abrev` varchar(2) NOT NULL,
  PRIMARY KEY (`pkCanton`),
  UNIQUE KEY `abrev_UNIQUE` (`abrev`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;


--
-- Table structure for table `t_conseil`
--

DROP TABLE IF EXISTS `t_conseil`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_conseil` (
  `pkConseil` int(11) NOT NULL AUTO_INCREMENT,
  `abrev` varchar(2) NOT NULL,
  PRIMARY KEY (`pkConseil`),
  UNIQUE KEY `abrev_UNIQUE` (`abrev`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;


--
-- Table structure for table `t_conseiller`
--

DROP TABLE IF EXISTS `t_conseiller`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_conseiller` (
  `pkConseiller` int(11) NOT NULL AUTO_INCREMENT,
  `nom` varchar(30) NOT NULL,
  `prenom` varchar(30) NOT NULL,
  `sexe` varchar(1) NOT NULL,
  `origine` varchar(100) DEFAULT NULL,
  `dateNaissance` date DEFAULT NULL,
  `dateDeces` date DEFAULT NULL,
  `actif` int(1) NOT NULL,
  `fkParti` int(11) NOT NULL,
  `fkCanton` int(11) NOT NULL,
  PRIMARY KEY (`pkConseiller`),
  KEY `conseiller_parti_idx` (`fkParti`),
  KEY `conseiller_canton_idx` (`fkCanton`),
  CONSTRAINT `canton_constraint` FOREIGN KEY (`fkCanton`) REFERENCES `t_canton` (`pkCanton`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `parti_constraint` FOREIGN KEY (`fkParti`) REFERENCES `t_parti` (`pkParti`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=3516 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;


--
-- Table structure for table `t_fonction`
--

DROP TABLE IF EXISTS `t_fonction`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_fonction` (
  `pkFonction` int(11) NOT NULL AUTO_INCREMENT,
  `nomFonction` varchar(20) NOT NULL,
  PRIMARY KEY (`pkFonction`),
  UNIQUE KEY `fonction_UNIQUE` (`nomFonction`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;


--
-- Table structure for table `t_groupe`
--

DROP TABLE IF EXISTS `t_groupe`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_groupe` (
  `pkGroupe` int(11) NOT NULL AUTO_INCREMENT,
  `nomGroupe` varchar(60) NOT NULL,
  PRIMARY KEY (`pkGroupe`),
  UNIQUE KEY `nomGroupe_UNIQUE` (`nomGroupe`)
) ENGINE=InnoDB AUTO_INCREMENT=45 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;


--
-- Table structure for table `t_login`
--

DROP TABLE IF EXISTS `t_login`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_login` (
  `pkLogin` int(11) NOT NULL AUTO_INCREMENT,
  `nom` varchar(50) NOT NULL,
  `motDePasse` varchar(128) DEFAULT NULL,
  `domaine` varchar(45) DEFAULT NULL,
  `profil` varchar(64) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `initiales` varchar(4) DEFAULT NULL,
  `langue` varchar(2) DEFAULT NULL,
  PRIMARY KEY (`pkLogin`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;


--
-- Table structure for table `t_parti`
--

DROP TABLE IF EXISTS `t_parti`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `t_parti` (
  `pkParti` int(11) NOT NULL AUTO_INCREMENT,
  `nomParti` varchar(60) NOT NULL,
  PRIMARY KEY (`pkParti`),
  UNIQUE KEY `abrev_UNIQUE` (`nomParti`)
) ENGINE=InnoDB AUTO_INCREMENT=80 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;


/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2016-03-17 15:44:47
