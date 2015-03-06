-- Created by Vertabelo (http://vertabelo.com)
-- Script type: create
-- Scope: [tables, references, sequences, views, procedures]
-- Generated at Thu Mar 05 20:06:24 UTC 2015


SET FOREIGN_KEY_CHECKS=0;

-- tables
-- Table attachments
CREATE TABLE IF NOT EXISTS attachments (
    Id int    NOT NULL  AUTO_INCREMENT,
    creation_time timestamp    NOT NULL ,
    Requirement_Id int    NOT NULL ,
    User_Id int    NOT NULL ,
    title varchar(255)    NOT NULL ,
    discriminator char(1)    NOT NULL ,
    file_path varchar(500)    NULL ,
    description text    NULL ,
    story text    NULL ,
    subject varchar(255)    NULL ,
    object varchar(255)    NULL ,
    object_desc varchar(255)    NULL ,
    CONSTRAINT attachments_pk PRIMARY KEY (Id).,
	CONSTRAINT Attachement_Requirement FOREIGN KEY Attachement_Requirement (Requirement_Id) REFERENCES requirements (Id) ON DELETE CASCADE,
	CONSTRAINT Attachement_User FOREIGN KEY Attachement_User (User_Id) REFERENCES users (Id)
);

-- Table comments
CREATE TABLE IF NOT EXISTS comments (
    Id int    NOT NULL  AUTO_INCREMENT,
    message text    NOT NULL ,
    creation_time timestamp    NOT NULL ,
    Requirement_Id int    NOT NULL ,
    User_Id int    NOT NULL ,
    CONSTRAINT comments_pk PRIMARY KEY (Id),
	CONSTRAINT Comment_Requirement FOREIGN KEY Comment_Requirement (Requirement_Id) REFERENCES requirements (Id) ON DELETE CASCADE,
	CONSTRAINT Comment_User FOREIGN KEY Comment_User (User_Id)  REFERENCES users (Id)
);

-- Table components
CREATE TABLE IF NOT EXISTS components (
    Id int    NOT NULL  AUTO_INCREMENT,
    name varchar(255)    NOT NULL ,
    description text    NOT NULL ,
    Project_Id int    NOT NULL ,
    Leader_Id int    NOT NULL ,
    CONSTRAINT components_pk PRIMARY KEY (Id),
	CONSTRAINT Component_Project FOREIGN KEY Component_Project (Project_Id) REFERENCES projects (Id),
	CONSTRAINT Components_Users FOREIGN KEY Components_Users (Leader_Id) REFERENCES users (Id)
);

-- Table developers
CREATE TABLE IF NOT EXISTS developers (
    Id int    NOT NULL  AUTO_INCREMENT,
    Requirement_Id int    NOT NULL ,
    User_Id int    NOT NULL ,
    creation_time timestamp    NOT NULL ,
    CONSTRAINT developers_pk PRIMARY KEY (Id),
	CONSTRAINT Developer_Requirement FOREIGN KEY Developer_Requirement (Requirement_Id)  REFERENCES requirements (Id)  ON DELETE CASCADE,
	CONSTRAINT Developer_User FOREIGN KEY Developer_User (User_Id)  REFERENCES users (Id)
);

-- Table followers
CREATE TABLE IF NOT EXISTS followers (
    Id int    NOT NULL  AUTO_INCREMENT,
    Requirement_Id int    NOT NULL ,
    User_Id int    NOT NULL ,
    creation_time timestamp    NULL ,
    CONSTRAINT followers_pk PRIMARY KEY (Id),
	CONSTRAINT Follower_Requirement FOREIGN KEY Follower_Requirement (Requirement_Id)  REFERENCES requirements (Id)  ON DELETE CASCADE,
	CONSTRAINT Follower_User FOREIGN KEY Follower_User (User_Id)  REFERENCES users (Id)
);

-- Table privileges
CREATE TABLE IF NOT EXISTS privileges (
    Id int    NOT NULL  AUTO_INCREMENT,
    name varchar(100)    NOT NULL ,
    CONSTRAINT privileges_pk PRIMARY KEY (Id)
);

-- Table projects
CREATE TABLE IF NOT EXISTS projects (
    Id int    NOT NULL  AUTO_INCREMENT,
    name varchar(255)    NOT NULL ,
    description text    NOT NULL ,
    visibility char(1)    NOT NULL ,
    Leader_Id int    NOT NULL ,
    Default_Components_Id int    NULL ,
    CONSTRAINT projects_pk PRIMARY KEY (Id),
	CONSTRAINT Projects_Components FOREIGN KEY Projects_Components (Default_Components_Id)  REFERENCES components (Id),
	CONSTRAINT Projects_Users FOREIGN KEY Projects_Users (Leader_Id)  REFERENCES users (Id)
);

-- Table requirements
CREATE TABLE IF NOT EXISTS requirements (
    Id int    NOT NULL  AUTO_INCREMENT,
    title varchar(255)    NOT NULL ,
    description text    NULL ,
    creation_time timestamp    NOT NULL ,
    Lead_developer_Id int    NOT NULL ,
    Creator_Id int    NOT NULL ,
    Project_Id int    NOT NULL ,
    CONSTRAINT requirements_pk PRIMARY KEY (Id),
	CONSTRAINT Creator FOREIGN KEY Creator (Creator_Id)  REFERENCES users (Id),
	CONSTRAINT LeadDeveloper FOREIGN KEY LeadDeveloper (Lead_developer_Id)  REFERENCES users (Id),
	CONSTRAINT Requirement_Project FOREIGN KEY Requirement_Project (Project_Id)   REFERENCES projects (Id)
);

-- Table role_privilege
CREATE TABLE IF NOT EXISTS role_privilege (
    Id int    NOT NULL  AUTO_INCREMENT,
    Roles_Id int    NOT NULL ,
    Privileges_Id int    NOT NULL ,
    CONSTRAINT role_privilege_pk PRIMARY KEY (Id),
	CONSTRAINT Role_Privilege_Privileges FOREIGN KEY Role_Privilege_Privileges (Privileges_Id)  REFERENCES privileges (Id)  ON DELETE CASCADE,
	CONSTRAINT Role_Privilege_Roles FOREIGN KEY Role_Privilege_Roles (Roles_Id)  REFERENCES roles (Id)  ON DELETE CASCADE
);

-- Table role_role
CREATE TABLE IF NOT EXISTS role_role (
    Id int    NOT NULL  AUTO_INCREMENT,
    Child_Id int    NOT NULL ,
    Parent_Id int    NOT NULL ,
    CONSTRAINT role_role_pk PRIMARY KEY (Id),
	CONSTRAINT Role_Child FOREIGN KEY Role_Child (Child_Id)   REFERENCES roles (Id)  ON DELETE CASCADE,
	CONSTRAINT Role_Parent FOREIGN KEY Role_Parent (Parent_Id)  REFERENCES roles (Id)   ON DELETE CASCADE
);

-- Table roles
CREATE TABLE IF NOT EXISTS roles (
    Id int    NOT NULL  AUTO_INCREMENT,
    name varchar(50)    NULL ,
    CONSTRAINT roles_pk PRIMARY KEY (Id)
);

CREATE  UNIQUE INDEX Role_idx_1 ON roles (name);


-- Table tags
CREATE TABLE IF NOT EXISTS tags (
    Id int    NOT NULL  AUTO_INCREMENT,
    Components_Id int    NOT NULL ,
    Requirements_Id int    NOT NULL ,
    CONSTRAINT tags_pk PRIMARY KEY (Id),
	CONSTRAINT Tags_Components FOREIGN KEY Tags_Components (Components_Id)    REFERENCES components (Id),
	CONSTRAINT Tags_Requirements FOREIGN KEY Tags_Requirements (Requirements_Id)    REFERENCES requirements (Id)    ON DELETE CASCADE
);

-- Table user_role
CREATE TABLE IF NOT EXISTS user_role (
    Id int    NOT NULL  AUTO_INCREMENT,
    Roles_Id int    NOT NULL ,
    Users_Id int    NOT NULL ,
    context_info varchar(255)    NULL ,
    CONSTRAINT user_role_pk PRIMARY KEY (Id),
	CONSTRAINT User_Role_Roles FOREIGN KEY User_Role_Roles (Roles_Id)    REFERENCES roles (Id)    ON DELETE CASCADE,
	CONSTRAINT User_Role_Users FOREIGN KEY User_Role_Users (Users_Id)    REFERENCES users (Id)    ON DELETE CASCADE
);

-- Table users
CREATE TABLE IF NOT EXISTS users (
    Id int    NOT NULL  AUTO_INCREMENT,
    first_name varchar(150)    NULL ,
    last_name varchar(150)    NULL ,
    email varchar(255)    NOT NULL ,
    admin BOOLEAN    NOT NULL ,
    Las2peer_Id bigint    NOT NULL ,
    user_name varchar(255)    NULL ,
    profile_image text    NULL ,
    CONSTRAINT users_pk PRIMARY KEY (Id)
);

-- Table votes
CREATE TABLE IF NOT EXISTS votes (
    Id int    NOT NULL  AUTO_INCREMENT,
    is_upvote BOOLEAN    NOT NULL ,
    Requirement_Id int    NOT NULL ,
    User_Id int    NOT NULL ,
    creation_time timestamp    NOT NULL ,
    CONSTRAINT votes_pk PRIMARY KEY (Id),
	CONSTRAINT Votes_Requirement FOREIGN KEY Votes_Requirement (Requirement_Id)    REFERENCES requirements (Id)    ON DELETE CASCADE,
	CONSTRAINT Votes_User FOREIGN KEY Votes_User (User_Id)    REFERENCES users (Id)
);


SET FOREIGN_KEY_CHECKS=1;