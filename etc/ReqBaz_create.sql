SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS attachment, comment, component, requirement_developer_map, requirement_follower_map, component_follower_map, project_follower_map, privilege, project, requirement, requirement_component_map, role_privilege_map, role_role_map, role, user_role_map, vote;

-- tables
-- Table attachment
CREATE TABLE IF NOT EXISTS attachment (
  id               INT           NOT NULL  AUTO_INCREMENT,
  creation_time    TIMESTAMP     NOT NULL  DEFAULT CURRENT_TIMESTAMP,
  lastupdated_time TIMESTAMP     NULL,
  requirement_id   INT           NOT NULL,
  user_id          INT           NOT NULL,
  name             VARCHAR(255)  NOT NULL,
  description      TEXT          NULL,
  mime_type        VARCHAR(255)  NOT NULL,
  identifier       VARCHAR(900)  NOT NULL,
  file_url         VARCHAR(1000) NOT NULL,
  CONSTRAINT attachment_pk PRIMARY KEY (id),
  CONSTRAINT attachment_requirement FOREIGN KEY attachment_requirement (requirement_id) REFERENCES requirement (id)
    ON DELETE CASCADE,
  CONSTRAINT attachment_user FOREIGN KEY attachment_user (user_id) REFERENCES user (id)
);

-- Table comment
CREATE TABLE IF NOT EXISTS comment (
  id                  INT       NOT NULL  AUTO_INCREMENT,
  message             TEXT      NOT NULL,
  creation_time       TIMESTAMP NOT NULL  DEFAULT CURRENT_TIMESTAMP,
  lastupdated_time    TIMESTAMP NULL,
  requirement_id      INT       NOT NULL,
  user_id             INT       NOT NULL,
  reply_to_comment_id INT,
  CONSTRAINT comment_pk PRIMARY KEY (id),
  CONSTRAINT comment_requirement FOREIGN KEY comment_requirement (requirement_id) REFERENCES requirement (id)
    ON DELETE CASCADE,
  CONSTRAINT comment_user FOREIGN KEY comment_user (user_id) REFERENCES user (id),
  CONSTRAINT reply_comment FOREIGN KEY reply_comment (reply_to_comment_id) REFERENCES comment (id)
);

-- Table component
CREATE TABLE IF NOT EXISTS component (
  id               INT          NOT NULL  AUTO_INCREMENT,
  name             VARCHAR(255) NOT NULL,
  description      TEXT         NOT NULL,
  creation_time    TIMESTAMP    NOT NULL  DEFAULT CURRENT_TIMESTAMP,
  lastupdated_time TIMESTAMP    NULL,
  project_id       INT          NOT NULL,
  leader_id        INT          NOT NULL,
  CONSTRAINT component_pk PRIMARY KEY (id),
  CONSTRAINT component_project FOREIGN KEY component_project (project_id) REFERENCES project (id),
  CONSTRAINT component_user FOREIGN KEY component_user (leader_id) REFERENCES user (id)
);

-- Table requirement_developer_map
CREATE TABLE IF NOT EXISTS requirement_developer_map (
  id             INT       NOT NULL  AUTO_INCREMENT,
  requirement_id INT       NOT NULL,
  user_id        INT       NOT NULL,
  creation_time  TIMESTAMP NOT NULL  DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT requirement_developer_map_pk PRIMARY KEY (id),
  CONSTRAINT requirement_developer_map_requirement FOREIGN KEY requirement_developer_map_requirement (requirement_id) REFERENCES requirement (id)
    ON DELETE CASCADE,
  CONSTRAINT requirement_developer_map_user FOREIGN KEY requirement_developer_map_user (user_id) REFERENCES user (id)
);

-- Table follower_requirement_map
CREATE TABLE IF NOT EXISTS requirement_follower_map (
  id             INT       NOT NULL  AUTO_INCREMENT,
  requirement_id INT       NOT NULL,
  user_id        INT       NOT NULL,
  creation_time  TIMESTAMP NOT NULL  DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT requirement_follower_map_pk PRIMARY KEY (id),
  CONSTRAINT requirement_follower_map_requirement FOREIGN KEY requirement_follower_map_requirement (requirement_id) REFERENCES requirement (id)
    ON DELETE CASCADE,
  CONSTRAINT requirement_follower_map_user FOREIGN KEY requirement_follower_user (user_id) REFERENCES user (id)
);

-- Table component_follower_map
CREATE TABLE IF NOT EXISTS component_follower_map (
  id            INT       NOT NULL  AUTO_INCREMENT,
  component_id  INT       NOT NULL,
  user_id       INT       NOT NULL,
  creation_time TIMESTAMP NOT NULL  DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT component_follower_map_pk PRIMARY KEY (id),
  CONSTRAINT component_follower_map_component FOREIGN KEY component_follower_map_component (component_id) REFERENCES component (id)
    ON DELETE CASCADE,
  CONSTRAINT component_follower_map_user FOREIGN KEY component_follower_map_user (user_id) REFERENCES user (id)
);

