SET FOREIGN_KEY_CHECKS = 0;
ALTER TABLE reqbaz.requirement_category_map
    DROP FOREIGN KEY `requirement_category_map_category`;

ALTER TABLE reqbaz.requirement_category_map
    ADD CONSTRAINT requirement_category_map_category FOREIGN KEY requirement_category_map_category (category_id) REFERENCES category (id) ON DELETE CASCADE;


ALTER TABLE reqbaz.requirement
    DROP FOREIGN KEY `requirement_project`;

ALTER TABLE reqbaz.requirement
    ADD CONSTRAINT requirement_project FOREIGN KEY requirement_project (project_id) REFERENCES project (id) ON DELETE CASCADE;


SET FOREIGN_KEY_CHECKS = 1;
