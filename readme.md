# Tagged Music

[![GitHub release](https://img.shields.io/github/downloads/ajdepaul/TaggedMusic/v1.0/total)](https://github.com/ajdepaul/TaggedMusic/releases/tag/v1.0)

This library provides a large amount of control over a collection of music through the use of tags.
Tagged Music makes it easy to store a set of audio files in [numerous ways](#Audio-File-Sources),
associate each song with a set of tags, and retrieve songs in a flexible way based on those tags.
All maps and sets retrieved by this library are
[persistent](https://github.com/Kotlin/kotlinx.collections.immutable), making them easy to filter
and modify in a thread-safe way.

To get the best use out of the persistent maps and sets, be sure to use the functions implemented by
the [Immutable collections library](https://github.com/Kotlin/kotlinx.collections.immutable)
discussed
[here](https://github.com/Kotlin/kotlinx.collections.immutable/blob/master/README.md),
[here](https://github.com/Kotlin/kotlinx.collections.immutable/blob/master/proposal.md), and
[here](https://github.com/Kotlin/kotlinx.collections.immutable/blob/master/core/commonMain/src/implementations/immutableMap/PersistentHashMap.kt)
(+, -, put, putAll, remove, removeAll, builder, etc.).

### Building

Run `./gradlew build` to compile. The output is located in [build/libs](build/libs).

### Testing

Run `./gradlew test` to run tests. Tests for remote sources (e.g. MySQL Library Source, Sftp Audio
File Source) require a server to test modifications. To run these tests properly, edit the
`server.properties` files located in the [test resources](src/test/resources/ajdepaul/taggedmusic).

## Song Libraries

Song libraries are set up as four maps: 

| Map        | Key                                                             | Value                    |
| ---------- | --------------------------------------------------------------- | ------------------------ |
| `songs`    | file name in [audio file source](#Audio-File-Sources): `String` | song data: `Song`        |
| `tags`     | tag name: `String`                                              | tag data: `Tag`          |
| `tagTypes` | tag type name: `String`                                         | tag type data: `TagType` |
| `data`     | key: `String`                                                   | value: `String`          |

There is also an additional default tag type value stored that should be used when a tag's type is
set to `null`.

You can interact with each of the maps by utilizing the `has`, `get`, `getAll`, `put` and `remove`
functions provided by the SongLibrary (e.g. `putSong(fileName: String, song: Song)`,
`getSong(fileName: String): Song`). The `getAll` functions will retrieve a
[persistent map](https://github.com/Kotlin/kotlinx.collections.immutable) of all the data in that
map from the [library source](#Library-Sources). These functions are often updating/retrieving data
to/from a remote library source and are not thread-safe. Because of this, all function calls on a
Song Library should be synchronized.

### Filtering songs

may be optimized for the library source

The `getSongsByTags(includeTags, excludeTags)` function will return a map of songs that will include
songs that contains *all* the tags from the `includeTags` set of tag names and will exclude songs
that contains *any* of the tags from the `excludeTags` set of tag names. An empty set for
`includeTags` will include every song, while an empty set for `excludeTags` will exclude no songs.
Both arguments default to empty sets if you would like to only include one or the other.

This function's implementation is left up to the [library source](#Library-Sources) so that the
library source can create the most efficient implementation specific to its source.

### The Data Map

Unlike the other three maps, the `data` map has no direct impact on how songs are organized. The
purpose of this map is to provide a convenient way of storing additional data for your application
that you can save to a [library source](#Library-Sources). For instance, if your application has a
theme setting that you would like to stay synchronized for the user across devices, you can store
the selected theme in the data map so that each device is able to share the same value. To reduce
the chance of your application's data conflicting with another, it is recommended to attach a unique
prefix to the data keys specific to your application.

### Cacheless vs Cached

Depending on the [library source](#Library-Sources) used, updating/retrieving data to/from can be
slow. Because of this, it may be better to keep a cache of the library data stored in memory to
minimize the amount of time waiting on a potentially slow remote library source. To achieve this,
song libraries have two implementations: `CachelessSongLibrary` and `CachedSongLibrary`.

`CachelessSongLibrary`s do not store any library data in memory and will call whatever
retrieval/update function of the library source is required for every retrieval/update. This can be
useful if you know that the library source in use is fast and want to save on memory usage.

`CachedSongLibrary`s, on the other hand, will store all four of the maps in memory as well as the
default tag type. This way when retrieving data about a song library, it is taken from memory
instead of the library source directly. Likewise, when initially updating a song library's data, the
cache receives the updates immediately and not the library source. To push any updates made to the
library source, call the `commit` function to push all the updates together as a batch to the
library source.

## Data Classes

[Songs](#Songs), [tags](#Tags), and [tag types](#Tag-Types) all are all persistent data types which
makes them easy to work with in a thread-safe way when stored in
[persistent collections](https://github.com/Kotlin/kotlinx.collections.immutable).

Because they are persistent, they must be modified through their `mutate(mutator)` functions. These
functions do not modify the values themselves, but return new instances with the changes made
applied through the `mutator`. To modify 

If you want to modify the songs, tags, or tag types in the song library you
must remember to use the appropriate `put` function to update them. For example:

``` Kotlin
val mutatedSong = songLibrary.songs["song.mp3"].mutate { this.tags += "A" }
songLibrary.put("song.mp3", mutatedSong)
```

### Songs

| Property      | Type                    | Default                 | Description                                   |
| ------------- | ----------------------- | ----------------------- | --------------------------------------------- |
| `title`       | `String`                | required                | Title of the song                             |
| `duration`    | `Int`                   | required                | Length of song in milliseconds                |
| `trackNum`    | `Int?`                  | `null`                  | Track number of the song in an album          |
| `releaseDate` | `LocalDateTime?`        | `null`                  | When the song was released                    |
| `createDate`  | `LocalDateTime`         | `LocalDateTime.now()`   | When the song was added to the library        |
| `modifyDate`  | `LocalDateTime`         | `LocalDateTime.now()`   | When the song was last modified               |
| `playCount`   | `Int`                   | `0`                     | How many times the song has been listened to  |
| `tags`        | `PersistentSet<String>` | `persistentHashSetOf()` | The set of tag strings that describe the song |

The songs' `mutate` function includes an additional optional argument `updateModifyDate` which will
update the `lastModified` value to `LocalDateTime.now()` if set to true and does so by default. The
`modifyDate` and `createDate` property cannot be modified directly through the `mutator`.

### Tags

| Property      | Type      | Default | Description                                    |
| ------------- | --------- | ------- | ---------------------------------------------- |
| `type`        | `String?` | null    | Tag type string used to classify this tag      | 
| `description` | `String?` | null    | Text describing what songs should get this tag |

A set of tags are used to describe a song. When `type` is `null`, the song library default tag type
should be used. 

### Tag Types

| Property | Type  | Default  | Description                                  |
| -------- | ----- | -------- | -------------------------------------------- |
| `color`  | `Int` | required | The display color used for tags of this type |

Tag types are used to group similar tags together. 

## Library Sources

To sync [song library](#Song-Libraries) data across multiple applications or devices, library
sources are used. A song library will retrieve data and update the library source provided to it
[when it is required to do so](#Cacheless-vs-Cached) so that, next time a user loads their library,
the data remains consistent. New library sources can be created by implementing the `LibrarySource`
interface, but some are provided by Tagged Music already.

### JSON Library Source

The `JsonLibrarySource` provides an easy way of storing library data locally. It simply stores
library data in a single JSON text file somewhere on the device. Because the file is only stored
locally, it is not possible to load the same library data from a different device.

When retrieving data from this library source, the entire JSON file has to be parsed, so it is
recommended to use a [cached song library](#Cacheless-vs-Cached) when using a JSON library source.

### SFTP Library Source

The `SftpLibrarySource` gives remote functionality to library sources that rely on local files by
keeping the files stored on an SFTP server. For example, an SFTP library source can keep the JSON
file of a JSON library source stored remotely to sync a song library between devices.

The required files are pulled from the SFTP server only once on instantiation. All updates are then
applied to those files by the nested local library source. Once those files are updated, they are
then pushed onto the SFTP server.

### MySQL Library Source

The `MysqlLibrarySource` stores song library data on a MySQL server. To properly initialize a MySQL
database to be used as a library source, use the Kotlin script in the [mysql_init](mysql_init)
directory.

For help with configuring the initialization script, run:
`kotlinc -script MysqlDatabaseInit.kts help`.

## Audio File Sources

Audio file sources are used to store and retrieve audio files by file name. The key string used in a
[song library's song map](#Song-Libraries) match the audio file names used in an audio file source.
New audio file sources can be created by implementing the `AudioFileSource` interface, but some are
provided by Tagged Music already.

### Local Audio File Source

The `LocalAudioFileSource`

### Sftp Audio File Source

The `SftpAudioFileSource`

### Cached Audio File Source

The `CachedAudioFileSource`

## Future Plans

- Tag synonyms: multiple names for the same tag
  - For example, "Childish Gambino" and "Donald Glover"
- Tag assumptions: assigning a song a tag automatically gives it another
  - For example, songs tagged with "house" or "dubstep" should get the "electronic" tag
- Tag recommendations: assigning a song with one tag might frequently be paired with another and
  should be recommended
  - For example, songs tagged with "happy" could get recommended the "summer" tag
- Additional functionality to tag types
  - Potentially ordinal tag types?
- Optimize SQL library source retrievals
- Choose what to cache in a cached song library

## Copyright and License

Copyright © 2021 Anthony DePaul  
Licensed under the [MIT License](LICENSE)
