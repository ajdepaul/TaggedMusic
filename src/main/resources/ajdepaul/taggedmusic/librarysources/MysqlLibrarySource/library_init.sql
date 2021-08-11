-- Script for initializing a MySQL server with a database that can be used as a MysqlLibrarySource.
-- <<version>> and <<default_tag_type_color>> will be replaced by the MysqlLibrarySource
-- initialization constructor.

CREATE TABLE library (
    version VARCHAR(255) PRIMARY KEY
);
INSERT INTO library VALUES ('<<version>>')

CREATE TABLE songs (
    file_name VARCHAR(255) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    duration INT NOT NULL,
    track_num INT,
    year INT,
    date_created DATETIME NOT NULL,
    last_modified DATETIME NOT NULL,
    play_count INT NOT NULL
);

CREATE TABLE tag_types (
    name VARCHAR(255) PRIMARY KEY,
    color INT NOT NULL
);
INSERT INTO tag_types VALUES ('', <<default_tag_type_color>>);

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

CREATE TABLE data (
    k VARCHAR(255) PRIMARY KEY,
    v VARCHAR(255) NOT NULL
);