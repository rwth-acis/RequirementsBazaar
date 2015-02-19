INSERT INTO `reqbaz`.`users` 
(`Id`,`first_name`, `last_name`, `email`, `admin`, `Las2peer_Id`, `user_name`)
 VALUES 
('1','Max1', 'Mustermann1', 'Max@Mustermann1.de', '1', '1', 'MaxMustermann1'),
('2','Max2', 'Mustermann2', 'Max@Mustermann2.de', '1', '2', 'MaxMustermann2'),
('3','Max3', 'Mustermann3', 'Max@Mustermann3.de', '1', '3', 'MaxMustermann3'),
('4','Max4', 'Mustermann4', 'Max@Mustermann4.de', '1', '4', 'MaxMustermann4'),
('5','Max5', 'Mustermann5', 'Max@Mustermann5.de', '1', '5', 'MaxMustermann5'),
('6','Max6', 'Mustermann6', 'Max@Mustermann6.de', '1', '6', 'MaxMustermann6'),
('7','Max7', 'Mustermann7', 'Max@Mustermann7.de', '1', '7', 'MaxMustermann7'),
('8','Max8', 'Mustermann8', 'Max@Mustermann8.de', '1', '8', 'MaxMustermann8'),
('9','Max9', 'Mustermann9', 'Max@Mustermann9.de', '1', '9', 'MaxMustermann9'),
('10','Max10', 'Mustermann10', 'Max@Mustermann10.de', '0', '10', 'MaxMustermann10'),
('11','Max11', 'Mustermann11', 'Max@Mustermann11.de', '0', '11', 'MaxMustermann11'),
('12','Max12', 'Mustermann12', 'Max@Mustermann12.de', '0', '12', 'MaxMustermann12'),
('13','Max13', 'Mustermann13', 'Max@Mustermann13.de', '0', '13', 'MaxMustermann13'),
('14','Max14', 'Mustermann14', 'Max@Mustermann14.de', '0', '14', 'MaxMustermann14'),
('15','Max15', 'Mustermann15', 'Max@Mustermann15.de', '0', '15', 'MaxMustermann15'),
('16','Max16', 'Mustermann16', 'Max@Mustermann16.de', '0', '16', 'MaxMustermann16'),
('17','Max17', 'Mustermann17', 'Max@Mustermann17.de', '0', '17', 'MaxMustermann17'),
('18','Max18', 'Mustermann18', 'Max@Mustermann18.de', '0', '18', 'MaxMustermann18'),
('19','Max19', 'Mustermann19', 'Max@Mustermann19.de', '0', '19', 'MaxMustermann19'),
('20','Max20', 'Mustermann20', 'Max@Mustermann20.de', '0', '20', 'MaxMustermann20'),
('21','Max21', 'Mustermann21', 'Max@Mustermann21.de', '0', '21', 'MaxMustermann21'),
('22','Max22', 'Mustermann22', 'Max@Mustermann22.de', '0', '22', 'MaxMustermann22'),
('23','Max23', 'Mustermann23', 'Max@Mustermann23.de', '0', '23', 'MaxMustermann23'),
('24','Max24', 'Mustermann24', 'Max@Mustermann24.de', '0', '24', 'MaxMustermann24'),
('25','Max25', 'Mustermann25', 'Max@Mustermann25.de', '0', '25', 'MaxMustermann25'),
('26','Max26', 'Mustermann26', 'Max@Mustermann26.de', '0', '26', 'MaxMustermann26'),
('27','Max27', 'Mustermann27', 'Max@Mustermann27.de', '0', '27', 'MaxMustermann27'),
('28','Max28', 'Mustermann28', 'Max@Mustermann28.de', '0', '28', 'MaxMustermann28'),
('29','Max29', 'Mustermann29', 'Max@Mustermann29.de', '0', '29', 'MaxMustermann29'),
('30','Max30', 'Mustermann30', 'Max@Mustermann30.de', '0', '30', 'MaxMustermann30'),
('31','Max31', 'Mustermann31', 'Max@Mustermann31.de', '0', '31', 'MaxMustermann31'),
('32','Max32', 'Mustermann32', 'Max@Mustermann32.de', '0', '32', 'MaxMustermann32'),
('33','Max33', 'Mustermann33', 'Max@Mustermann33.de', '0', '33', 'MaxMustermann33'),
('34','Max34', 'Mustermann34', 'Max@Mustermann34.de', '0', '34', 'MaxMustermann34'),
('35','Max35', 'Mustermann35', 'Max@Mustermann35.de', '0', '35', 'MaxMustermann35'),
('36','Max36', 'Mustermann36', 'Max@Mustermann36.de', '0', '36', 'MaxMustermann36'),
('37','Max37', 'Mustermann37', 'Max@Mustermann37.de', '0', '37', 'MaxMustermann37'),
('38','Max38', 'Mustermann38', 'Max@Mustermann38.de', '0', '38', 'MaxMustermann38'),
('39','Max39', 'Mustermann39', 'Max@Mustermann39.de', '0', '39', 'MaxMustermann39'),
('40','Max40', 'Mustermann40', 'Max@Mustermann40.de', '0', '40', 'MaxMustermann40'),
('41',NULL, NULL, 'anonymous@requirements-bazaar.org', '0', '-1722613621014065292', 'anonymous');


