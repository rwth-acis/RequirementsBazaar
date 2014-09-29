-- Created by Vertabelo (http://vertabelo.com)
-- Script type: create
-- Scope: [tables, references, sequences, views, procedures]
-- Generated at Mon Jul 21 11:23:13 UTC 2014

-- tables
-- Table Attachments
CREATE TABLE Attachments (
    Id integer primary key autoincrement not null,
    creation_time timestamp  NOT NULL,
    Requirement_Id int  NOT NULL,
    User_Id int  NOT NULL,
    title varchar(255)  NOT NULL,
    discriminator char(1)  NOT NULL,
    file_path varchar(500)  NOT NULL,
    description text  NOT NULL,
    story text  NOT NULL,
    subject varchar(255)  NOT NULL,
    object varchar(255)  NOT NULL,
    object_desc varchar(255)  NOT NULL,
	
	FOREIGN KEY (Requirement_Id) REFERENCES Requirements (Id),
	FOREIGN KEY (User_Id) REFERENCES Users (Id)
);

-- Table Authorizations
CREATE TABLE Authorizations (
    Id integer primary key autoincrement not null,
    access_right int  NOT NULL,
    Project_Id int  NOT NULL,
    User_Id int  NOT NULL,
	
	FOREIGN KEY (Project_Id)   REFERENCES Projects (Id),
	FOREIGN KEY (User_Id)    REFERENCES Users (Id)
);

-- Table Comments
CREATE TABLE Comments (
    Id integer primary key autoincrement not null,
    message text  NOT NULL,
    creation_time timestamp  NOT NULL,
    Requirement_Id int  NOT NULL,
    User_Id int  NOT NULL,
	
	FOREIGN KEY (Requirement_Id)    REFERENCES Requirements (Id),
	FOREIGN KEY (User_Id)    REFERENCES Users (Id)
);

-- Table Components
CREATE TABLE Components (
    Id integer primary key autoincrement not null,
    name varchar(255)  NOT NULL,
    description text  NOT NULL,
    Project_Id int  NOT NULL,
    Leader_Id int  NOT NULL,
	
	FOREIGN KEY (Project_Id)    REFERENCES Projects (Id),
	FOREIGN KEY (Leader_Id)    REFERENCES Users (Id)
);

-- Table Developers
CREATE TABLE Developers (
    Id integer primary key autoincrement not null,
    Requirement_Id int  NOT NULL,
    User_Id int  NOT NULL,
	
	FOREIGN KEY (Requirement_Id)    REFERENCES Requirements (Id),
	FOREIGN KEY (User_Id)    REFERENCES Users (Id)
);

-- Table Followers
CREATE TABLE Followers (
    Id integer primary key autoincrement not null,
    Requirement_Id int  NOT NULL,
    User_Id int  NOT NULL,
	
	FOREIGN KEY (Requirement_Id)    REFERENCES Requirements (Id),
	FOREIGN KEY (User_Id)    REFERENCES Users (Id)
);

-- Table Projects
CREATE TABLE Projects (
    Id integer primary key autoincrement not null,
    name varchar(255)  NOT NULL,
    description varchar(255)  NOT NULL,
    visibility char(1)  NOT NULL,
    Leader_Id int  NOT NULL,
	
	FOREIGN KEY (Leader_Id)    REFERENCES Users (Id),
);

-- Table Requirements
CREATE TABLE Requirements (
    Id integer primary key autoincrement not null,
    title varchar(255)  NOT NULL,
    description text  NOT NULL,
    creation_time timestamp  NOT NULL,
    Lead_developer_Id int  NOT NULL,
    Creator_Id int  NOT NULL,
    Project_Id int  NOT NULL,
	
	FOREIGN KEY (Creator_Id)    REFERENCES Users (Id),
	FOREIGN KEY (Lead_developer_Id)    REFERENCES Users (Id),
	FOREIGN KEY (Project_Id)    REFERENCES Projects (Id)
);

-- Table Tags
CREATE TABLE Tags (
    Id integer primary key autoincrement not null,
    Components_Id int  NOT NULL,
    Requirements_Id int  NOT NULL,
	
	FOREIGN KEY (Components_Id)    REFERENCES Components (Id),
	FOREIGN KEY (Requirements_Id)    REFERENCES Requirements (Id)
);

-- Table Users
CREATE TABLE Users (
    Id integer primary key autoincrement not null,
    first_name varchar(150)  NOT NULL,
    last_name varchar(150)  NOT NULL,
    email varchar(255)  NOT NULL,
    admin BOOLEAN  NOT NULL,
    User_Id int  NOT NULL,
    user_name varchar(255)  NOT NULL,
    openId_iss varchar(300)  NOT NULL,
    openId_sub varchar(300)  NOT NULL
);

-- Table Votes
CREATE TABLE Votes (
    Id integer primary key autoincrement not null,
    is_upvote BOOLEAN  NOT NULL,
    Requirement_Id int  NOT NULL,
    User_Id int  NOT NULL,
	
	FOREIGN KEY (Requirement_Id)    REFERENCES Requirements (Id),
	FOREIGN KEY (User_Id)    REFERENCES Users (Id)
);


-- End of file.
