SET FOREIGN_KEY_CHECKS = 0;

-- tables
-- Table attachments
CREATE TABLE IF NOT EXISTS attachments (
  Id               INT          NOT NULL  AUTO_INCREMENT,
  creation_time    TIMESTAMP    NOT NULL  DEFAULT CURRENT_TIMESTAMP,
  lastupdated_time TIMESTAMP    NULL,
  Requirement_Id   INT          NOT NULL,
  User_Id          INT          NOT NULL,
  title            VARCHAR(255) NOT NULL,
  description      TEXT         NULL,
  mime_type        VARCHAR(255) NOT NULL,
  identifier       VARCHAR(900) NOT NULL,
  fileUrl          VARCHAR(1000) NOT NULL,
  CONSTRAINT attachments_pk PRIMARY KEY (Id),
  CONSTRAINT Attachement_Requirement FOREIGN KEY Attachement_Requirement (Requirement_Id) REFERENCES requirements (Id)
    ON DELETE CASCADE,
  CONSTRAINT Attachement_User FOREIGN KEY Attachement_User (User_Id) REFERENCES users (Id)
);

-- Table comments
CREATE TABLE IF NOT EXISTS comments (
  Id               INT       NOT NULL  AUTO_INCREMENT,
  message          TEXT      NOT NULL,
  creation_time    TIMESTAMP NOT NULL  DEFAULT CURRENT_TIMESTAMP,
  lastupdated_time TIMESTAMP NULL,
  Requirement_Id   INT       NOT NULL,
  User_Id          INT       NOT NULL,
  BelongsToComment_Id INT,
  CONSTRAINT comments_pk PRIMARY KEY (Id),
  CONSTRAINT Comment_Requirement FOREIGN KEY Comment_Requirement (Requirement_Id) REFERENCES requirements (Id)
    ON DELETE CASCADE,
  CONSTRAINT Comment_User FOREIGN KEY Comment_User (User_Id) REFERENCES users (Id),
  CONSTRAINT belongsToComment FOREIGN KEY comments (BelongsToComment_Id) REFERENCES comments (Id)
);

-- Table components
CREATE TABLE IF NOT EXISTS components (
  Id               INT          NOT NULL  AUTO_INCREMENT,
  name             VARCHAR(255) NOT NULL,
  description      TEXT         NOT NULL,
  creation_time    TIMESTAMP    NOT NULL  DEFAULT CURRENT_TIMESTAMP,
  lastupdated_time TIMESTAMP    NULL,
  Project_Id       INT          NOT NULL,
  Leader_Id        INT          NOT NULL,
  CONSTRAINT components_pk PRIMARY KEY (Id),
  CONSTRAINT Component_Project FOREIGN KEY Component_Project (Project_Id) REFERENCES projects (Id),
  CONSTRAINT Components_Users FOREIGN KEY Components_Users (Leader_Id) REFERENCES users (Id)
);

-- Table developers
CREATE TABLE IF NOT EXISTS developers (
  Id             INT       NOT NULL  AUTO_INCREMENT,
  Requirement_Id INT       NOT NULL,
  User_Id        INT       NOT NULL,
  creation_time  TIMESTAMP NOT NULL  DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT developers_pk PRIMARY KEY (Id),
  CONSTRAINT Developer_Requirement FOREIGN KEY Developer_Requirement (Requirement_Id) REFERENCES requirements (Id)
    ON DELETE CASCADE,
  CONSTRAINT Developer_User FOREIGN KEY Developer_User (User_Id) REFERENCES users (Id)
);

-- Table follower_requirement
CREATE TABLE IF NOT EXISTS requirement_follower (
  Id             INT       NOT NULL  AUTO_INCREMENT,
  Requirement_Id INT       NOT NULL,
  User_Id        INT       NOT NULL,
  creation_time  TIMESTAMP NOT NULL  DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT requirement_followers_pk PRIMARY KEY (Id),
  CONSTRAINT Requirement_Follower FOREIGN KEY Requirement_Follower (Requirement_Id) REFERENCES requirements (Id)
    ON DELETE CASCADE,
  CONSTRAINT Requirement_Follower_User FOREIGN KEY Follower_User (User_Id) REFERENCES users (Id)
);

