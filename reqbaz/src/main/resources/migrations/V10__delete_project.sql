REPLACE INTO reqbaz.privilege
    (id, name)
VALUES (37, 'Delete_PROJECT');

INSERT INTO reqbaz.role_privilege_map
(role_id, privilege_id)
VALUES (3, 37);
