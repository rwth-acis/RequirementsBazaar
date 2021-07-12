SET FOREIGN_KEY_CHECKS = 0;
ALTER TABLE reqbaz.category
    DROP FOREIGN KEY `category_project`;

ALTER TABLE reqbaz.category
    ADD CONSTRAINT category_project FOREIGN KEY category_project (project_id) REFERENCES project (id)
        ON DELETE CASCADE;

ALTER TABLE reqbaz.project
    DROP FOREIGN KEY `project_category`;

ALTER TABLE reqbaz.project
    ADD CONSTRAINT project_category FOREIGN KEY project_category (default_category_id) REFERENCES category (id)
        ON DELETE CASCADE;
SET FOREIGN_KEY_CHECKS = 1;