-- Table project_follower_map
CREATE TABLE IF NOT EXISTS project_follower_map (
  id            INT       NOT NULL  AUTO_INCREMENT,
  project_id    INT       NOT NULL,
  user_id       INT       NOT NULL,
  creation_time TIMESTAMP NOT NULL  DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT project_follower_map_pk PRIMARY KEY (id),
  CONSTRAINT project_follower_map_project FOREIGN KEY project_follower_map_project (project_id) REFERENCES project (id)
    ON DELETE CASCADE,
  CONSTRAINT project_follower_map_user FOREIGN KEY project_follower_map_user (user_id) REFERENCES user (id)
);

-- Table privilege
CREATE TABLE IF NOT EXISTS privilege (
  id   INT          NOT NULL  AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  CONSTRAINT privilege_pk PRIMARY KEY (id)
);

-- Table project
CREATE TABLE IF NOT EXISTS project (
  id                   INT          NOT NULL  AUTO_INCREMENT,
  name                 VARCHAR(255) NOT NULL,
  description          TEXT         NOT NULL,
  visibility           BOOLEAN      NOT NULL,
  creation_time        TIMESTAMP    NOT NULL  DEFAULT CURRENT_TIMESTAMP,
  lastupdated_time     TIMESTAMP    NULL,
  leader_id            INT          NOT NULL,
  default_component_id INT          NULL,
  CONSTRAINT project_pk PRIMARY KEY (id),
  CONSTRAINT project_component FOREIGN KEY project_component (default_component_id) REFERENCES component (id),
  CONSTRAINT project_user FOREIGN KEY project_user (leader_id) REFERENCES user (id)
);

-- Table requirement
CREATE TABLE IF NOT EXISTS requirement (
  id                INT          NOT NULL  AUTO_INCREMENT,
  name              VARCHAR(255) NOT NULL,
  description       TEXT         NULL,
  realized          TIMESTAMP    NULL      DEFAULT NULL,
  creation_time     TIMESTAMP    NOT NULL  DEFAULT CURRENT_TIMESTAMP,
  lastupdated_time  TIMESTAMP    NULL,
  lead_developer_id INT          NULL,
  creator_id        INT          NOT NULL,
  project_id        INT          NOT NULL,
  CONSTRAINT requirement_pk PRIMARY KEY (id),
  CONSTRAINT creator FOREIGN KEY creator (creator_id) REFERENCES user (id),
  CONSTRAINT lead_developer FOREIGN KEY lead_developer (lead_developer_id) REFERENCES user (id),
  CONSTRAINT requirement_project FOREIGN KEY requirement_project (project_id) REFERENCES project (id)
);

-- Table requirement_component_map
CREATE TABLE IF NOT EXISTS requirement_component_map (
  id             INT NOT NULL  AUTO_INCREMENT,
  component_id   INT NOT NULL,
  requirement_id INT NOT NULL,
  CONSTRAINT requirement_component_map_pk PRIMARY KEY (id),
  CONSTRAINT requirement_component_map_component FOREIGN KEY requirement_component_map_component (component_id) REFERENCES component (id),
  CONSTRAINT requirement_component_map_requirement FOREIGN KEY requirement_component_map_requirement (requirement_id) REFERENCES requirement (id)
    ON DELETE CASCADE
);

-- Table role_privilege_map
CREATE TABLE IF NOT EXISTS role_privilege_map (
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
CREATE TABLE IF NOT EXISTS role_role_map (
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
CREATE TABLE IF NOT EXISTS role (
  id   INT         NOT NULL  AUTO_INCREMENT,
  name VARCHAR(50) NULL,
  CONSTRAINT role_pk PRIMARY KEY (id),
  UNIQUE KEY role_idx_1 (name)
);

-- Table user_role_map
CREATE TABLE IF NOT EXISTS user_role_map (
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
CREATE TABLE IF NOT EXISTS user (
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
  CONSTRAINT user_pk PRIMARY KEY (id)
);

-- Table vote
CREATE TABLE IF NOT EXISTS vote (
  id             INT       NOT NULL  AUTO_INCREMENT,
  is_upvote      BOOLEAN   NOT NULL,
  requirement_id INT       NOT NULL,
  user_id        INT       NOT NULL,
  creation_time  TIMESTAMP NOT NULL  DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT vote_pk PRIMARY KEY (id),
  CONSTRAINT vote_requirement FOREIGN KEY vote_requirement (requirement_id) REFERENCES requirement (id)
    ON DELETE CASCADE,
  CONSTRAINT vote_user FOREIGN KEY vote_user (user_id) REFERENCES user (id)
);

SET FOREIGN_KEY_CHECKS = 1;