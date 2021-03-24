CREATE TABLE IF NOT EXISTS reqbaz.feedback (
    id                  INT NOT NULL AUTO_INCREMENT,
    project_id          INT NOT NULL,
    requirement_id      INT,
    creation_date       TIMESTAMP NOT NULL  DEFAULT CURRENT_TIMESTAMP,
    email               VARCHAR(255),
    feedback            TEXT NOT NULL,
    CONSTRAINT feedback_project FOREIGN KEY feedback_project (project_id) REFERENCES project (id) ON DELETE CASCADE,
    CONSTRAINT feedback_pk PRIMARY KEY (id)
);
