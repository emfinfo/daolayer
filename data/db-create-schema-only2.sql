-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

-- -----------------------------------------------------
-- Schema parlement
-- -----------------------------------------------------
DROP SCHEMA IF EXISTS `parlement` ;

-- -----------------------------------------------------
-- Schema parlement
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `parlement` DEFAULT CHARACTER SET latin1 ;
USE `parlement` ;

-- -----------------------------------------------------
-- Table `t_conseil`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `t_conseil` ;

CREATE TABLE IF NOT EXISTS `t_conseil` (
  `pkConseil` INT(11) NOT NULL AUTO_INCREMENT,
  `abrev` VARCHAR(2) NOT NULL,
  `nom` VARCHAR(30) NOT NULL,
  PRIMARY KEY (`pkConseil`))
ENGINE = InnoDB
AUTO_INCREMENT = 4
DEFAULT CHARACTER SET = latin1;

CREATE UNIQUE INDEX `abrev_UNIQUE` ON `t_conseil` (`abrev` ASC);


-- -----------------------------------------------------
-- Table `t_canton`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `t_canton` ;

CREATE TABLE IF NOT EXISTS `t_canton` (
  `pkCanton` INT(11) NOT NULL AUTO_INCREMENT,
  `abrev` VARCHAR(2) NOT NULL,
  `nom` VARCHAR(30) NOT NULL,
  PRIMARY KEY (`pkCanton`))
ENGINE = InnoDB
AUTO_INCREMENT = 27
DEFAULT CHARACTER SET = latin1;

CREATE UNIQUE INDEX `abrev_UNIQUE` ON `t_canton` (`abrev` ASC);


-- -----------------------------------------------------
-- Table `t_parti`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `t_parti` ;

CREATE TABLE IF NOT EXISTS `t_parti` (
  `pkParti` INT(11) NOT NULL AUTO_INCREMENT,
  `abrev` VARCHAR(10) NOT NULL,
  `nom` VARCHAR(60) NOT NULL,
  PRIMARY KEY (`pkParti`))
ENGINE = InnoDB
AUTO_INCREMENT = 80
DEFAULT CHARACTER SET = latin1;

CREATE UNIQUE INDEX `abrev_UNIQUE` ON `t_parti` (`abrev` ASC);


-- -----------------------------------------------------
-- Table `t_etat_civil`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `t_etat_civil` ;

CREATE TABLE IF NOT EXISTS `t_etat_civil` (
  `pkEtatCivil` INT NOT NULL AUTO_INCREMENT,
  `abrev` VARCHAR(1) NOT NULL,
  `nom` VARCHAR(40) NOT NULL,
  PRIMARY KEY (`pkEtatCivil`))
ENGINE = InnoDB;

CREATE UNIQUE INDEX `abrev_UNIQUE` ON `t_etat_civil` (`abrev` ASC);


-- -----------------------------------------------------
-- Table `t_conseiller`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `t_conseiller` ;

