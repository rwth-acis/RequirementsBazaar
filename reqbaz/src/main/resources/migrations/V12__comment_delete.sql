
SET FOREIGN_KEY_CHECKS = 0;
ALTER TABLE reqbaz.comment
    DROP FOREIGN KEY `reply_comment`;

ALTER TABLE reqbaz.comment
    ADD deleted BOOLEAN NOT NULL DEFAULT FALSE,
    ADD CONSTRAINT reply_comment FOREIGN KEY reply_comment (reply_to_comment_id) REFERENCES comment (id)
    ON DELETE CASCADE;

SET FOREIGN_KEY_CHECKS = 1;
