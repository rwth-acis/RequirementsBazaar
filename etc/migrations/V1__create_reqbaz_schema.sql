SET FOREIGN_KEY_CHECKS = 0;

-- tables
-- Table attachment
CREATE TABLE IF NOT EXISTS reqbaz.attachment (
  id                INT           NOT NULL  AUTO_INCREMENT,
  creation_date     TIMESTAMP     NOT NULL  DEFAULT CURRENT_TIMESTAMP,
  last_updated_date TIMESTAMP     NULL,
  requirement_id    INT           NOT NULL,
  user_id           INT           NOT NULL,
  name              VARCHAR(255)  NOT NULL,
  description       TEXT          NULL,
  mime_type         VARCHAR(255)  NOT NULL,
  identifier        VARCHAR(900)  NOT NULL,
  file_url          VARCHAR(1000) NOT NULL,
  CONSTRAINT attachment_pk PRIMARY KEY (id),
  CONSTRAINT attachment_requirement FOREIGN KEY attachment_requirement (requirement_id) REFERENCES requirement (id)
    ON DELETE CASCADE,
  CONSTRAINT attachment_user FOREIGN KEY attachment_user (user_id) REFERENCES user (id)
);

-- Table comment
CREATE TABLE IF NOT EXISTS reqbaz.comment (
  id                  INT       NOT NULL  AUTO_INCREMENT,
  message             TEXT      NOT NULL,
  creation_date       TIMESTAMP NOT NULL  DEFAULT CURRENT_TIMESTAMP,
  last_updated_date   TIMESTAMP NULL,
  requirement_id      INT       NOT NULL,
  user_id             INT       NOT NULL,
  reply_to_comment_id INT,
  CONSTRAINT comment_pk PRIMARY KEY (id),
  CONSTRAINT comment_requirement FOREIGN KEY comment_requirement (requirement_id) REFERENCES requirement (id)
    ON DELETE CASCADE,
  CONSTRAINT comment_user FOREIGN KEY comment_user (user_id) REFERENCES user (id),
  CONSTRAINT reply_comment FOREIGN KEY reply_comment (reply_to_comment_id) REFERENCES comment (id)
);

-- Table category
CREATE TABLE IF NOT EXISTS reqbaz.category (
  id                INT          NOT NULL  AUTO_INCREMENT,
  name              VARCHAR(255) NOT NULL,
  description       TEXT         NOT NULL,
  creation_date     TIMESTAMP    NOT NULL  DEFAULT CURRENT_TIMESTAMP,
  last_updated_date TIMESTAMP    NULL,
  project_id        INT          NOT NULL,
  leader_id         INT          NOT NULL,
  CONSTRAINT category_pk PRIMARY KEY (id),
  CONSTRAINT category_project FOREIGN KEY category_project (project_id) REFERENCES project (id),
  CONSTRAINT category_user FOREIGN KEY category_user (leader_id) REFERENCES user (id)
);

-- Table requirement_developer_map
CREATE TABLE IF NOT EXISTS reqbaz.requirement_developer_map (
  id             INT       NOT NULL  AUTO_INCREMENT,
  requirement_id INT       NOT NULL,
  user_id        INT       NOT NULL,
  creation_date  TIMESTAMP NOT NULL  DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT requirement_developer_map_pk PRIMARY KEY (id),
  CONSTRAINT requirement_developer_map_requirement FOREIGN KEY requirement_developer_map_requirement (requirement_id) REFERENCES requirement (id)
    ON DELETE CASCADE,
  CONSTRAINT requirement_developer_map_user FOREIGN KEY requirement_developer_map_user (user_id) REFERENCES user (id)
);

-- Table follower_requirement_map
CREATE TABLE IF NOT EXISTS reqbaz.requirement_follower_map (
  id             INT       NOT NULL  AUTO_INCREMENT,
  requirement_id INT       NOT NULL,
  user_id        INT       NOT NULL,
  creation_date  TIMESTAMP NOT NULL  DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT requirement_follower_map_pk PRIMARY KEY (id),
  CONSTRAINT requirement_follower_map_requirement FOREIGN KEY requirement_follower_map_requirement (requirement_id) REFERENCES requirement (id)
    ON DELETE CASCADE,
  CONSTRAINT requirement_follower_map_user FOREIGN KEY requirement_follower_user (user_id) REFERENCES user (id)
);

-- Table category_follower_map
CREATE TABLE IF NOT EXISTS reqbaz.category_follower_map (
  id            INT       NOT NULL  AUTO_INCREMENT,
  category_id   INT       NOT NULL,
  user_id       INT       NOT NULL,
  creation_date TIMESTAMP NOT NULL  DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT category_follower_map_pk PRIMARY KEY (id),
  CONSTRAINT category_follower_map_category FOREIGN KEY category_follower_map_category (category_id) REFERENCES category (id)
    ON DELETE CASCADE,
  CONSTRAINT category_follower_map_user FOREIGN KEY category_follower_map_user (user_id) REFERENCES user (id)
);

