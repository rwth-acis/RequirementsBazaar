REPLACE INTO reqbaz.privilege
    (id, name)
VALUES (29, 'Read_FEEDBACK'),
       (31, 'Promote_USER'),
       (32, 'Realize_REQUIREMENT'),
       (34, 'Modify_MEMBERS'),
       (35, 'Modify_ADMIN_MEMBERS'),
       (36, 'Read_USERS');


REPLACE INTO reqbaz.role
    (id, name)
VALUES (5, 'ProjectManager'),
       (6, 'ProjectMember');

TRUNCATE TABLE reqbaz.role_privilege_map;
# This ist quite difficult to read
# For more info see the privileges documentation
# or run this query:
# SELECT rpm.id, r.name, p.name, p.id FROM reqbaz.role_privilege_map rpm JOIN reqbaz.role AS r on r.id = rpm.role_id JOIN reqbaz.privilege AS p ON p.id = rpm.privilege_id order by r.name asc, p.name asc;
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

TRUNCATE TABLE reqbaz.role_role_map;
INSERT INTO reqbaz.role_role_map
(child_id, parent_id)
VALUES
(4, 3),
(3, 5),
(5, 6),
(6, 2),
(2, 1);

ALTER TABLE reqbaz.user_role_map
    MODIFY context_info INT;

ALTER TABLE reqbaz.user_role_map
    ADD CONSTRAINT role_project_context FOREIGN KEY role_project_context (context_info) REFERENCES project (id)
        ON DELETE CASCADE;

ALTER TABLE reqbaz.user DROP COLUMN admin;