INSERT INTO `reqbaz`.`projects` 
(`Id`,`name`, `description`, `visibility`, `Leader_Id`, `Default_Components_Id`) 
VALUES
('1','Project 1', 'Project 1 - Description - This project is visible - leader MaxMustermann1', '+', '1',NULL),
('2','Project 2', 'Project 2 - Description - This project is visible - leader MaxMustermann1', '+', '1',NULL),
('3','Project 3', 'Project 3 - Description - This project is visible - leader MaxMustermann1', '+', '1',NULL),
('4','Project 4', 'Project 4 - Description - This project is visible - leader MaxMustermann2', '+', '2',NULL),
('5','Project 5', 'Project 5 - Description - This project is visible - leader MaxMustermann3', '+', '3',NULL),
('6','Project 6', 'Project 6 - Description - This project is visible - leader MaxMustermann4', '+', '4',NULL),
('7','Project 7', 'Project 7 - Description - This project is visible - leader MaxMustermann5', '+', '5',NULL),
('8','Project 8', 'Project 8 - Description - This project is visible - leader MaxMustermann6  - Has no components', '+', '6',NULL),
('9','Project 9', 'Project 9 - Description - This project is not visible - leader MaxMustermann7', '-', '7',NULL),
('10','Project 10', 'Project 10 - Description - This project is not visible - leader MaxMustermann8', '-', '8',NULL);



INSERT INTO `reqbaz`.`components` 
(`Id`,`name`, `description`, `Project_Id`, `Leader_Id`) 
VALUES 
('1','Component 1', 'Component  - Description', '1', '1'),
('2','Component 2', 'Component  - Description', '1', '2'),
('3','Component 3', 'Component  - Description', '1', '2'),
('4','Component 4', 'Component  - Description', '1', '2'),
('5','Component 5', 'Component  - Description', '1', '2'),
('6','Component 6', 'Component  - Description', '1', '3'),
('7','Component 7', 'Component  - Description', '1', '4'),
('8','Component 8', 'Component  - Description', '2', '5'),
('9','Component 9', 'Component  - Description', '2', '6'),
('10','Component 10', 'Component  - Description', '2', '7'),
('11','Component 11', 'Component  - Description', '2', '8'),
('12','Component 12', 'Component  - Description', '2', '9'),
('13','Component 13', 'Component  - Description', '2', '10'),
('14','Component 14', 'Component  - Description', '2', '11'),
('15','Component 15', 'Component  - Description', '3', '12'),
('16','Component 16', 'Component  - Description', '3', '13'),
('17','Component 17', 'Component  - Description', '3', '14'),
('18','Component 18', 'Component  - Description', '4', '15'),
('19','Component 19', 'Component  - Description', '4', '16'),
('20','Component 20', 'Component  - Description', '5', '17'),
('21','Component 21', 'Component  - Description', '5', '18'),
('22','Component 22', 'Component  - Description', '5', '1'),
('23','Component 23', 'Component  - Description', '6', '1'),
('24','Component 24', 'Component  - Description', '6', '1'),
('25','Component 25', 'Component  - Description', '7', '1'),
('26','Component 26', 'Component  - Description', '7', '1'),
('27','Component 27', 'Component  - Description', '9', '1'),
('28','Component 28', 'Component  - Description', '10', '1'),
('29','Component 29', 'Component  - Description', '10', '1'),
('30','Component 30', 'Component  - Description', '10', '1'),
('31','Uncategorized P1 ', 'Uncategorized requirements for Project 1 ', '1 ', '1'),
('32','Uncategorized-P2 ', 'Uncategorized requirements for Project 2 ', '2 ', '1'),
('33','Uncategorized-P3 ', 'Uncategorized requirements for Project 3 ', '3 ', '1'),
('34','Uncategorized-P4 ', 'Uncategorized requirements for Project 4 ', '4 ', '2'),
('35','Uncategorized-P5 ', 'Uncategorized requirements for Project 5 ', '5 ', '3'),
('36','Uncategorized-P6 ', 'Uncategorized requirements for Project 6 ', '6 ', '4'),
('37','Uncategorized-P7 ', 'Uncategorized requirements for Project 7 ', '7 ', '5'),
('38','Uncategorized-P8 ', 'Uncategorized requirements for Project 8 ', '8 ', '6'),
('39','Uncategorized-P9 ', 'Uncategorized requirements for Project 9 ', '9 ', '7'),
('40','Uncategorized-P10', 'Uncategorized requirements for Project 10', '10', '8');

