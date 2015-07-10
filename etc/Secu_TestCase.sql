-- phpMyAdmin SQL Dump
-- version 4.1.4
-- http://www.phpmyadmin.net
--
-- Host: 127.0.0.1
-- Generation Time: Feb 26, 2015 at 11:14 AM
-- Server version: 5.6.15-log
-- PHP Version: 5.3.28

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT = @@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS = @@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION = @@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `reqbaz`
--

-- --------------------------------------------------------

--
-- Table structure for table `attachments`
--

CREATE TABLE IF NOT EXISTS `attachments` (
  `Id`             INT(11)          NOT NULL AUTO_INCREMENT,
  `creation_time`  TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `Requirement_Id` INT(11)          NOT NULL,
  `User_Id`        INT(11)          NOT NULL,
  `title`          VARCHAR(255)
                   COLLATE utf8_bin NOT NULL,
  `discriminator`  CHAR(1)
                   COLLATE utf8_bin NOT NULL,
  `file_path`      VARCHAR(500)
                   COLLATE utf8_bin          DEFAULT NULL,
  `description`    TEXT
                   COLLATE utf8_bin,
  `story`          TEXT
                   COLLATE utf8_bin,
  `subject`        VARCHAR(255)
                   COLLATE utf8_bin          DEFAULT NULL,
  `object`         VARCHAR(255)
                   COLLATE utf8_bin          DEFAULT NULL,
  `object_desc`    VARCHAR(255)
                   COLLATE utf8_bin          DEFAULT NULL,
  PRIMARY KEY (`Id`),
  KEY `Attachement_Requirement` (`Requirement_Id`),
  KEY `Attachement_User` (`User_Id`)
)
  ENGINE = MyISAM
  DEFAULT CHARSET = utf8
  COLLATE = utf8_bin
  AUTO_INCREMENT = 1;

-- --------------------------------------------------------

--
-- Table structure for table `comments`
--

CREATE TABLE IF NOT EXISTS `comments` (
  `Id`             INT(11)          NOT NULL AUTO_INCREMENT,
  `message`        TEXT
                   COLLATE utf8_bin NOT NULL,
  `creation_time`  TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `Requirement_Id` INT(11)          NOT NULL,
  `User_Id`        INT(11)          NOT NULL,
  PRIMARY KEY (`Id`),
  KEY `Comment_Requirement` (`Requirement_Id`),
  KEY `Comment_User` (`User_Id`)
)
  ENGINE = MyISAM
  DEFAULT CHARSET = utf8
  COLLATE = utf8_bin
  AUTO_INCREMENT = 3;

--
-- Dumping data for table `comments`
--

INSERT INTO `comments` (`Id`, `message`, `creation_time`, `Requirement_Id`, `User_Id`) VALUES
  (1, 'TestPublicComment', '2015-02-23 15:39:42', 1, 42),
  (2, 'TestPrivateComment', '2015-02-23 15:38:34', 2, 42);

-- --------------------------------------------------------

--
-- Table structure for table `components`
--

CREATE TABLE IF NOT EXISTS `components` (
  `Id`          INT(11)          NOT NULL AUTO_INCREMENT,
  `name`        VARCHAR(255)
                COLLATE utf8_bin NOT NULL,
  `description` TEXT
                COLLATE utf8_bin NOT NULL,
  `Project_Id`  INT(11)          NOT NULL,
  `Leader_Id`   INT(11)          NOT NULL,
  PRIMARY KEY (`Id`),
  KEY `Component_Project` (`Project_Id`),
  KEY `Components_Users` (`Leader_Id`)
)
  ENGINE = MyISAM
  DEFAULT CHARSET = utf8
  COLLATE = utf8_bin
  AUTO_INCREMENT = 902;

--
-- Dumping data for table `components`
--

INSERT INTO `components` (`Id`, `name`, `description`, `Project_Id`, `Leader_Id`) VALUES
  (1, 'PublicProjectComponent', 'hyenfngfgaegafd', 1, 0),
  (2, 'PrivateProjectComponent', 'mmmkwmprmewmr', 2, 0);

-- --------------------------------------------------------

--
-- Table structure for table `developers`
--