-- Table project_follower_map
CREATE TABLE IF NOT EXISTS reqbaz.project_follower_map (
  id            INT       NOT NULL  AUTO_INCREMENT,
  project_id    INT       NOT NULL,
  user_id       INT       NOT NULL,
  creation_date TIMESTAMP NOT NULL  DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT project_follower_map_pk PRIMARY KEY (id),
  CONSTRAINT project_follower_map_project FOREIGN KEY project_follower_map_project (project_id) REFERENCES project (id)
    ON DELETE CASCADE,
  CONSTRAINT project_follower_map_user FOREIGN KEY project_follower_map_user (user_id) REFERENCES user (id)
);

-- Table privilege
CREATE TABLE IF NOT EXISTS reqbaz.privilege (
  id   INT          NOT NULL  AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  CONSTRAINT privilege_pk PRIMARY KEY (id)
);

-- Table project
CREATE TABLE IF NOT EXISTS reqbaz.project (
  id                  INT          NOT NULL  AUTO_INCREMENT,
  name                VARCHAR(255) NOT NULL,
  description         TEXT         NOT NULL,
  visibility          BOOLEAN      NOT NULL,
  creation_date       TIMESTAMP    NOT NULL  DEFAULT CURRENT_TIMESTAMP,
  last_updated_date   TIMESTAMP    NULL,
  leader_id           INT          NOT NULL,
  default_category_id INT          NULL,
  CONSTRAINT project_pk PRIMARY KEY (id),
  CONSTRAINT project_category FOREIGN KEY project_category (default_category_id) REFERENCES category (id),
  CONSTRAINT project_user FOREIGN KEY project_user (leader_id) REFERENCES user (id)
);

-- Table requirement
CREATE TABLE IF NOT EXISTS reqbaz.requirement (
  id                INT          NOT NULL  AUTO_INCREMENT,
  name              VARCHAR(255) NOT NULL,
  description       TEXT         NULL,
  realized          TIMESTAMP    NULL      DEFAULT NULL,
  creation_date     TIMESTAMP    NOT NULL  DEFAULT CURRENT_TIMESTAMP,
  last_updated_date TIMESTAMP    NULL,
  lead_developer_id INT          NULL,
  creator_id        INT          NOT NULL,
  project_id        INT          NOT NULL,
  CONSTRAINT requirement_pk PRIMARY KEY (id),
  CONSTRAINT creator FOREIGN KEY creator (creator_id) REFERENCES user (id),
  CONSTRAINT lead_developer FOREIGN KEY lead_developer (lead_developer_id) REFERENCES user (id),
  CONSTRAINT requirement_project FOREIGN KEY requirement_project (project_id) REFERENCES project (id)
);

-- Table requirement_category_map
CREATE TABLE IF NOT EXISTS reqbaz.requirement_category_map (
  id             INT NOT NULL  AUTO_INCREMENT,
  category_id    INT NOT NULL,
  requirement_id INT NOT NULL,
  CONSTRAINT requirement_category_map_pk PRIMARY KEY (id),
  CONSTRAINT requirement_category_map_category FOREIGN KEY requirement_category_map_category (category_id) REFERENCES category (id),
  CONSTRAINT requirement_category_map_requirement FOREIGN KEY requirement_category_map_requirement (requirement_id) REFERENCES requirement (id)
    ON DELETE CASCADE
);

-- Table role_privilege_map
CREATE TABLE IF NOT EXISTS reqbaz.role_privilege_map (
  id           INT NOT NULL  AUTO_INCREMENT,
  role_id      INT NOT NULL,
  privilege_id INT NOT NULL,
  CONSTRAINT role_privilege_map_pk PRIMARY KEY (id),
  CONSTRAINT role_privilege_map_privilege FOREIGN KEY role_privilege_map_privilege (privilege_id) REFERENCES privilege (id)
    ON DELETE CASCADE,
  CONSTRAINT role_privilege_map_role FOREIGN KEY role_privilege_map_role (role_id) REFERENCES role (id)
    ON DELETE CASCADE
);

-- Table role_role_map
CREATE TABLE IF NOT EXISTS reqbaz.role_role_map (
  id        INT NOT NULL  AUTO_INCREMENT,
  child_id  INT NOT NULL,
  parent_id INT NOT NULL,
  CONSTRAINT role_role_map_pk PRIMARY KEY (id),
  CONSTRAINT role_role_map_child FOREIGN KEY role_role_map_child (child_id) REFERENCES role (id)
    ON DELETE CASCADE,
  CONSTRAINT role_role_map_parent FOREIGN KEY role_role_map_parent (parent_id) REFERENCES role (id)
    ON DELETE CASCADE
);

-- Table role
CREATE TABLE IF NOT EXISTS reqbaz.role (
  id   INT         NOT NULL  AUTO_INCREMENT,
  name VARCHAR(50) NULL,
  CONSTRAINT role_pk PRIMARY KEY (id),
  UNIQUE KEY role_idx_1 (name)
);

