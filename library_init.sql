/*
 * Script for initializing a MySQL server with a database that can be used as a MysqlLibrarySource.
 * The database name can be set on lines 4 and 5, and the default tag type initial values can be set
 * on line 12.
 */

-- Sets the name of the database on the MySQL server.
CREATE DATABASE tagged_music;
USE tagged_music;

-- Sets the initial default tag type properties.
SET @default_tag_type_color := 0;

-- Do not modify.
SET @version := '1.0';

CREATE TABLE Library (
    version VARCHAR(255) PRIMARY KEY
);
INSERT INTO Library(version) VALUES (@version);

CREATE TABLE Songs (
    file_name VARCHAR(255) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    duration INT NOT NULL,
    track_num INT,
    release_date DATETIME,
    create_date DATETIME NOT NULL,
    modify_date DATETIME NOT NULL,
    play_count INT NOT NULL
);

CREATE TABLE TagTypes (
    name VARCHAR(255) PRIMARY KEY,
    color INT NOT NULL
);
INSERT INTO TagTypes(name, color) VALUES ('', @default_tag_type_color);

CREATE TABLE Tags (
    name VARCHAR(255) PRIMARY KEY,
    type VARCHAR(255),
    description TEXT NOT NULL,
    FOREIGN KEY(type) REFERENCES TagTypes(name) ON DELETE SET NULL
);

CREATE TABLE SongHasTag (
    song_file VARCHAR(255) NOT NULL,
    tag VARCHAR(255) NOT NULL,
    PRIMARY KEY(song_file, tag),
    FOREIGN KEY(song_file) REFERENCES Songs(file_name) ON DELETE CASCADE,
    FOREIGN KEY(tag) REFERENCES Tags(name) ON DELETE CASCADE
);

CREATE TABLE Data (
    k VARCHAR(255) PRIMARY KEY,
    v VARCHAR(255) NOT NULL
);

/* ------------------------------------ Retrieving Procedures ----------------------------------- */

DELIMITER &&

-- Result: the version of the library.
CREATE PROCEDURE Library_get_version() BEGIN
    SELECT version FROM Library;
END &&

-- Result: the default tag type.
CREATE PROCEDURE TagTypes_get_default() BEGIN
    SELECT * FROM TagTypes WHERE name = '';
END &&

-- Result: the `file_name` song.
CREATE PROCEDURE Songs_select(file_name VARCHAR(255)) BEGIN
    SELECT * FROM Songs WHERE file_name = file_name;
END &&

-- Result: all the songs.
CREATE PROCEDURE Songs_select_all() BEGIN
    SELECT * FROM Songs;
END &&

-- Result: the `name` tag.
CREATE PROCEDURE Tags_select(name VARCHAR(255)) BEGIN
    SELECT * FROM Tags WHERE name = name;
END &&

-- Result: all the tags.
CREATE PROCEDURE Tags_select_all() BEGIN
    SELECT * FROM Tags;
END &&

-- Result: the `name` tag type.
CREATE PROCEDURE TagTypes_select(name VARCHAR(255)) BEGIN
    SELECT * FROM TagTypes WHERE name = name;
END &&

-- Result: all the tag types.
CREATE PROCEDURE TagTypes_select_all() BEGIN
    SELECT * FROM TagTypes;
END &&

-- Result: all the tags that `file_name` song has.
CREATE PROCEDURE SongHasTag_select_song_tags(song_file VARCHAR(255)) BEGIN
    SELECT * FROM SongHasTag WHERE song_file = song_file;
END &&

-- Result: all song has tags relationships.
CREATE PROCEDURE SongHasTag_select_all() BEGIN
    SELECT * FROM SongHasTag;
END &&

-- Result: the `k` data entry.
CREATE PROCEDURE Data_select(k VARCHAR(255)) BEGIN
    SELECT * FROM Data where k = k;
END &&

-- Result: all the data entries.
CREATE PROCEDURE Data_select_all() BEGIN
    SELECT * FROM Data;
END &&

/* ------------------------------------- Updating Procedures ------------------------------------ */

-- Inserts/updates a song.
CREATE PROCEDURE Songs_put(
    arg_file_name VARCHAR(255),
    arg_title VARCHAR(255),
    arg_duration INT,
    arg_track_num INT,
    arg_release_date DATETIME,
    arg_create_date DATETIME,
    arg_modify_date DATETIME,
    arg_play_count INT
) BEGIN
    INSERT INTO
        Songs(
            file_name,
            title,
            duration,
            track_num,
            release_date,
            create_date,
            modify_date,
            play_count
        )
    VALUES (
        arg_file_name,
        arg_title,
        arg_duration,
        arg_track_num,
        arg_release_date,
        arg_create_date,
        arg_modify_date,
        arg_play_count
    )
    ON DUPLICATE KEY UPDATE
        title = arg_title,
        duration = arg_duration,
        track_num = arg_track_num,
        release_date = arg_release_date,
        create_date = arg_create_date,
        modify_date = arg_modify_date,
        play_count = arg_play_count;
END &&

-- Removes a song.
CREATE PROCEDURE Songs_remove(file_name VARCHAR(255)) BEGIN
    DELETE FROM Songs WHERE file_name = file_name;
END &&

-- Inserts/updates a tag.
CREATE PROCEDURE Tags_put(arg_name VARCHAR(255), arg_type VARCHAR(255), arg_description TEXT) BEGIN
    INSERT INTO Tags(name, type, description)
    VALUES (arg_name, arg_type, arg_description)
    ON DUPLICATE KEY UPDATE type = arg_type, description = arg_description;
END &&

-- Removes a tag.
CREATE PROCEDURE Tags_remove(name VARCHAR(255)) BEGIN
    DELETE FROM Tags WHERE name = name;
END &&

-- Inserts/updates a tag type.
CREATE PROCEDURE TagTypes_put(name VARCHAR(255), arg_color INT) BEGIN
    INSERT INTO TagTypes(name, color)
    VALUES (name, arg_color)
    ON DUPLICATE KEY UPDATE color = arg_color;
END &&

-- Removes a tag type.
CREATE PROCEDURE TagTypes_remove(name VARCHAR(255)) BEGIN
    DELETE FROM TagTypes WHERE name = name;
END &&

-- Inserts a new song has tag relationship.
CREATE PROCEDURE SongHasTag_put(arg_song_file VARCHAR(255), arg_tag VARCHAR(255)) BEGIN
    INSERT INTO SongHasTag(song_file, tag) VALUES (arg_song_file, arg_tag);
END &&

-- Removes a song has tag relationship.
CREATE PROCEDURE SongHasTag_remove(song_file VARCHAR(255), tag VARCHAR(255)) BEGIN
    DELETE FROM SongHasTag WHERE song_file = song_file AND tag = tag;
END &&

-- Removes all song has tag relationships for a song.
CREATE PROCEDURE SongHasTag_remove_all_for_song(song_file VARCHAR(255)) BEGIN
    DELETE FROM SongHasTag WHERE song_file = song_file;
END &&

-- Inserts/updates a data entry.
CREATE PROCEDURE Data_put(arg_k VARCHAR(255), arg_v VARCHAR(255)) BEGIN
    INSERT INTO Data(k, v)
    VALUES (arg_k, arg_v)
    ON DUPLICATE KEY UPDATE v = arg_v;
END &&

-- Removes a data entry.
CREATE PROCEDURE Data_remove(k VARCHAR(255)) BEGIN
    DELETE FROM Data WHERE k = k;
END &&

DELIMITER ;
