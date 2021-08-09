-- Script for initializing a MySQL server with a database that can be used as a MysqlLibrarySource.
-- You can change the database name on lines 4 and 5 and the initial default tag type values on line
-- 16.

CREATE DATABASE tagged_music;
USE tagged_music;

CREATE TABLE library (
    version VARCHAR(255) PRIMARY KEY
);
INSERT INTO library VALUES ("1.0");

CREATE TABLE default_tag_type (
    color INT PRIMARY KEY
);
INSERT INTO default_tag_type VALUES (0);

CREATE TABLE songs (
    file_name VARCHAR(255) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    duration BIGINT NOT NULL,
    artist VARCHAR(255),
    album VARCHAR(255),
    track_num INT,
    year INT,
    date_created DATETIME,
    last_modified DATETIME,
    play_count INT
);

CREATE TABLE tag_types (
    name VARCHAR(255) PRIMARY KEY,
    color INT NOT NULL
);

CREATE TABLE tags (
    name VARCHAR(255) PRIMARY KEY,
    type VARCHAR(255),
    description TEXT NOT NULL,
    FOREIGN KEY(type) REFERENCES tag_types(name) ON DELETE SET NULL
);

CREATE TABLE has_tag (
    song_file VARCHAR(255) NOT NULL,
    tag VARCHAR(255) NOT NULL,
    PRIMARY KEY(song_file, tag),
    FOREIGN KEY(song_file) REFERENCES songs(file_name) ON DELETE CASCADE,
    FOREIGN KEY(tag) REFERENCES tags(name) ON DELETE CASCADE
);
