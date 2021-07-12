CREATE TABLE IF NOT EXISTS reqbaz.tag
(
    id            INT         NOT NULL AUTO_INCREMENT,
    project_id    INT         NOT NULL,
    creation_date TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    name          VARCHAR(20) NOT NULL,
    colour        VARCHAR(7)  NOT NULL,
    CONSTRAINT tag_project FOREIGN KEY tag_project (project_id) REFERENCES project (id) ON DELETE CASCADE,
    CONSTRAINT tag_pk PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS reqbaz.requirement_tag_map
(
    id             INT NOT NULL AUTO_INCREMENT,
    tag_id         INT NOT NULL,
    requirement_id INT NOT NULL,
    CONSTRAINT requirement_tag_map_pk PRIMARY KEY (id),
    CONSTRAINT requirement_tag_map_requirement FOREIGN KEY requirement_tag_map_requirement (requirement_id) REFERENCES requirement (id)
        ON DELETE CASCADE,
    CONSTRAINT requirement_tag_map_tag FOREIGN KEY requirement_tag_map_tag (tag_id) REFERENCES tag (id)
        ON DELETE CASCADE
);