-- Table component_follower
CREATE TABLE IF NOT EXISTS component_follower (
  Id            INT       NOT NULL  AUTO_INCREMENT,
  Component_Id  INT       NOT NULL,
  User_Id       INT       NOT NULL,
  creation_time TIMESTAMP NOT NULL  DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT component_followers_pk PRIMARY KEY (Id),
  CONSTRAINT Component_Follower FOREIGN KEY Component_Follower (Component_Id) REFERENCES components (Id)
    ON DELETE CASCADE,
  CONSTRAINT Component_Follower_User FOREIGN KEY Component_Follower_User (User_Id) REFERENCES users (Id)
);

-- Table project_follower
CREATE TABLE IF NOT EXISTS project_follower (
  Id            INT       NOT NULL  AUTO_INCREMENT,
  Project_Id    INT       NOT NULL,
  User_Id       INT       NOT NULL,
  creation_time TIMESTAMP NOT NULL  DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT project_followers_pk PRIMARY KEY (Id),
  CONSTRAINT Project_Follower FOREIGN KEY Project_Follower (Project_Id) REFERENCES projects (Id)
    ON DELETE CASCADE,
  CONSTRAINT Project_Follower_User FOREIGN KEY Project_Follower_User (User_Id) REFERENCES users (Id)
);

-- Table privileges
CREATE TABLE IF NOT EXISTS privileges (
  Id   INT          NOT NULL  AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  CONSTRAINT privileges_pk PRIMARY KEY (Id)
);

-- Table projects
CREATE TABLE IF NOT EXISTS projects (
  Id                    INT          NOT NULL  AUTO_INCREMENT,
  name                  VARCHAR(255) NOT NULL,
  description           TEXT         NOT NULL,
  visibility            CHAR(1)      NOT NULL,
  creation_time         TIMESTAMP    NOT NULL  DEFAULT CURRENT_TIMESTAMP,
  lastupdated_time      TIMESTAMP    NULL,
  Leader_Id             INT          NOT NULL,
  Default_Components_Id INT          NULL,
  CONSTRAINT projects_pk PRIMARY KEY (Id),
  CONSTRAINT Projects_Components FOREIGN KEY Projects_Components (Default_Components_Id) REFERENCES components (Id),
  CONSTRAINT Projects_Users FOREIGN KEY Projects_Users (Leader_Id) REFERENCES users (Id)
);

-- Table requirements
CREATE TABLE IF NOT EXISTS requirements (
  Id                INT          NOT NULL  AUTO_INCREMENT,
  title             VARCHAR(255) NOT NULL,
  description       TEXT         NULL,
  realized          TIMESTAMP    NULL      DEFAULT NULL,
  creation_time     TIMESTAMP    NOT NULL  DEFAULT CURRENT_TIMESTAMP,
  lastupdated_time  TIMESTAMP    NULL,
  Lead_developer_Id INT          NULL,
  Creator_Id        INT          NOT NULL,
  Project_Id        INT          NOT NULL,
  CONSTRAINT requirements_pk PRIMARY KEY (Id),
  CONSTRAINT Creator FOREIGN KEY Creator (Creator_Id) REFERENCES users (Id),
  CONSTRAINT LeadDeveloper FOREIGN KEY LeadDeveloper (Lead_developer_Id) REFERENCES users (Id),
  CONSTRAINT Requirement_Project FOREIGN KEY Requirement_Project (Project_Id) REFERENCES projects (Id)
);

-- Table role_privilege
CREATE TABLE IF NOT EXISTS role_privilege (
  Id            INT NOT NULL  AUTO_INCREMENT,
  Roles_Id      INT NOT NULL,
  Privileges_Id INT NOT NULL,
  CONSTRAINT role_privilege_pk PRIMARY KEY (Id),
  CONSTRAINT Role_Privilege_Privileges FOREIGN KEY Role_Privilege_Privileges (Privileges_Id) REFERENCES privileges (Id)
    ON DELETE CASCADE,
  CONSTRAINT Role_Privilege_Roles FOREIGN KEY Role_Privilege_Roles (Roles_Id) REFERENCES roles (Id)
    ON DELETE CASCADE
);