CREATE TABLE IF NOT EXISTS `developers` (
  `Id`             INT(11) NOT NULL AUTO_INCREMENT,
  `Requirement_Id` INT(11) NOT NULL,
  `User_Id`        INT(11) NOT NULL,
  PRIMARY KEY (`Id`),
  KEY `Developer_Requirement` (`Requirement_Id`),
  KEY `Developer_User` (`User_Id`)
)
  ENGINE = MyISAM
  DEFAULT CHARSET = utf8
  COLLATE = utf8_bin
  AUTO_INCREMENT = 1;

-- --------------------------------------------------------

--
-- Table structure for table `followers`
--

CREATE TABLE IF NOT EXISTS `followers` (
  `Id`             INT(11) NOT NULL AUTO_INCREMENT,
  `Requirement_Id` INT(11) NOT NULL,
  `User_Id`        INT(11) NOT NULL,
  PRIMARY KEY (`Id`),
  KEY `Follower_Requirement` (`Requirement_Id`),
  KEY `Follower_User` (`User_Id`)
)
  ENGINE = MyISAM
  DEFAULT CHARSET = utf8
  COLLATE = utf8_bin
  AUTO_INCREMENT = 1;

-- --------------------------------------------------------

--
-- Table structure for table `privileges`
--

CREATE TABLE IF NOT EXISTS `privileges` (
  `Id`   INT(11)          NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100)
         COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`Id`)
)
  ENGINE = MyISAM
  DEFAULT CHARSET = utf8
  COLLATE = utf8_bin
  AUTO_INCREMENT = 21;

--
-- Dumping data for table `privileges`
--

INSERT INTO `privileges` (`Id`, `name`) VALUES
  (1, 'Create_PROJECT'),
  (2, 'Read_PROJECT'),
  (3, 'Read_PUBLIC_PROJECT'),
  (4, 'Modify_PROJECT'),
  (5, 'Create_COMPONENT'),
  (6, 'Read_COMPONENT'),
  (7, 'Read_PUBLIC_COMPONENT'),
  (8, 'Modify_COMPONENT'),
  (9, 'Create_REQUIREMENT'),
  (10, 'Read_REQUIREMENT'),
  (11, 'Read_PUBLIC_REQUIREMENT'),
  (12, 'Modify_REQUIREMENT'),
  (13, 'Create_COMMENT'),
  (14, 'Read_COMMENT'),
  (15, 'Read_PUBLIC_COMMENT'),
  (16, 'Modify_COMMENT'),
  (17, 'Create_ATTACHMENT'),
  (18, 'Read_ATTACHMENT'),
  (19, 'Read_PUBLIC_ATTACHMENT'),
  (20, 'Modify_ATTACHMENT');

-- --------------------------------------------------------

--
-- Table structure for table `projects`
--

CREATE TABLE IF NOT EXISTS `projects` (
  `Id`                    INT(11)          NOT NULL AUTO_INCREMENT,
  `name`                  VARCHAR(255)
                          COLLATE utf8_bin NOT NULL,
  `description`           TEXT
                          COLLATE utf8_bin NOT NULL,
  `visibility`            CHAR(1)
                          COLLATE utf8_bin NOT NULL,
  `Leader_Id`             INT(11)          NOT NULL,
  `Default_Components_Id` INT(11)                   DEFAULT NULL,
  PRIMARY KEY (`Id`),
  KEY `Projects_Components` (`Default_Components_Id`),
  KEY `Projects_Users` (`Leader_Id`)
)
  ENGINE = MyISAM
  DEFAULT CHARSET = utf8
  COLLATE = utf8_bin
  AUTO_INCREMENT = 3;

--
-- Dumping data for table `projects`
--

