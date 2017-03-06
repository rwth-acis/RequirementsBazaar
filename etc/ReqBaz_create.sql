SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS attachment, comment, component, developer, requirement_follower, component_follower, project_follower, privilege, project, requirement, role_privilege, role_role, role, tags, user_role, requirement_component, vote;

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
  CONSTRAINT attachment_requirement FOREIGN KEY attachment_requirement (requirement_id) REFERENCES requirements (id)
    ON DELETE CASCADE,
  CONSTRAINT attachment_user FOREIGN KEY attachment_user (user_id) REFERENCES users (id)
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
  CONSTRAINT comment_requirement FOREIGN KEY comment_requirement (requirement_id) REFERENCES requirements (id)
    ON DELETE CASCADE,
  CONSTRAINT comment_user FOREIGN KEY comment_user (user_id) REFERENCES users (id),
  CONSTRAINT reply_comment FOREIGN KEY reply_comment (reply_to_comment_id) REFERENCES comments (id)
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
  CONSTRAINT component_pk PRIMARY KEY (Id),
  CONSTRAINT component_project FOREIGN KEY component_project (project_id) REFERENCES projects (id),
  CONSTRAINT components_users FOREIGN KEY components_users (leader_id) REFERENCES users (id)
);

-- Table developer
CREATE TABLE IF NOT EXISTS developer (
  id             INT       NOT NULL  AUTO_INCREMENT,
  requirement_id INT       NOT NULL,
  user_id        INT       NOT NULL,
  creation_time  TIMESTAMP NOT NULL  DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT developer_pk PRIMARY KEY (id),
  CONSTRAINT developer_requirement FOREIGN KEY developer_requirement (requirement_id) REFERENCES requirements (id)
    ON DELETE CASCADE,
  CONSTRAINT developer_user FOREIGN KEY developer_user (user_id) REFERENCES users (id)
);

-- Table follower_requirement
CREATE TABLE IF NOT EXISTS requirement_follower (
  id             INT       NOT NULL  AUTO_INCREMENT,
  requirement_id INT       NOT NULL,
  user_id        INT       NOT NULL,
  creation_time  TIMESTAMP NOT NULL  DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT requirement_follower_pk PRIMARY KEY (id),
  CONSTRAINT requirement_follower FOREIGN KEY requirement_follower (requirement_id) REFERENCES requirements (id)
    ON DELETE CASCADE,
  CONSTRAINT requirement_follower_user FOREIGN KEY requirement_follower_user (user_id) REFERENCES users (id)
);

-- Table component_follower
CREATE TABLE IF NOT EXISTS component_follower (
  id            INT       NOT NULL  AUTO_INCREMENT,
  component_id  INT       NOT NULL,
  user_id       INT       NOT NULL,
  creation_time TIMESTAMP NOT NULL  DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT component_follower_pk PRIMARY KEY (id),
  CONSTRAINT component_follower FOREIGN KEY component_follower (component_id) REFERENCES components (id)
    ON DELETE CASCADE,
  CONSTRAINT component_follower_user FOREIGN KEY component_follower_user (user_id) REFERENCES users (id)
);

-- Table project_follower
CREATE TABLE IF NOT EXISTS project_follower (
  id            INT       NOT NULL  AUTO_INCREMENT,
  project_id    INT       NOT NULL,
  user_id       INT       NOT NULL,
  creation_time TIMESTAMP NOT NULL  DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT project_follower_pk PRIMARY KEY (id),
  CONSTRAINT project_follower FOREIGN KEY project_follower (project_id) REFERENCES projects (id)
    ON DELETE CASCADE,
  CONSTRAINT project_follower_user FOREIGN KEY project_follower_user (user_id) REFERENCES users (id)
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
  CONSTRAINT project_component FOREIGN KEY project_component (default_component_id) REFERENCES components (id),
  CONSTRAINT project_user FOREIGN KEY project_user (leader_id) REFERENCES users (id)
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
  CONSTRAINT creator FOREIGN KEY creator (creator_id) REFERENCES users (id),
  CONSTRAINT lead_developer FOREIGN KEY lead_developer (Lead_developer_Id) REFERENCES users (id),
  CONSTRAINT requirement_project FOREIGN KEY requirement_project (project_id) REFERENCES projects (id)
);