-- Table role_role
CREATE TABLE IF NOT EXISTS role_role (
  Id        INT NOT NULL  AUTO_INCREMENT,
  Child_Id  INT NOT NULL,
  Parent_Id INT NOT NULL,
  CONSTRAINT role_role_pk PRIMARY KEY (Id),
  CONSTRAINT Role_Child FOREIGN KEY Role_Child (Child_Id) REFERENCES roles (Id)
    ON DELETE CASCADE,
  CONSTRAINT Role_Parent FOREIGN KEY Role_Parent (Parent_Id) REFERENCES roles (Id)
    ON DELETE CASCADE
);

-- Table roles
CREATE TABLE IF NOT EXISTS roles (
  Id   INT         NOT NULL  AUTO_INCREMENT,
  name VARCHAR(50) NULL,
  CONSTRAINT roles_pk PRIMARY KEY (Id),
  UNIQUE KEY Role_idx_1 (name)
);

-- Table tags
CREATE TABLE IF NOT EXISTS tags (
  Id              INT NOT NULL  AUTO_INCREMENT,
  Components_Id   INT NOT NULL,
  Requirements_Id INT NOT NULL,
  CONSTRAINT tags_pk PRIMARY KEY (Id),
  CONSTRAINT Tags_Components FOREIGN KEY Tags_Components (Components_Id) REFERENCES components (Id),
  CONSTRAINT Tags_Requirements FOREIGN KEY Tags_Requirements (Requirements_Id) REFERENCES requirements (Id)
    ON DELETE CASCADE
);

-- Table user_role
CREATE TABLE IF NOT EXISTS user_role (
  Id           INT          NOT NULL  AUTO_INCREMENT,
  Roles_Id     INT          NOT NULL,
  Users_Id     INT          NOT NULL,
  context_info VARCHAR(255) NULL,
  CONSTRAINT user_role_pk PRIMARY KEY (Id),
  CONSTRAINT User_Role_Roles FOREIGN KEY User_Role_Roles (Roles_Id) REFERENCES roles (Id)
    ON DELETE CASCADE,
  CONSTRAINT User_Role_Users FOREIGN KEY User_Role_Users (Users_Id) REFERENCES users (Id)
    ON DELETE CASCADE
);

-- Table users
CREATE TABLE IF NOT EXISTS users (
  Id                              INT          NOT NULL  AUTO_INCREMENT,
  first_name                      VARCHAR(150) NULL,
  last_name                       VARCHAR(150) NULL,
  email                           VARCHAR(255) NOT NULL,
  admin                           BOOLEAN      NOT NULL,
  Las2peer_Id                     BIGINT       NOT NULL,
  user_name                       VARCHAR(255) NULL,
  profile_image                   TEXT         NULL,
  email_lead_items            BOOLEAN      NOT NULL  DEFAULT TRUE,
  email_follow_items          BOOLEAN      NOT NULL  DEFAULT TRUE,
  CONSTRAINT users_pk PRIMARY KEY (Id)
);

-- Table votes
CREATE TABLE IF NOT EXISTS votes (
  Id             INT       NOT NULL  AUTO_INCREMENT,
  is_upvote      BOOLEAN   NOT NULL,
  Requirement_Id INT       NOT NULL,
  User_Id        INT       NOT NULL,
  creation_time  TIMESTAMP NOT NULL  DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT votes_pk PRIMARY KEY (Id),
  CONSTRAINT Votes_Requirement FOREIGN KEY Votes_Requirement (Requirement_Id) REFERENCES requirements (Id)
    ON DELETE CASCADE,
  CONSTRAINT Votes_User FOREIGN KEY Votes_User (User_Id) REFERENCES users (Id)
);

SET FOREIGN_KEY_CHECKS = 1;