UPDATE `reqbaz`.`projects` SET `Default_Components_Id`='31' WHERE `Id`='1 '; 
UPDATE `reqbaz`.`projects` SET `Default_Components_Id`='32' WHERE `Id`='2 '; 
UPDATE `reqbaz`.`projects` SET `Default_Components_Id`='33' WHERE `Id`='3 '; 
UPDATE `reqbaz`.`projects` SET `Default_Components_Id`='34' WHERE `Id`='4 '; 
UPDATE `reqbaz`.`projects` SET `Default_Components_Id`='35' WHERE `Id`='5 '; 
UPDATE `reqbaz`.`projects` SET `Default_Components_Id`='36' WHERE `Id`='6 '; 
UPDATE `reqbaz`.`projects` SET `Default_Components_Id`='37' WHERE `Id`='7 '; 
UPDATE `reqbaz`.`projects` SET `Default_Components_Id`='38' WHERE `Id`='8 '; 
UPDATE `reqbaz`.`projects` SET `Default_Components_Id`='39' WHERE `Id`='9 '; 
UPDATE `reqbaz`.`projects` SET `Default_Components_Id`='40' WHERE `Id`='10'; 



INSERT INTO `reqbaz`.`requirements` 
(`Id`, `title`, `description`, `Lead_developer_Id`, `Creator_Id`, `Project_Id`) 
VALUES 
('1', 'Requirement ', 'Requirement - Description', '1', '1', '1'),
('2', 'Requirement ', 'Requirement - Description', '1', '1', '1'),
('3', 'Requirement ', 'Requirement - Description', '1', '1', '1'),
('4', 'Requirement ', 'Requirement - Description', '1', '1', '1'),
('5', 'Requirement ', 'Requirement - Description', '1', '1', '1'),
('6', 'Requirement ', 'Requirement - Description', '1', '1', '1'),
('7', 'Requirement ', 'Requirement - Description', '2', '2', '1'),
('8', 'Requirement ', 'Requirement - Description', '2', '2', '1'),
('9', 'Requirement ', 'Requirement - Description', '2', '2', '3'),
('10', 'Requirement ', 'Requirement - Description', '2', '2', '4'),
('11', 'Requirement ', 'Requirement - Description', '2', '2', '5'),
('12', 'Requirement ', 'Requirement - Description', '1', '3', '6'),
('13', 'Requirement ', 'Requirement - Description', '1', '4', '7'),
('14', 'Requirement ', 'Requirement - Description', '1', '5', '8'),
('15', 'Requirement ', 'Requirement - Description', '1', '6', '8'),
('16', 'Requirement ', 'Requirement - Description', '3', '7', '9'),
('17', 'Requirement ', 'Requirement - Description', '4', '8', '10'),
('18', 'Requirement ', 'Requirement - Description', '5', '9', '10'),
('19', 'Requirement ', 'Requirement - Description', '6', '10', '10'),
('20', 'Requirement ', 'Requirement - Description', '1', '11', '1'),
('21', 'Requirement ', 'Requirement - Description', '1', '12', '1'),
('22', 'Requirement ', 'Requirement - Description', '1', '13', '1'),
('23', 'Requirement ', 'Requirement - Description', '1', '14', '1'),
('24', 'Requirement ', 'Requirement - Description', '7', '15', '1'),
('25', 'Requirement ', 'Requirement - Description', '1', '16', '1'),
('26', 'Requirement ', 'Requirement - Description', '8', '17', '1'),
('27', 'Requirement ', 'Requirement - Description', '8', '18', '1'),
('28', 'Requirement ', 'Requirement - Description', '8', '1', '2'),
('29', 'Requirement ', 'Requirement - Description', '8', '1', '2'),
('30', 'Requirement ', 'Requirement - Description', '8', '1', '1'),
('31', 'Requirement ', 'Requirement - Description', '8', '1', '1'),
('32', 'Requirement ', 'Requirement - Description', '8', '1', '1'),
('33', 'Requirement ', 'Requirement - Description', '8', '1', '1'),
('34', 'Requirement ', 'Requirement - Description', '9', '2', '1'),
('35', 'Requirement ', 'Requirement - Description', '10', '2', '1'),
('36', 'Requirement ', 'Requirement - Description', '11', '3', '1'),
('37', 'Requirement ', 'Requirement - Description', '12', '3', '1'),
('38', 'Requirement ', 'Requirement - Description', '14', '3', '2'),
('39', 'Requirement ', 'Requirement - Description', '15', '3', '2'),
('40', 'Requirement ', 'Requirement - Description', '16', '3', '2');




