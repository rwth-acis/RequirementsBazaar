-- Created by Vertabelo (http://vertabelo.com)
-- Script type: create
-- Scope: [tables, references, sequences, views, procedures]
-- Generated at Fri Jan 09 10:21:00 UTC 2015




-- tables
-- Table Attachments
CREATE TABLE Attachments (
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
    CONSTRAINT Attachments_pk PRIMARY KEY (Id)
);

-- Table Authorizations
CREATE TABLE Authorizations (
    Id int    NOT NULL  AUTO_INCREMENT,
    access_right int    NOT NULL ,
    Project_Id int    NOT NULL ,
    User_Id int    NOT NULL ,
    CONSTRAINT Authorizations_pk PRIMARY KEY (Id)
);

-- Table Comments
CREATE TABLE Comments (
    Id int    NOT NULL  AUTO_INCREMENT,
    message text    NOT NULL ,
    creation_time timestamp    NOT NULL ,
    Requirement_Id int    NOT NULL ,
    User_Id int    NOT NULL ,
    CONSTRAINT Comments_pk PRIMARY KEY (Id)
);

-- Table Components
CREATE TABLE Components (
    Id int    NOT NULL  AUTO_INCREMENT,
    name varchar(255)    NOT NULL ,
    description text    NOT NULL ,
    Project_Id int    NOT NULL ,
    Leader_Id int    NOT NULL ,
    CONSTRAINT Components_pk PRIMARY KEY (Id)
);

-- Table Developers
CREATE TABLE Developers (
    Id int    NOT NULL  AUTO_INCREMENT,
    Requirement_Id int    NOT NULL ,
    User_Id int    NOT NULL ,
    CONSTRAINT Developers_pk PRIMARY KEY (Id)
);

-- Table Followers
CREATE TABLE Followers (
    Id int    NOT NULL  AUTO_INCREMENT,
    Requirement_Id int    NOT NULL ,
    User_Id int    NOT NULL ,
    CONSTRAINT Followers_pk PRIMARY KEY (Id)
);

-- Table Projects
CREATE TABLE Projects (
    Id int    NOT NULL  AUTO_INCREMENT,
    name varchar(255)    NOT NULL ,
    description varchar(255)    NOT NULL ,
    visibility char(1)    NOT NULL ,
    Leader_Id int    NOT NULL ,
    Default_Components_Id int    NULL ,
    CONSTRAINT Projects_pk PRIMARY KEY (Id)
);

-- Table Requirements
CREATE TABLE Requirements (
    Id int    NOT NULL  AUTO_INCREMENT,
    title varchar(255)    NOT NULL ,
    description text    NOT NULL ,
    creation_time timestamp    NOT NULL ,
    Lead_developer_Id int    NOT NULL ,
    Creator_Id int    NOT NULL ,
    Project_Id int    NOT NULL ,
    CONSTRAINT Requirements_pk PRIMARY KEY (Id)
);

-- Table Tags
CREATE TABLE Tags (
    Id int    NOT NULL  AUTO_INCREMENT,
    Components_Id int    NOT NULL ,
    Requirements_Id int    NOT NULL ,
    CONSTRAINT Tags_pk PRIMARY KEY (Id)
);

-- Table Users
CREATE TABLE Users (
    Id int    NOT NULL  AUTO_INCREMENT,
    first_name varchar(150)    NOT NULL ,
    last_name varchar(150)    NOT NULL ,
    email varchar(255)    NOT NULL ,
    admin BOOLEAN    NOT NULL ,
    Las2peer_Id int    NOT NULL ,
    user_name varchar(255)    NOT NULL ,
    CONSTRAINT Users_pk PRIMARY KEY (Id)
);

-- Table Votes
CREATE TABLE Votes (
    Id int    NOT NULL  AUTO_INCREMENT,
    is_upvote BOOLEAN    NOT NULL ,
    Requirement_Id int    NOT NULL ,
    User_Id int    NOT NULL ,
    CONSTRAINT Votes_pk PRIMARY KEY (Id)
);





-- foreign keys
-- Reference:  Attachement_Requirement (table: Attachments)


ALTER TABLE Attachments ADD CONSTRAINT Attachement_Requirement FOREIGN KEY Attachement_Requirement (Requirement_Id)
    REFERENCES Requirements (Id);
-- Reference:  Attachement_User (table: Attachments)


ALTER TABLE Attachments ADD CONSTRAINT Attachement_User FOREIGN KEY Attachement_User (User_Id)
    REFERENCES Users (Id);
-- Reference:  Authorization_Project (table: Authorizations)


ALTER TABLE Authorizations ADD CONSTRAINT Authorization_Project FOREIGN KEY Authorization_Project (Project_Id)
    REFERENCES Projects (Id);
-- Reference:  Authorization_User (table: Authorizations)


ALTER TABLE Authorizations ADD CONSTRAINT Authorization_User FOREIGN KEY Authorization_User (User_Id)
    REFERENCES Users (Id);
-- Reference:  Comment_Requirement (table: Comments)


ALTER TABLE Comments ADD CONSTRAINT Comment_Requirement FOREIGN KEY Comment_Requirement (Requirement_Id)
    REFERENCES Requirements (Id);
-- Reference:  Comment_User (table: Comments)


ALTER TABLE Comments ADD CONSTRAINT Comment_User FOREIGN KEY Comment_User (User_Id)
    REFERENCES Users (Id);
-- Reference:  Component_Project (table: Components)


ALTER TABLE Components ADD CONSTRAINT Component_Project FOREIGN KEY Component_Project (Project_Id)
    REFERENCES Projects (Id);
-- Reference:  Components_Users (table: Components)


ALTER TABLE Components ADD CONSTRAINT Components_Users FOREIGN KEY Components_Users (Leader_Id)
    REFERENCES Users (Id);
-- Reference:  Creator (table: Requirements)


ALTER TABLE Requirements ADD CONSTRAINT Creator FOREIGN KEY Creator (Creator_Id)
    REFERENCES Users (Id);
-- Reference:  Developer_Requirement (table: Developers)


ALTER TABLE Developers ADD CONSTRAINT Developer_Requirement FOREIGN KEY Developer_Requirement (Requirement_Id)
    REFERENCES Requirements (Id);
-- Reference:  Developer_User (table: Developers)


ALTER TABLE Developers ADD CONSTRAINT Developer_User FOREIGN KEY Developer_User (User_Id)
    REFERENCES Users (Id);
-- Reference:  Follower_Requirement (table: Followers)


ALTER TABLE Followers ADD CONSTRAINT Follower_Requirement FOREIGN KEY Follower_Requirement (Requirement_Id)
    REFERENCES Requirements (Id);
-- Reference:  Follower_User (table: Followers)


ALTER TABLE Followers ADD CONSTRAINT Follower_User FOREIGN KEY Follower_User (User_Id)
    REFERENCES Users (Id);
-- Reference:  LeadDeveloper (table: Requirements)


ALTER TABLE Requirements ADD CONSTRAINT LeadDeveloper FOREIGN KEY LeadDeveloper (Lead_developer_Id)
    REFERENCES Users (Id);
-- Reference:  Projects_Components (table: Projects)


ALTER TABLE Projects ADD CONSTRAINT Projects_Components FOREIGN KEY Projects_Components (Default_Components_Id)
    REFERENCES Components (Id);
-- Reference:  Projects_Users (table: Projects)


ALTER TABLE Projects ADD CONSTRAINT Projects_Users FOREIGN KEY Projects_Users (Leader_Id)
    REFERENCES Users (Id);
-- Reference:  Requirement_Project (table: Requirements)


ALTER TABLE Requirements ADD CONSTRAINT Requirement_Project FOREIGN KEY Requirement_Project (Project_Id)
    REFERENCES Projects (Id);
-- Reference:  Tags_Components (table: Tags)


ALTER TABLE Tags ADD CONSTRAINT Tags_Components FOREIGN KEY Tags_Components (Components_Id)
    REFERENCES Components (Id);
-- Reference:  Tags_Requirements (table: Tags)


ALTER TABLE Tags ADD CONSTRAINT Tags_Requirements FOREIGN KEY Tags_Requirements (Requirements_Id)
    REFERENCES Requirements (Id);
-- Reference:  Votes_Requirement (table: Votes)


ALTER TABLE Votes ADD CONSTRAINT Votes_Requirement FOREIGN KEY Votes_Requirement (Requirement_Id)
    REFERENCES Requirements (Id);
-- Reference:  Votes_User (table: Votes)


ALTER TABLE Votes ADD CONSTRAINT Votes_User FOREIGN KEY Votes_User (User_Id)
    REFERENCES Users (Id);



-- End of file.
