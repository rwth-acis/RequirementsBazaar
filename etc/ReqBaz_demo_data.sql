INSERT INTO `reqbaz`.`users` 
(`Id`,`first_name`, `last_name`, `email`, `admin`, `Las2peer_Id`, `user_name`, `profile_image`)
 VALUES 
('41',NULL, NULL, 'anonymous@requirements-bazaar.org', '0', '-1722613621014065292', 'anonymous','https://api.learning-layers.eu/profile.png');


INSERT INTO `reqbaz`.`projects` 
(`Id`,`name`, `description`, `visibility`, `Leader_Id`, `Default_Components_Id`) 
VALUES
('1','Layers', 'This is everything about Layers', '+', '42', NULL),
('2','Requirements Bazaar', 'This project is about Requirement Bazaar', '+', '42',NULL);



INSERT INTO `reqbaz`.`components` 
(`Id`,`name`, `description`, `Project_Id`, `Leader_Id`) 
VALUES 
('1','Layers Box', 'This is a play box component for the Layers project.', '1 ', '42'),
('2','WebApp', 'Post your requirements about the Requirement Bazaar WebApp under this component', '2', '42');

UPDATE `reqbaz`.`projects` SET `Default_Components_Id`='1' WHERE `Id`='1 '; 
UPDATE `reqbaz`.`projects` SET `Default_Components_Id`='2' WHERE `Id`='2 '; 


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
(20, 'Modify_ATTACHMENT'),
(21, 'Create_VOTE'),
(22, 'Delete_VOTE'),
(23, 'Create_FOLLOW'),
(24, 'Delete_FOLLOW'),
(25, 'Create_DEVELOP'),
(26, 'Delete_DEVELOP');
	

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
(24, 4, 20),
(25, 4, 21),
(26, 4, 22),
(27, 4, 23),
(28, 4, 24),
(29, 4, 25),
(30, 4, 26);


INSERT INTO `role_role` (`Id`, `Child_Id`, `Parent_Id`) VALUES
(1, 2, 1),
(2, 3, 2),
(3, 4, 3);

INSERT INTO `user_role` (`Id`, `Roles_Id`, `Users_Id`) VALUES
(1, 1, 41);