-- Table role_privilege
CREATE TABLE IF NOT EXISTS role_privilege (
  id           INT NOT NULL  AUTO_INCREMENT,
  role_id      INT NOT NULL,
  privilege_id INT NOT NULL,
  CONSTRAINT role_privilege_pk PRIMARY KEY (id),
  CONSTRAINT role_privilege_privilege FOREIGN KEY role_privilege_privilege (privilege_id) REFERENCES privileges (id)
    ON DELETE CASCADE,
  CONSTRAINT role_privilege_role FOREIGN KEY role_privilege_role (role_id) REFERENCES roles (id)
    ON DELETE CASCADE
);

-- Table role_role
CREATE TABLE IF NOT EXISTS role_role (
  id        INT NOT NULL  AUTO_INCREMENT,
  child_id  INT NOT NULL,
  parent_id INT NOT NULL,
  CONSTRAINT role_role_pk PRIMARY KEY (id),
  CONSTRAINT role_child FOREIGN KEY role_child (child_id) REFERENCES roles (id)
    ON DELETE CASCADE,
  CONSTRAINT role_parent FOREIGN KEY role_parent (parent_id) REFERENCES roles (id)
    ON DELETE CASCADE
);

-- Table role
CREATE TABLE IF NOT EXISTS role (
  id   INT         NOT NULL  AUTO_INCREMENT,
  name VARCHAR(50) NULL,
  CONSTRAINT role_pk PRIMARY KEY (id),
  UNIQUE KEY role_idx_1 (name)
);

-- Table requirement_component
CREATE TABLE IF NOT EXISTS requirement_component (
  id             INT NOT NULL  AUTO_INCREMENT,
  component_id   INT NOT NULL,
  requirement_id INT NOT NULL,
  CONSTRAINT requirement_component_pk PRIMARY KEY (id),
  CONSTRAINT requirement_component_component FOREIGN KEY requirement_component_component (component_id) REFERENCES components (id),
  CONSTRAINT requirement_component_requirement FOREIGN KEY requirement_component_requirement (requirement_id) REFERENCES requirements (id)
    ON DELETE CASCADE
);

-- Table user_role
CREATE TABLE IF NOT EXISTS user_role (
  id           INT          NOT NULL  AUTO_INCREMENT,
  role_id      INT          NOT NULL,
  user_id      INT          NOT NULL,
  context_info VARCHAR(255) NULL,
  CONSTRAINT user_role_pk PRIMARY KEY (id),
  CONSTRAINT user_role_role FOREIGN KEY user_role_role (role_id) REFERENCES roles (id)
    ON DELETE CASCADE,
  CONSTRAINT user_role_user FOREIGN KEY user_role_user (user_id) REFERENCES users (id)
    ON DELETE CASCADE
);

-- Table user
CREATE TABLE IF NOT EXISTS user (
  id                 INT          NOT NULL  AUTO_INCREMENT,
  first_name         VARCHAR(150) NULL,
  last_name          VARCHAR(150) NULL,
  email              VARCHAR(255) NOT NULL,
  admin              BOOLEAN      NOT NULL,
  las2peer_id        BIGINT       NOT NULL,
  user_name          VARCHAR(255) NULL,
  profile_image      TEXT         NULL,
  email_lead_items   BOOLEAN      NOT NULL  DEFAULT TRUE,
  email_follow_items BOOLEAN      NOT NULL  DEFAULT TRUE,
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
  CONSTRAINT vote_requirement FOREIGN KEY vote_requirement (requirement_id) REFERENCES requirements (id)
    ON DELETE CASCADE,
  CONSTRAINT vote_user FOREIGN KEY vote_user (user_id) REFERENCES users (id)
);

SET FOREIGN_KEY_CHECKS = 1;