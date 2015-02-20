-- Created by Vertabelo (http://vertabelo.com)
-- Script type: create
-- Scope: [tables, references, sequences, views, procedures]
-- Generated at Fri Feb 20 12:42:30 UTC 2015




-- tables
-- Table attachments
CREATE TABLE attachments (
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
    CONSTRAINT attachments_pk PRIMARY KEY (Id)
);

-- Table comments
CREATE TABLE comments (
    Id int    NOT NULL  AUTO_INCREMENT,
    message text    NOT NULL ,
    creation_time timestamp    NOT NULL ,
    Requirement_Id int    NOT NULL ,
    User_Id int    NOT NULL ,
    CONSTRAINT comments_pk PRIMARY KEY (Id)
);

-- Table components
CREATE TABLE components (
    Id int    NOT NULL  AUTO_INCREMENT,
    name varchar(255)    NOT NULL ,
    description text    NOT NULL ,
    Project_Id int    NOT NULL ,
    Leader_Id int    NOT NULL ,
    CONSTRAINT components_pk PRIMARY KEY (Id)
);

-- Table developers
CREATE TABLE developers (
    Id int    NOT NULL  AUTO_INCREMENT,
    Requirement_Id int    NOT NULL ,
    User_Id int    NOT NULL ,
    CONSTRAINT developers_pk PRIMARY KEY (Id)
);

-- Table followers
CREATE TABLE followers (
    Id int    NOT NULL  AUTO_INCREMENT,
    Requirement_Id int    NOT NULL ,
    User_Id int    NOT NULL ,
    CONSTRAINT followers_pk PRIMARY KEY (Id)
);

-- Table privileges
CREATE TABLE privileges (
    Id int    NOT NULL  AUTO_INCREMENT,
    name varchar(100)    NOT NULL ,
    CONSTRAINT privileges_pk PRIMARY KEY (Id)
);

-- Table projects
CREATE TABLE projects (
    Id int    NOT NULL  AUTO_INCREMENT,
    name varchar(255)    NOT NULL ,
    description text    NOT NULL ,
    visibility char(1)    NOT NULL ,
    Leader_Id int    NOT NULL ,
    Default_Components_Id int    NULL ,
    CONSTRAINT projects_pk PRIMARY KEY (Id)
);

-- Table requirements
CREATE TABLE requirements (
    Id int    NOT NULL  AUTO_INCREMENT,
    title varchar(255)    NOT NULL ,
    description text    NOT NULL ,
    creation_time timestamp    NOT NULL ,
    Lead_developer_Id int    NOT NULL ,
    Creator_Id int    NOT NULL ,
    Project_Id int    NOT NULL ,
    CONSTRAINT requirements_pk PRIMARY KEY (Id)
);

-- Table role_privilege
CREATE TABLE role_privilege (
    Id int    NOT NULL  AUTO_INCREMENT,
    Roles_Id int    NOT NULL ,
    Privileges_Id int    NOT NULL ,
    CONSTRAINT role_privilege_pk PRIMARY KEY (Id)
);

-- Table role_role
CREATE TABLE role_role (
    Id int    NOT NULL  AUTO_INCREMENT,
    Child_Id int    NOT NULL ,
    Parent_Id int    NOT NULL ,
    CONSTRAINT role_role_pk PRIMARY KEY (Id)
);

-- Table roles
CREATE TABLE roles (
    Id int    NOT NULL  AUTO_INCREMENT,
    name varchar(50)    NULL ,
    CONSTRAINT roles_pk PRIMARY KEY (Id)
);

CREATE  UNIQUE INDEX Role_idx_1 ON roles (name);


-- Table tags
CREATE TABLE tags (
    Id int    NOT NULL  AUTO_INCREMENT,
    Components_Id int    NOT NULL ,
    Requirements_Id int    NOT NULL ,
    CONSTRAINT tags_pk PRIMARY KEY (Id)
);

-- Table user_role
CREATE TABLE user_role (
    Id int    NOT NULL  AUTO_INCREMENT,
    Roles_Id int    NOT NULL ,
    Users_Id int    NOT NULL ,
    context_info varchar(255)    NULL ,
    CONSTRAINT user_role_pk PRIMARY KEY (Id)
);

-- Table users
CREATE TABLE users (
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
CREATE TABLE votes (
    Id int    NOT NULL  AUTO_INCREMENT,
    is_upvote BOOLEAN    NOT NULL ,
    Requirement_Id int    NOT NULL ,
    User_Id int    NOT NULL ,
    CONSTRAINT votes_pk PRIMARY KEY (Id)
);





-- foreign keys
-- Reference:  Attachement_Requirement (table: attachments)


ALTER TABLE attachments ADD CONSTRAINT Attachement_Requirement FOREIGN KEY Attachement_Requirement (Requirement_Id)
    REFERENCES requirements (Id)
    ON DELETE CASCADE;
-- Reference:  Attachement_User (table: attachments)


ALTER TABLE attachments ADD CONSTRAINT Attachement_User FOREIGN KEY Attachement_User (User_Id)
    REFERENCES users (Id);
-- Reference:  Comment_Requirement (table: comments)


ALTER TABLE comments ADD CONSTRAINT Comment_Requirement FOREIGN KEY Comment_Requirement (Requirement_Id)
    REFERENCES requirements (Id)
    ON DELETE CASCADE;
-- Reference:  Comment_User (table: comments)


ALTER TABLE comments ADD CONSTRAINT Comment_User FOREIGN KEY Comment_User (User_Id)
    REFERENCES users (Id);
-- Reference:  Component_Project (table: components)


ALTER TABLE components ADD CONSTRAINT Component_Project FOREIGN KEY Component_Project (Project_Id)
    REFERENCES projects (Id);
-- Reference:  Components_Users (table: components)


ALTER TABLE components ADD CONSTRAINT Components_Users FOREIGN KEY Components_Users (Leader_Id)
    REFERENCES users (Id);
-- Reference:  Creator (table: requirements)


ALTER TABLE requirements ADD CONSTRAINT Creator FOREIGN KEY Creator (Creator_Id)
    REFERENCES users (Id);
-- Reference:  Developer_Requirement (table: developers)


ALTER TABLE developers ADD CONSTRAINT Developer_Requirement FOREIGN KEY Developer_Requirement (Requirement_Id)
    REFERENCES requirements (Id)
    ON DELETE CASCADE;
-- Reference:  Developer_User (table: developers)


ALTER TABLE developers ADD CONSTRAINT Developer_User FOREIGN KEY Developer_User (User_Id)
    REFERENCES users (Id);
-- Reference:  Follower_Requirement (table: followers)


ALTER TABLE followers ADD CONSTRAINT Follower_Requirement FOREIGN KEY Follower_Requirement (Requirement_Id)
    REFERENCES requirements (Id)
    ON DELETE CASCADE;
-- Reference:  Follower_User (table: followers)


ALTER TABLE followers ADD CONSTRAINT Follower_User FOREIGN KEY Follower_User (User_Id)
    REFERENCES users (Id);
-- Reference:  LeadDeveloper (table: requirements)


ALTER TABLE requirements ADD CONSTRAINT LeadDeveloper FOREIGN KEY LeadDeveloper (Lead_developer_Id)
    REFERENCES users (Id);
-- Reference:  Projects_Components (table: projects)


ALTER TABLE projects ADD CONSTRAINT Projects_Components FOREIGN KEY Projects_Components (Default_Components_Id)
    REFERENCES components (Id);
-- Reference:  Projects_Users (table: projects)


ALTER TABLE projects ADD CONSTRAINT Projects_Users FOREIGN KEY Projects_Users (Leader_Id)
    REFERENCES users (Id);
-- Reference:  Requirement_Project (table: requirements)


ALTER TABLE requirements ADD CONSTRAINT Requirement_Project FOREIGN KEY Requirement_Project (Project_Id)
    REFERENCES projects (Id);
-- Reference:  Role_Child (table: role_role)


ALTER TABLE role_role ADD CONSTRAINT Role_Child FOREIGN KEY Role_Child (Child_Id)
    REFERENCES roles (Id)
    ON DELETE CASCADE;
-- Reference:  Role_Parent (table: role_role)


ALTER TABLE role_role ADD CONSTRAINT Role_Parent FOREIGN KEY Role_Parent (Parent_Id)
    REFERENCES roles (Id)
    ON DELETE CASCADE;
-- Reference:  Role_Privilege_Privileges (table: role_privilege)


ALTER TABLE role_privilege ADD CONSTRAINT Role_Privilege_Privileges FOREIGN KEY Role_Privilege_Privileges (Privileges_Id)
    REFERENCES privileges (Id)
    ON DELETE CASCADE;
-- Reference:  Role_Privilege_Roles (table: role_privilege)


ALTER TABLE role_privilege ADD CONSTRAINT Role_Privilege_Roles FOREIGN KEY Role_Privilege_Roles (Roles_Id)
    REFERENCES roles (Id)
    ON DELETE CASCADE;
-- Reference:  Tags_Components (table: tags)


ALTER TABLE tags ADD CONSTRAINT Tags_Components FOREIGN KEY Tags_Components (Components_Id)
    REFERENCES components (Id);
-- Reference:  Tags_Requirements (table: tags)


ALTER TABLE tags ADD CONSTRAINT Tags_Requirements FOREIGN KEY Tags_Requirements (Requirements_Id)
    REFERENCES requirements (Id)
    ON DELETE CASCADE;
-- Reference:  User_Role_Roles (table: user_role)


ALTER TABLE user_role ADD CONSTRAINT User_Role_Roles FOREIGN KEY User_Role_Roles (Roles_Id)
    REFERENCES roles (Id)
    ON DELETE CASCADE;
-- Reference:  User_Role_Users (table: user_role)


ALTER TABLE user_role ADD CONSTRAINT User_Role_Users FOREIGN KEY User_Role_Users (Users_Id)
    REFERENCES users (Id)
    ON DELETE CASCADE;
-- Reference:  Votes_Requirement (table: votes)


ALTER TABLE votes ADD CONSTRAINT Votes_Requirement FOREIGN KEY Votes_Requirement (Requirement_Id)
    REFERENCES requirements (Id)
    ON DELETE CASCADE;
-- Reference:  Votes_User (table: votes)


ALTER TABLE votes ADD CONSTRAINT Votes_User FOREIGN KEY Votes_User (User_Id)
    REFERENCES users (Id);



-- End of file.
