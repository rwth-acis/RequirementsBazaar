SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE reqbaz.user;
TRUNCATE TABLE reqbaz.project;
TRUNCATE TABLE reqbaz.category;
TRUNCATE TABLE reqbaz.requirement;
TRUNCATE TABLE reqbaz.requirement_category_map;
TRUNCATE TABLE reqbaz.project_follower_map;
TRUNCATE TABLE reqbaz.category_follower_map;
TRUNCATE TABLE reqbaz.requirement_follower_map;
TRUNCATE TABLE reqbaz.requirement_developer_map;
TRUNCATE TABLE reqbaz.comment;
TRUNCATE TABLE reqbaz.attachment;
TRUNCATE TABLE reqbaz.vote;
TRUNCATE TABLE reqbaz.role;
TRUNCATE TABLE reqbaz.privilege;
TRUNCATE TABLE reqbaz.role_privilege_map;
TRUNCATE TABLE reqbaz.role_role_map;
TRUNCATE TABLE reqbaz.user_role_map;

REPLACE INTO reqbaz.user
(id, first_name, last_name, email, admin, las2peer_id, user_name, profile_image, email_lead_subscription, email_follow_subscription)
VALUES
  (1, NULL, NULL, 'anonymous@requirements-bazaar.org', 0, 'anonymous', 'anonymous',
   'https://api.learning-layers.eu/profile.png', 0, 0),
  (2, 'Max1', 'Mustermann1', 'Max@Mustermann1.de', 1, 1, 'MaxMustermann1', 'https://api.learning-layers.eu/profile.png',
   0, 0),
  (3, 'Max2', 'Mustermann2', 'Max@Mustermann2.de', 1, 2, 'MaxMustermann2', 'https://api.learning-layers.eu/profile.png',
   0, 0),
  (4, 'Max3', 'Mustermann3', 'Max@Mustermann3.de', 1, 3, 'MaxMustermann3', 'https://api.learning-layers.eu/profile.png',
   0, 0),
  (5, 'Max4', 'Mustermann4', 'Max@Mustermann4.de', 1, 4, 'MaxMustermann4', 'https://api.learning-layers.eu/profile.png',
   0, 0);

REPLACE INTO reqbaz.project
(id, name, description, visibility, leader_id, default_category_id)
VALUES
  (1, 'Project 1 - public', 'Project 1 - This project is public - Leader MaxMustermann1', true, 2,
   '1'),
  (2, 'Project 2 - public', 'Project 2 - This project is public - Leader MaxMustermann2', true, 3,
   '4'),
  (3, 'Project 3 - private', 'Project 3 - This project is private - Leader MaxMustermann3', false, 4,
   5);

REPLACE INTO reqbaz.category
(id, name, description, project_id, leader_id)
VALUES
  (1, 'Category 1', 'Category 1 - 1. Category of Project 1', '1 ', 2),
  (2, 'Category 2', 'Category 2 - 2. Category of Project 1', 1, 2),
  (3, 'Category 3', 'Category 3 - 3. Category of Project 1', 1, 3),
  (4, 'Category 4', 'Category 4 - 1. Category of Project 2', 2, 4),
  (5, 'Category 5', 'Category 5 - 1. Category of Project 3', 3, 4);

REPLACE INTO reqbaz.requirement
(id, name, description, realized, lead_developer_id, creator_id, project_id)
VALUES
  (1, 'Requirement 1', 'Requirement 1 - 1. Requirement of Category 1', NULL, 1, 1, 1),
  (2, 'Requirement 2', 'Requirement 2 - 2. Requirement of Category 1', NULL, NULL, 2, 1),
  (3, 'Requirement 3', 'Requirement 3 - 1. Requirement of Category 2', NULL, 2, 3, 1),
  (4, 'Requirement 4', 'Requirement 4 - 1. Requirement of Category 3', NULL, NULL, 4, 1),
  (5, 'Requirement 5 - realized', 'Requirement 5 - 1. realized Requirement of Category 3', NOW(), 2, 4, 1),
  (6, 'Requirement 6 - realized', 'Requirement 6 - 1. Requirement of Category 4', NOW(), NULL, 4, 2),
  (7, 'Requirement 7', 'Requirement 7 - 1. Requirement of Category 5', NULL, NULL, 4, 5),
  (8, 'Requirement 8', 'Requirement 8 - 3. Requirement of Category 1 and 2. of Category 2', NULL, 2, 4, 1);

