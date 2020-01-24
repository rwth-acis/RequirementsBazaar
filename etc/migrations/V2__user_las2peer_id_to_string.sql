SET FOREIGN_KEY_CHECKS = 0;

ALTER TABLE reqbaz.user change las2peer_id las2peer_id VARCHAR(128);

UPDATE reqbaz.user SET las2peer_id = "anonymous" where id = 1;

SET FOREIGN_KEY_CHECKS = 1;