CREATE TABLE IF NOT EXISTS `t_conseiller` (
  `pkConseiller` INT(11) NOT NULL AUTO_INCREMENT,
  `actif` INT(1) NOT NULL,
  `dateNaissance` DATE NULL DEFAULT NULL,
  `dateDeces` DATE NULL DEFAULT NULL,
  `nom` VARCHAR(30) NOT NULL,
  `prenom` VARCHAR(30) NOT NULL,
  `sexe` VARCHAR(1) NOT NULL,
  `citoyennete` VARCHAR(100) NULL DEFAULT NULL,
  `lieuNaissance` VARCHAR(60) NULL DEFAULT NULL,
  `cantonNaissance` VARCHAR(30) NULL DEFAULT NULL,
  `mandats` LONGTEXT NULL DEFAULT NULL,
  `fkEtatCivil` INT(11) NOT NULL,
  `fkCanton` INT(11) NOT NULL,
  `fkParti` INT(11) NOT NULL,
  PRIMARY KEY (`pkConseiller`),
  CONSTRAINT `canton_constraint`
    FOREIGN KEY (`fkCanton`)
    REFERENCES `t_canton` (`pkCanton`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `parti_constraint`
    FOREIGN KEY (`fkParti`)
    REFERENCES `t_parti` (`pkParti`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_t_conseiller_t_etat_civil1`
    FOREIGN KEY (`fkEtatCivil`)
    REFERENCES `t_etat_civil` (`pkEtatCivil`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
AUTO_INCREMENT = 3542
DEFAULT CHARACTER SET = latin1;

CREATE INDEX `conseiller_parti_idx` ON `t_conseiller` (`fkParti` ASC);

CREATE INDEX `conseiller_canton_idx` ON `t_conseiller` (`fkCanton` ASC);

CREATE INDEX `conseiller_etat_civil_idx` ON `t_conseiller` (`fkEtatCivil` ASC);


-- -----------------------------------------------------
-- Table `t_groupe`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `t_groupe` ;

CREATE TABLE IF NOT EXISTS `t_groupe` (
  `pkGroupe` INT(11) NOT NULL AUTO_INCREMENT,
  `abrev` VARCHAR(10) NOT NULL,
  `nom` VARCHAR(60) NOT NULL,
  PRIMARY KEY (`pkGroupe`))
ENGINE = InnoDB
AUTO_INCREMENT = 45
DEFAULT CHARACTER SET = latin1;

CREATE UNIQUE INDEX `abrev_UNIQUE` ON `t_groupe` (`abrev` ASC);


-- -----------------------------------------------------
-- Table `t_activite`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `t_activite` ;

CREATE TABLE IF NOT EXISTS `t_activite` (
  `pkActivite` INT(11) NOT NULL AUTO_INCREMENT,
  `dateEntree` DATE NULL DEFAULT NULL,
  `dateSortie` DATE NULL DEFAULT NULL,
  `fkConseiller` INT(11) NOT NULL,
  `fkConseil` INT(11) NOT NULL,
  `fkGroupe` INT(11) NOT NULL,
  PRIMARY KEY (`pkActivite`),
  CONSTRAINT `conseil_constraint`
    FOREIGN KEY (`fkConseil`)
    REFERENCES `t_conseil` (`pkConseil`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `conseiller_constraint`
    FOREIGN KEY (`fkConseiller`)
    REFERENCES `t_conseiller` (`pkConseiller`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `groupe_constraint`
    FOREIGN KEY (`fkGroupe`)
    REFERENCES `t_groupe` (`pkGroupe`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
AUTO_INCREMENT = 5893
DEFAULT CHARACTER SET = latin1;

CREATE INDEX `activite_conseiller_idx` ON `t_activite` (`fkConseiller` ASC);

CREATE INDEX `activite_conseil_idx` ON `t_activite` (`fkConseil` ASC);

CREATE INDEX `activite_groupe_idx` ON `t_activite` (`fkGroupe` ASC);


-- -----------------------------------------------------
-- Table `t_login`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `t_login` ;

CREATE TABLE IF NOT EXISTS `t_login` (
  `pkLogin` INT(11) NOT NULL AUTO_INCREMENT,
  `nom` VARCHAR(50) NOT NULL,
  `motDePasse` VARCHAR(128) NULL DEFAULT NULL,
  `domaine` VARCHAR(45) NULL DEFAULT NULL,
  `profil` VARCHAR(64) NULL DEFAULT NULL,
  `email` VARCHAR(255) NULL DEFAULT NULL,
  `initiales` VARCHAR(4) NULL DEFAULT NULL,
  `langue` VARCHAR(2) NULL DEFAULT NULL,
  PRIMARY KEY (`pkLogin`))
ENGINE = InnoDB
AUTO_INCREMENT = 3
DEFAULT CHARACTER SET = latin1;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