INSERT INTO `projects` (`Id`, `name`, `description`, `visibility`, `Leader_Id`, `Default_Components_Id`) VALUES
  (1, 'PublicTestProject', 'dfasfasdfasdfasfasdfasd', '+', 0, NULL),
  (2, 'PrivateTestProject', 'thsjyrjsjysyrjys', '-', 0, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `requirements`
--

CREATE TABLE IF NOT EXISTS `requirements` (
  `Id`                INT(11)          NOT NULL AUTO_INCREMENT,
  `title`             VARCHAR(255)
                      COLLATE utf8_bin NOT NULL,
  `description`       TEXT
                      COLLATE utf8_bin NOT NULL,
  `creation_time`     TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `Lead_developer_Id` INT(11)          NOT NULL,
  `Creator_Id`        INT(11)          NOT NULL,
  `Project_Id`        INT(11)          NOT NULL,
  PRIMARY KEY (`Id`),
  KEY `Creator` (`Creator_Id`),
  KEY `LeadDeveloper` (`Lead_developer_Id`),
  KEY `Requirement_Project` (`Project_Id`)
)
  ENGINE = MyISAM
  DEFAULT CHARSET = utf8
  COLLATE = utf8_bin
  AUTO_INCREMENT = 3;

--
-- Dumping data for table `requirements`
--

INSERT INTO `requirements` (`Id`, `title`, `description`, `creation_time`, `Lead_developer_Id`, `Creator_Id`, `Project_Id`)
VALUES
  (1, 'PublicRequirement', 'vfehbsdgfd', '2015-02-23 15:29:48', 42, 42, 1),
  (2, 'PrivateRequirement', 'fsdfsdg', '2015-02-23 15:29:54', 42, 42, 2);

-- --------------------------------------------------------

--
-- Table structure for table `roles`
--

CREATE TABLE IF NOT EXISTS `roles` (
  `Id`   INT(11) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50)
         COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`Id`),
  UNIQUE KEY `Role_idx_1` (`name`)
)
  ENGINE = MyISAM
  DEFAULT CHARSET = utf8
  COLLATE = utf8_bin
  AUTO_INCREMENT = 5;

--
-- Dumping data for table `roles`
--

INSERT INTO `roles` (`Id`, `name`) VALUES
  (1, 'Anonymous'),
  (2, 'LoggedInUser'),
  (3, 'ProjectAdmin'),
  (4, 'SystemAdmin');

-- --------------------------------------------------------

--
-- Table structure for table `role_privilege`
--

CREATE TABLE IF NOT EXISTS `role_privilege` (
  `Id`            INT(11) NOT NULL AUTO_INCREMENT,
  `Roles_Id`      INT(11) NOT NULL,
  `Privileges_Id` INT(11) NOT NULL,
  PRIMARY KEY (`Id`),
  KEY `Role_Privilege_Privileges` (`Privileges_Id`),
  KEY `Role_Privilege_Roles` (`Roles_Id`)
)
  ENGINE = MyISAM
  DEFAULT CHARSET = utf8
  COLLATE = utf8_bin
  AUTO_INCREMENT = 25;

--
-- Dumping data for table `role_privilege`
--

INSERT INTO `role_privilege` (`Id`, `Roles_Id`, `Privileges_Id`) VALUES
  (1, 1, 3),
  (2, 1, 7),
  (3, 1, 11),
  (4, 1, 15),
  (5, 1, 19),
  (6, 4, 1),
  (7, 4, 2),
  (8, 4, 8),
  (9, 4, 7),
  (10, 4, 6),
  (11, 4, 5),
  (12, 4, 3),
  (13, 4, 4),
  (14, 4, 9),
  (15, 4, 10),
  (16, 4, 11),
  (17, 4, 12),
  (18, 4, 13),
  (19, 4, 14),
  (20, 4, 16),
  (21, 4, 17),
  (22, 4, 18),
  (23, 4, 19),
  (24, 4, 20);

-- --------------------------------------------------------

--
-- Table structure for table `role_role`
--

CREATE TABLE IF NOT EXISTS `role_role` (
  `Id`        INT(11) NOT NULL AUTO_INCREMENT,
  `Child_Id`  INT(11) NOT NULL,
  `Parent_Id` INT(11) NOT NULL,
  PRIMARY KEY (`Id`),
  KEY `Role_Child` (`Child_Id`),
  KEY `Role_Parent` (`Parent_Id`)
)
  ENGINE = MyISAM
  DEFAULT CHARSET = utf8
  COLLATE = utf8_bin
  AUTO_INCREMENT = 4;

--
-- Dumping data for table `role_role`
--

INSERT INTO `role_role` (`Id`, `Child_Id`, `Parent_Id`) VALUES
  (1, 2, 1),
  (2, 3, 2),
  (3, 4, 3);

-- --------------------------------------------------------

--
-- Table structure for table `tags`
--

