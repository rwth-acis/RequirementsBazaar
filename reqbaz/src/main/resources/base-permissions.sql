INSERT INTO reqbaz.role
    (id, name)
VALUES (1, 'Anonymous'),
       (2, 'LoggedInUser'),
       (3, 'ProjectAdmin'),
       (4, 'SystemAdmin'),
       (5, 'ProjectManager'),
       (6, 'ProjectMember');

INSERT INTO reqbaz.privilege
    (id, name)
VALUES (1, 'Create_PROJECT'),
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
       (26, 'Delete_DEVELOP'),
       (27, 'Read_PERSONALISATION_DATA'),
       (28, 'Create_PERSONALISATION_DATA'),
       (29, 'Read_FEEDBACK'),
       (31, 'Promote_USER'),
       (32, 'Realize_REQUIREMENT'),
       (34, 'Modify_MEMBERS'),
       (35, 'Modify_ADMIN_MEMBERS'),
       (36, 'Read_USERS');

INSERT INTO reqbaz.user
(first_name, last_name, email, las2peer_id, user_name, profile_image, email_lead_subscription,
 email_follow_subscription)
VALUES (NULL, NULL, 'anonymous@requirements-bazaar.org', '-1722613621014065292', 'anonymous',
        'https://api.learning-layers.eu/profile.png', true, true);

INSERT INTO reqbaz.user_role_map
    (role_id, user_id)
VALUES (1, 1);


INSERT INTO reqbaz.role_privilege_map
    (role_id, privilege_id)
VALUES (1, 3),
       (1, 7),
       (1, 11),
       (1, 15),
       (1, 19),

       (2, 1),
       (2, 9),
       (2, 13),
       (2, 17),
       (2, 21),
       (2, 22),
       (2, 23),
       (2, 24),
       (2, 27),
       (2, 28),
       (2, 36),

       (3, 4),
       (3, 8),
       (3, 35),

       (5, 29),
       (5, 31),
       (5, 12),
       (5, 16),
       (5, 20),
       (5, 5),
       (5, 34),

       (6, 2),
       (6, 6),
       (6, 25),
       (6, 26),
       (6, 18),
       (6, 14),
       (6, 10),
       (6, 32);

INSERT INTO reqbaz.role_role_map
    (child_id, parent_id)
VALUES (4, 3),
       (3, 5),
       (5, 6),
       (6, 2),
       (2, 1);
