SET FOREIGN_KEY_CHECKS = 0;

REPLACE INTO `reqbaz`.`users`
(`Id`, `first_name`, `last_name`, `email`, `admin`, `Las2peer_Id`, `user_name`, `profile_image`, `email_lead_items`, `email_follow_items`)
VALUES
  ('1', NULL, NULL, 'anonymous@requirements-bazaar.org', '0', '-1722613621014065292', 'anonymous',
   'https://api.learning-layers.eu/profile.png', '0', '0'),
  ('2', 'Max1', 'Mustermann1', 'Max@Mustermann1.de', '1', '1', 'MaxMustermann1',
   'https://api.learning-layers.eu/profile.png', '0', '0'),
  ('3', 'Max2', 'Mustermann2', 'Max@Mustermann2.de', '1', '2', 'MaxMustermann2',
   'https://api.learning-layers.eu/profile.png', '0', '0'),
  ('4', 'Max3', 'Mustermann3', 'Max@Mustermann3.de', '1', '3', 'MaxMustermann3',
   'https://api.learning-layers.eu/profile.png', '0', '0'),
  ('5', 'Max4', 'Mustermann4', 'Max@Mustermann4.de', '1', '4', 'MaxMustermann4',
   'https://api.learning-layers.eu/profile.png', '0', '0');

REPLACE INTO `reqbaz`.`projects`
(`Id`, `name`, `description`, `visibility`, `Leader_Id`, `Default_Components_Id`)
VALUES
  ('1', 'Project 1', 'Project 1 - Description - This project is visible - leader MaxMustermann1', '+', '1', '1'),
  ('2', 'Project 2', 'Project 2 - Description - This project is visible - leader MaxMustermann2', '+', '2', '4');

REPLACE INTO `reqbaz`.`components`
(`Id`, `name`, `description`, `Project_Id`, `Leader_Id`)
VALUES
  ('1', 'Component 1', 'Component 1 for Project 1', '1 ', '1'),
  ('2', 'Component 2', 'Component 2 for Project 1', '1', '2'),
  ('3', 'Component 3', 'Component 3 for Project 1', '1', '3'),
  ('4', 'Component 4', 'Component 4 for Project 2', '1', '4');

REPLACE INTO `reqbaz`.`requirements`
(`Id`, `title`, `description`, `realized`, `Lead_developer_Id`, `Creator_Id`, `Project_Id`)
VALUES
  ('1', 'Requirement 1', 'Requirement - Description', NULL, '1', '1', '1'),
  ('2', 'Requirement 2', 'Requirement - Description', NULL, '1', '2', '1'),
  ('3', 'Requirement 3', 'Requirement - Description', NULL, '2', '3', '1'),
  ('4', 'Requirement 4', 'Requirement - Description', NULL, '2', '4', '2');

REPLACE INTO `reqbaz`.`project_follower`
(`Id`, `Project_Id`, `User_Id`)
VALUES
  ('1', '1', '1'),
  ('2', '1', '2'),
  ('3', '2', '3');

REPLACE INTO `reqbaz`.`component_follower`
(`Id`, `Component_Id`, `User_Id`)
VALUES
  ('1', '1', '3'),
  ('2', '1', '4'),
  ('3', '2', '5');

REPLACE INTO `reqbaz`.`requirement_follower`
(`Id`, `Requirement_Id`, `User_Id`)
VALUES
  ('1', '1', '1'),
  ('2', '1', '4'),
  ('3', '2', '5');

REPLACE INTO `reqbaz`.`developers`
(`Id`, `Requirement_Id`, `User_Id`)
VALUES
  ('1', '1', '1'),
  ('2', '1', '2');

REPLACE INTO `reqbaz`.`tags`
(`Id`, `Components_Id`, `Requirements_Id`)
VALUES
  ('1', '1', '1'),
  ('2', '1', '2'),
  ('3', '2', '3'),
  ('4', '4', '4');

REPLACE INTO `reqbaz`.`comments`
(`Id`, `message`, `Requirement_Id`, `User_Id`)
VALUES
  ('1', 'Comment', '1', '1'),
  ('2', 'Comment', '2', '2'),
  ('3', 'Comment', '2', '1'),
  ('4', 'Comment', '3', '2'),
  ('5', 'Comment', '4', '2'),
  ('6', 'Comment', '4', '5');

REPLACE INTO `reqbaz`.`votes`
(`Id`, `is_upvote`, `Requirement_Id`, `User_Id`)
VALUES
  ('1', '1', '1', '1'),
  ('2', '1', '2', '3'),
  ('3', '1', '2', '2'),
  ('4', '1', '3', '4');

REPLACE INTO `roles` (`Id`, `name`) VALUES
  (1, 'Anonymous'),
  (2, 'LoggedInUser'),
  (3, 'ProjectAdmin'),
  (4, 'SystemAdmin');

REPLACE INTO `privileges` (`Id`, `name`) VALUES
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

REPLACE INTO `role_privilege` (`Id`, `Roles_Id`, `Privileges_Id`) VALUES
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

REPLACE INTO `role_role` (`Id`, `Child_Id`, `Parent_Id`) VALUES
  (1, 2, 1),
  (2, 3, 2),
  (3, 4, 3);

REPLACE INTO `user_role` (`Id`, `Roles_Id`, `Users_Id`) VALUES
  (1, 1, 1);

SET FOREIGN_KEY_CHECKS = 1;