CREATE TABLE IF NOT EXISTS `tags` (
  `Id`              INT(11) NOT NULL AUTO_INCREMENT,
  `Components_Id`   INT(11) NOT NULL,
  `Requirements_Id` INT(11) NOT NULL,
  PRIMARY KEY (`Id`),
  KEY `Tags_Components` (`Components_Id`),
  KEY `Tags_Requirements` (`Requirements_Id`)
)
  ENGINE = MyISAM
  DEFAULT CHARSET = utf8
  COLLATE = utf8_bin
  AUTO_INCREMENT = 3;

--
-- Dumping data for table `tags`
--

INSERT INTO `tags` (`Id`, `Components_Id`, `Requirements_Id`) VALUES
  (1, 1, 1),
  (2, 2, 2);

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE IF NOT EXISTS `users` (
  `Id`            INT(11)          NOT NULL AUTO_INCREMENT,
  `first_name`    VARCHAR(150)
                  COLLATE utf8_bin          DEFAULT NULL,
  `last_name`     VARCHAR(150)
                  COLLATE utf8_bin          DEFAULT NULL,
  `email`         VARCHAR(255)
                  COLLATE utf8_bin NOT NULL,
  `admin`         TINYINT(1)       NOT NULL,
  `Las2peer_Id`   BIGINT(20)       NOT NULL,
  `user_name`     VARCHAR(255)
                  COLLATE utf8_bin          DEFAULT NULL,
  `profile_image` TEXT
                  COLLATE utf8_bin,
  PRIMARY KEY (`Id`)
)
  ENGINE = MyISAM
  DEFAULT CHARSET = utf8
  COLLATE = utf8_bin
  AUTO_INCREMENT = 45;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`Id`, `first_name`, `last_name`, `email`, `admin`, `Las2peer_Id`, `user_name`, `profile_image`)
VALUES
  (41, NULL, NULL, 'anonymous@requirements-bazaar.org', 0, -1722613621014065292, 'anonymous',
   'https://api.learning-layers.eu/profile.png'),
  (42, NULL, NULL, 'no.email@warning.com', 0, -5021131362343084485, 'adam',
   'https://api.learning-layers.eu/profile.png'),
  (43, NULL, NULL, 'no.email@warning.com', 0, -7678677630072537742, 'abel',
   'https://api.learning-layers.eu/profile.png'),
  (44, NULL, NULL, 'no.email@warning.com', 0, -8626247308297765772, 'eve1st',
   'https://api.learning-layers.eu/profile.png');

-- --------------------------------------------------------

--
-- Table structure for table `user_role`
--

CREATE TABLE IF NOT EXISTS `user_role` (
  `Id`           INT(11) NOT NULL AUTO_INCREMENT,
  `Roles_Id`     INT(11) NOT NULL,
  `Users_Id`     INT(11) NOT NULL,
  `context_info` VARCHAR(255)
                 COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`Id`),
  KEY `User_Role_Roles` (`Roles_Id`),
  KEY `User_Role_Users` (`Users_Id`)
)
  ENGINE = MyISAM
  DEFAULT CHARSET = utf8
  COLLATE = utf8_bin
  AUTO_INCREMENT = 3;

--
-- Dumping data for table `user_role`
--

INSERT INTO `user_role` (`Id`, `Roles_Id`, `Users_Id`, `context_info`) VALUES
  (1, 1, 41, NULL),
  (2, 4, 44, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `votes`
--

CREATE TABLE IF NOT EXISTS `votes` (
  `Id`             INT(11)    NOT NULL AUTO_INCREMENT,
  `is_upvote`      TINYINT(1) NOT NULL,
  `Requirement_Id` INT(11)    NOT NULL,
  `User_Id`        INT(11)    NOT NULL,
  PRIMARY KEY (`Id`),
  KEY `Votes_Requirement` (`Requirement_Id`),
  KEY `Votes_User` (`User_Id`)
)
  ENGINE = MyISAM
  DEFAULT CHARSET = utf8
  COLLATE = utf8_bin
  AUTO_INCREMENT = 2;

--
-- Dumping data for table `votes`
--

INSERT INTO `votes` (`Id`, `is_upvote`, `Requirement_Id`, `User_Id`) VALUES
  (1, 0, 1, 41);

/*!40101 SET CHARACTER_SET_CLIENT = @OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS = @OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION = @OLD_COLLATION_CONNECTION */;
