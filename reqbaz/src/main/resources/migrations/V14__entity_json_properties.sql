ALTER TABLE reqbaz.project
    ADD COLUMN additional_properties JSON;

ALTER TABLE reqbaz.category
    ADD COLUMN additional_properties JSON;

ALTER TABLE reqbaz.requirement
    ADD COLUMN additional_properties JSON;