INSERT INTO `reqbaz`.`followers`
 (`Id`, `Requirement_Id`, `User_Id`) 
VALUES 
('1', '1', '1'),
('2', '1', '1'),
('3', '1', '2'),
('4', '1', '2'),
('5', '1', '2'),
('6', '2', '3'),
('7', '2', '4'),
('8', '2', '5'),
('9', '2', '6'),
('10', '2', '16'),
('11', '2', '18'),
('12', '2', '19'),
('13', '3', '20'),
('14', '3', '24'),
('15', '4', '30'),
('16', '5', '31'),
('17', '6', '19'),
('18', '7', '40'),
('19', '2', '29'),
('20', '2', '1');


INSERT INTO `reqbaz`.`developers` 
(`Id`, `Requirement_Id`, `User_Id`) 
VALUES 
('1', '1', '1'),
('2', '1', '1'),
('3', '1', '1'),
('4', '1', '1'),
('5', '1', '1'),
('6', '1', '1'),
('7', '2', '2'),
('8', '2', '2'),
('9', '2', '2'),
('10', '2', '2'),
('11', '2', '3'),
('12', '2', '4'),
('13', '3', '5'),
('14', '3', '6'),
('15', '3', '7'),
('16', '4', '8'),
('17', '4', '9'),
('18', '4', '10'),
('19', '5', '11'),
('20', '6', '12');


INSERT INTO `reqbaz`.`tags` 
(`Id`, `Components_Id`, `Requirements_Id`) 
VALUES 
('1', '1', '1'),
('2', '1', '1'),
('3', '1', '1'),
('4', '1', '1'),
('5', '1', '1'),
('6', '1', '2'),
('7', '1', '3'),
('8', '2', '4'),
('9', '2', '5'),
('10', '2', '6'),
('11', '2', '7'),
('12', '2', '8'),
('13', '2', '9'),
('14', '4', '10'),
('15', '4', '11'),
('16', '4', '12'),
('17', '4', '13'),
('18', '4', '14'),
('19', '4', '15'),
('20', '4', '16'),
('21', '4', '17'),
('22', '4', '18'),
('23', '5', '19'),
('24', '6', '20'),
('25', '7', '21'),
('26', '8', '22'),
('27', '9', '23'),
('28', '10', '24'),
('29', '11', '25'),
('30', '1', '26'),
('31', '1', '27'),
('32', '1', '28'),
('33', '1', '29'),
('34', '2', '30'),
('35', '2', '31'),
('36', '2', '32'),
('37', '2', '33'),
('38', '2', '34'),
('39', '1', '35'),
('40', '5', '36'),
('41', '4', '37'),
('42', '5', '38'),
('43', '5', '39'),
('44', '5', '40');

INSERT INTO `roles` (`Id`, `name`) VALUES
(1, 'Anonymous'),
(2, 'LoggedInUser'),
(3, 'ProjectAdmin'),
(4, 'SystemAdmin');

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

INSERT INTO `role_role` (`Id`, `Child_Id`, `Parent_Id`) VALUES
(1, 2, 1),
(2, 3, 2),
(3, 4, 3);

INSERT INTO `user_role` (`Id`, `Roles_Id`, `Users_Id`) VALUES
(1, 1, 41);











