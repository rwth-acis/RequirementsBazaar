CREATE TABLE IF NOT EXISTS reqbaz.personalisation_data (
    id                  INT       NOT NULL  AUTO_INCREMENT,
    creation_date       TIMESTAMP NOT NULL  DEFAULT CURRENT_TIMESTAMP,
    last_updated_date   TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    identifier          VARCHAR(50)      NOT NULL,
    user_id             INT       NOT NULL,
    version             INT       NOT NULL,
    setting             TEXT,
    CONSTRAINT personalisation_pk PRIMARY KEY (id),
    CONSTRAINT personalisation_key  UNIQUE KEY  (identifier,user_id,version),
    CONSTRAINT personalisation_user FOREIGN KEY personalisation_user (user_id) REFERENCES user (id)
);

REPLACE INTO reqbaz.privilege
(id, name)
VALUES
(27, 'Read_PERSONALISATION_DATA'),
(28, 'Create_PERSONALISATION_DATA');


REPLACE INTO reqbaz.role_privilege_map
(id, role_id, privilege_id)
VALUES
(31, 2, 27),
(32, 2, 28);