REPLACE INTO reqbaz.requirement_category_map
(id, category_id, requirement_id)
VALUES
  (1, 1, 1),
  (2, 1, 2),
  (3, 2, 3),
  (4, 3, 4),
  (5, 3, 5),
  (6, 4, 6),
  (6, 5, 7),
  (7, 1, 8),
  (8, 2, 8);

REPLACE INTO reqbaz.project_follower_map
(id, project_id, user_id)
VALUES
  (1, 1, 2),
  (2, 1, 3),
  (3, 2, 4);

REPLACE INTO reqbaz.category_follower_map
(id, category_id, user_id)
VALUES
  (1, 1, 3),
  (2, 1, 4),
  (3, 2, 5);

REPLACE INTO reqbaz.requirement_follower_map
(id, requirement_id, user_id)
VALUES
  (1, 1, 2),
  (2, 1, 4),
  (3, 2, 5);

REPLACE INTO reqbaz.requirement_developer_map
(id, requirement_id, user_id)
VALUES
  (1, 1, 2),
  (2, 1, 3),
  (3, 2, 3),
  (4, 3, 4);

REPLACE INTO reqbaz.comment
(id, message, requirement_id, reply_to_comment_id, user_id)
VALUES
  (1, 'Comment 1 - 1. Comment of Requirement 1', 1, NULL, 2),
  (2, 'Comment 2 - 1. Comment of Requirement 2', 2, NULL, 2),
  (3, 'Comment 3 - 2. Comment of Requirement 2', 2, 2, 2),
  (4, 'Comment 4 - 1. Comment of Requirement 3', 3, NULL, 2),
  (5, 'Comment 5 - 1. Comment of Requirement 4', 4, NULL, 3),
  (6, 'Comment 6 - 2. Comment of Requirement 4', 4, 1, 5);

REPLACE INTO reqbaz.attachment
(id, requirement_id, user_id, name, description, mime_type, identifier, file_url)
VALUES
  (1, 1, 2, 'Image 1', 'Image Attachment 1 - 1. Attachment of Requirement 1', 'image/jpeg', 'affe1',
   'https://localhost:8080/fileservice/affe1'),
  (2, 1, 3, 'Image 2', 'Image Attachment 2 - 2. Attachment of Requirement 1', 'image/jpeg', 'affe2',
   'https://localhost:8080/fileservice/affe2'),
  (3, 2, 4, 'Image 3', 'Image Attachment 3 - 1. Attachment of Requirement 2', 'image/jpeg', 'affe3',
   'https://localhost:8080/fileservice/affe3');

REPLACE INTO reqbaz.vote
(id, is_upvote, requirement_id, user_id)
VALUES
  (1, 1, 1, 2),
  (2, 1, 2, 3),
  (3, 1, 2, 2),
  (4, 1, 3, 4),
  (5, 1, 4, 4);

-- roles and privilege schema is already set up with V1__create_reqbaz_schema
REPLACE INTO reqbaz.role
(id, name)
VALUES
  (1, 'Anonymous'),
  (2, 'LoggedInUser'),
  (3, 'ProjectAdmin'),
  (4, 'SystemAdmin');

REPLACE INTO reqbaz.privilege
(id, name)
VALUES
  (1, 'Create_PROJECT'),
  (2, 'Read_PROJECT'),
  (3, 'Read_PUBLIC_PROJECT'),
  (4, 'Modify_PROJECT'),
  (5, 'Create_CATEGORY'),
  (6, 'Read_CATEGORY'),
  (7, 'Read_PUBLIC_CATEGORY'),
  (8, 'Modify_CATEGORY'),
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

REPLACE INTO reqbaz.role_privilege_map
(id, role_id, privilege_id)
VALUES
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

REPLACE INTO reqbaz.role_role_map
(id, child_id, parent_id)
VALUES
  (1, 2, 1),
  (2, 3, 2),
  (3, 4, 3);

REPLACE INTO reqbaz.user_role_map
(id, role_id, user_id)
VALUES
  (1, 1, 1);

SET FOREIGN_KEY_CHECKS = 1;