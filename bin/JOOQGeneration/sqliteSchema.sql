-- Created by Vertabelo (http://vertabelo.com)
-- Script type: create
-- Scope: [tables, references, sequences, views, procedures]
-- Generated at Wed Oct 01 11:57:05 UTC 2014



-- tables
-- Table: Attachments
CREATE TABLE Attachments (
    Id integer NOT NULL  PRIMARY KEY AUTOINCREMENT,
    creation_time datetime NOT NULL,
    Requirement_Id integer NOT NULL,
    User_Id integer NOT NULL,
    title varchar(255) NOT NULL,
    discriminator character(1) NOT NULL,
    file_path varchar(500),
    description text,
    story text,
    subject varchar(255),
    object varchar(255),
    object_desc varchar(255),
    FOREIGN KEY (Requirement_Id) REFERENCES Requirements (Id),
    FOREIGN KEY (User_Id) REFERENCES Users (Id)
);

-- Table: Authorizations
CREATE TABLE Authorizations (
    Id integer NOT NULL  PRIMARY KEY AUTOINCREMENT,
    access_right integer NOT NULL,
    Project_Id integer NOT NULL,
    User_Id integer NOT NULL,
    FOREIGN KEY (Project_Id) REFERENCES Projects (Id),
    FOREIGN KEY (User_Id) REFERENCES Users (Id)
);

-- Table: Comments
CREATE TABLE Comments (
    Id integer NOT NULL  PRIMARY KEY AUTOINCREMENT,
    message text NOT NULL,
    creation_time datetime NOT NULL,
    Requirement_Id integer NOT NULL,
    User_Id integer NOT NULL,
    FOREIGN KEY (Requirement_Id) REFERENCES Requirements (Id),
    FOREIGN KEY (User_Id) REFERENCES Users (Id)
);

-- Table: Components
CREATE TABLE Components (
    Id integer NOT NULL  PRIMARY KEY AUTOINCREMENT,
    name varchar(255) NOT NULL,
    description text NOT NULL,
    Project_Id integer NOT NULL,
    Leader_Id integer NOT NULL,
    FOREIGN KEY (Project_Id) REFERENCES Projects (Id),
    FOREIGN KEY (Leader_Id) REFERENCES Users (Id)
);

-- Table: Developers
CREATE TABLE Developers (
    Id integer NOT NULL  PRIMARY KEY AUTOINCREMENT,
    Requirement_Id integer NOT NULL,
    User_Id integer NOT NULL,
    FOREIGN KEY (Requirement_Id) REFERENCES Requirements (Id),
    FOREIGN KEY (User_Id) REFERENCES Users (Id)
);

-- Table: Followers
CREATE TABLE Followers (
    Id integer NOT NULL  PRIMARY KEY AUTOINCREMENT,
    Requirement_Id integer NOT NULL,
    User_Id integer NOT NULL,
    FOREIGN KEY (Requirement_Id) REFERENCES Requirements (Id),
    FOREIGN KEY (User_Id) REFERENCES Users (Id)
);

-- Table: Projects
CREATE TABLE Projects (
    Id integer NOT NULL  PRIMARY KEY AUTOINCREMENT,
    name varchar(255) NOT NULL,
    description varchar(255) NOT NULL,
    visibility character(1) NOT NULL,
    Leader_Id integer NOT NULL,
    FOREIGN KEY (Leader_Id) REFERENCES Users (Id)
);

-- Table: Requirements
CREATE TABLE Requirements (
    Id integer NOT NULL  PRIMARY KEY AUTOINCREMENT,
    title varchar(255) NOT NULL,
    description text NOT NULL,
    creation_time datetime NOT NULL,
    Lead_developer_Id integer NOT NULL,
    Creator_Id integer NOT NULL,
    Project_Id integer NOT NULL,
    FOREIGN KEY (Lead_developer_Id) REFERENCES Users (Id),
    FOREIGN KEY (Creator_Id) REFERENCES Users (Id),
    FOREIGN KEY (Project_Id) REFERENCES Projects (Id)
);

-- Table: Tags
CREATE TABLE Tags (
    Id integer NOT NULL  PRIMARY KEY AUTOINCREMENT,
    Components_Id integer NOT NULL,
    Requirements_Id integer NOT NULL,
    FOREIGN KEY (Components_Id) REFERENCES Components (Id),
    FOREIGN KEY (Requirements_Id) REFERENCES Requirements (Id)
);

-- Table: Users
CREATE TABLE Users (
    Id integer NOT NULL  PRIMARY KEY AUTOINCREMENT,
    first_name varchar(150) NOT NULL,
    last_name varchar(150) NOT NULL,
    email varchar(255) NOT NULL,
    admin BOOLEAN NOT NULL,
    Las2peer_Id integer NOT NULL,
    user_name varchar(255) NOT NULL
);

-- Table: Votes
CREATE TABLE Votes (
    Id integer NOT NULL  PRIMARY KEY AUTOINCREMENT,
    is_upvote BOOLEAN NOT NULL,
    Requirement_Id integer NOT NULL,
    User_Id integer NOT NULL,
    FOREIGN KEY (Requirement_Id) REFERENCES Requirements (Id),
    FOREIGN KEY (User_Id) REFERENCES Users (Id)
);





-- End of file.