-- Table user_role_map
CREATE TABLE IF NOT EXISTS reqbaz.user_role_map (
  id           INT          NOT NULL  AUTO_INCREMENT,
  role_id      INT          NOT NULL,
  user_id      INT          NOT NULL,
  context_info VARCHAR(255) NULL,
  CONSTRAINT user_role_map_pk PRIMARY KEY (id),
  CONSTRAINT user_role_map_role FOREIGN KEY user_role_map_role (role_id) REFERENCES role (id)
    ON DELETE CASCADE,
  CONSTRAINT user_role_map_user FOREIGN KEY user_role_map_user (user_id) REFERENCES user (id)
    ON DELETE CASCADE
);

-- Table user
CREATE TABLE IF NOT EXISTS reqbaz.user (
  id                        INT          NOT NULL  AUTO_INCREMENT,
  first_name                VARCHAR(150) NULL,
  last_name                 VARCHAR(150) NULL,
  email                     VARCHAR(255) NOT NULL,
  admin                     BOOLEAN      NOT NULL,
  las2peer_id               BIGINT       NOT NULL,
  user_name                 VARCHAR(255) NULL,
  profile_image             TEXT         NULL,
  email_lead_subscription   BOOLEAN      NOT NULL  DEFAULT TRUE,
  email_follow_subscription BOOLEAN      NOT NULL  DEFAULT TRUE,
  creation_date             TIMESTAMP    NOT NULL  DEFAULT CURRENT_TIMESTAMP,
  last_updated_date         TIMESTAMP    NULL,
  last_login_date           TIMESTAMP    NULL,
  CONSTRAINT user_pk PRIMARY KEY (id),
  UNIQUE KEY las2peer_idx (las2peer_id)
);

-- Table vote
CREATE TABLE IF NOT EXISTS reqbaz.vote (
  id             INT       NOT NULL  AUTO_INCREMENT,
  is_upvote      BOOLEAN   NOT NULL,
  requirement_id INT       NOT NULL,
  user_id        INT       NOT NULL,
  creation_date  TIMESTAMP NOT NULL  DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT vote_pk PRIMARY KEY (id),
  CONSTRAINT vote_requirement FOREIGN KEY vote_requirement (requirement_id) REFERENCES requirement (id)
    ON DELETE CASCADE,
  CONSTRAINT vote_user FOREIGN KEY vote_user (user_id) REFERENCES user (id)
);

-- Fill roles and privileges
REPLACE INTO reqbaz.role
(id, name)
VALUES
  (1, 'Anonymous'),
  (2, 'LoggedInUser'),
  (3, 'ProjectAdmin'),
  (4, 'SystemAdmin');

REPLACE INTO reqbaz.privilege
(id, name)
VALUES
  (1, 'Create_PROJECT'),
  (2, 'Read_PROJECT'),
  (3, 'Read_PUBLIC_PROJECT'),
  (4, 'Modify_PROJECT'),
  (5, 'Create_CATEGORY'),
  (6, 'Read_CATEGORY'),
  (7, 'Read_PUBLIC_CATEGORY'),
  (8, 'Modify_CATEGORY'),
  (9, 'Create_REQUIREMENT'),
  (10, 'Read_REQUIREMENT'),
  (11, 'Read_PUBLIC_REQUIREMENT'),
  (12, 'Modify_REQUIREMENT'),
  (13, 'Create_COMMENT'),
  (14, 'Read_COMMENT'),
  (15, 'Read_PUBLIC_COMMENT'),
  (16, 'Modify_COMMENT'),
  (17, 'Create_ATTACHMENT'),
  (18, 'Read_ATTACHMENT'),
  (19, 'Read_PUBLIC_ATTACHMENT'),
  (20, 'Modify_ATTACHMENT'),
  (21, 'Create_VOTE'),
  (22, 'Delete_VOTE'),
  (23, 'Create_FOLLOW'),
  (24, 'Delete_FOLLOW'),
  (25, 'Create_DEVELOP'),
  (26, 'Delete_DEVELOP');

REPLACE INTO reqbaz.role_privilege_map
(id, role_id, privilege_id)
VALUES
  (1, 1, 3),
  (2, 1, 7),
  (3, 1, 11),
  (4, 1, 15),
  (5, 1, 19),
  (6, 4, 1),
  (7, 4, 2),
  (8, 4, 8),
  (9, 4, 7),
  (10, 4, 6),
  (11, 4, 5),
  (12, 4, 3),
  (13, 4, 4),
  (14, 4, 9),
  (15, 4, 10),
  (16, 4, 11),
  (17, 4, 12),
  (18, 4, 13),
  (19, 4, 14),
  (20, 4, 16),
  (21, 4, 17),
  (22, 4, 18),
  (23, 4, 19),
  (24, 4, 20),
  (25, 4, 21),
  (26, 4, 22),
  (27, 4, 23),
  (28, 4, 24),
  (29, 4, 25),
  (30, 4, 26);

REPLACE INTO reqbaz.role_role_map
(id, child_id, parent_id)
VALUES
  (1, 2, 1),
  (2, 3, 2),
  (3, 4, 3);

REPLACE INTO reqbaz.user_role_map
(id, role_id, user_id)
VALUES
  (1, 1, 1);

SET FOREIGN_KEY_CHECKS = 1;