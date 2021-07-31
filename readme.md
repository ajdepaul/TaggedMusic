TODO for the best efficiency with PersisentSets and PersisentMaps, use the functions implemented by
kotlinx-collections-immutable discussed
[here](https://github.com/Kotlin/kotlinx.collections.immutable/blob/master/README.md]) and
[here](https://github.com/Kotlin/kotlinx.collections.immutable/blob/master/proposal.md).
(+, -, put, putAll, remove, removeAll, etc.)

also this https://github.com/Kotlin/kotlinx.collections.immutable/blob/master/core/commonMain/src/implementations/immutableMap/PersistentHashMap.kt
this too import kotlinx.collections.immutable.*

# Tagged Music

Applying folksonomy to music libraries. This library provides a large amount of control over a
collection of music through the use of tags.

Run `./gradlew build` to compile.

# SongLibrary

Song libraries are set up as three read-only maps:

| Map        | Key                             | Value                    |
| ---------- | ------------------------------- | ------------------------ |
| `songs`    | file name in provider: `String` | song data: `Song`        |
| `tags`     | tag name: `String`              | tag data: `Tag`          |
| `tagTypes` | tag type name: `String?`        | tag type data: `TagType` |

These maps are all [immutable maps](https://github.com/Kotlin/kotlinx.collections.immutable)
containing immutable types making them all thread safe. The tag types `null` key refers to the
default tag type which can also be referred to through the `defaultTagType` property. The default
tag type is used when a tag does not have a tag type specified and is also the tag type given to
tags that have their tag type removed.

You can modify each of the maps by utilizing the `put` and `remove` functions provided by song (e.g.
`putTag(tagName, tag)`, `removeTag(tagName)`).

The `tagFilter(includeTags, excludeTags)` function will return a map of songs (with the same
key-value structure as the `songs` map) that will include songs that contains *all* the tags from
the `includeTags` set of tag names and will exclude songs that contains *any* of the tags from the
`excludeTags` set of tag names. An empty set for `includeTags` would include every song, while an
empty set for `excludeTags` would exclude no songs. Both arguments default to empty sets if you
would like to only include one or the other.

Because each of the maps are public read-only immutable types, you can also filter and sort through
them without modifying the maps stored in the song library.

Song libraries can be converted into a json friendly data class format by using `toJsonData()`. The
json data class contains a `jsonVersion` property that you may want to check if you may be dealing
with multiple versions. The json files should only be managed through this library as [some
libraries may cause null pointer
exceptions](https://bytes.babbel.com/en/articles/2018-05-25-kotlin-gson-nullability.html).

# Data Classes

Songs, tags, and tag types all are all immutable data types that are json friendly and thread safe.
Because they are immutable, they must be modified through their `mutate(mutator)` functions. These
functions return new instances that have changed made applied through the mutator. The mutator takes
the form `MutableTag.() -> Unit` (or whichever mutable equivalent when appropriate). With this form,
when passing the the mutator lambda, the properties can be referred through `this`. Because this
function returns a new instance rather than mutating the object itself, if you want to modify the
songs, tags, or tag types in the song library you must remember to use the appropriate `put`
function to update them. For example:

``` Kotlin
val mutatedSong = songLibrary.songs["song.mp3"].mutate { this.tags += "A" }
songLibrary.put("song.mp3", mutatedSong)
```

## Songs

| Property       | Type                    | Default                 |
| -------------- | ----------------------- | ----------------------- |
| `title`        | `String`                | required                |
| `duration`     | `Int`                   | required                |
| `artist`       | `String?`               | `null`                  |
| `album`        | `String?`               | `null`                  |
| `trackNum`     | `Int?`                  | `null`                  |
| `year`         | `Int?`                  | `null`                  |
| `lastModified` | `LocalDateTime`         | `LocalDateTime.now()`   |
| `playCount`    | `Int`                   | `0`                     |
| `tags`         | `PersistentSet<String>` | `persistentHashSetOf()` |

The tag set contains tag names that match the keys in the song library `tags` map.

The songs' `mutate` function include an additional optional parameter `updateLastModified` which
will update the `lastModified` value to `LocalDateTime.now()` if set to true, which is the default
value. The `lastModified` property cannot be modified through the `mutator` argument otherwise.

## Tags

| Property      | Type      | Default |
| ------------- | --------- | ------- |
| `type`        | `String?` | null    |
| `description` | `String?` | null    |

The type is a tag type name that matches the keys in the song library `tagTypes` map. When `type` is
`null`, the song library default tag type is used. 

## Tag Types

| Property | Type  | Default  |
| -------- | ----- | -------- |
| `color`  | `Int` | required |

# Providers

The provider interfaces are meant to standardize the way song libraries and songs can be stored in a
remote location.

## Library Provider

| Function | Description                                                                                 |
| -------- | ------------------------------------------------------------------------------------------- |
| `pull`   | Returns the song library associated with this provider or `null` if failed.                 |
| `push`   | Pushes the provided song library to the provider. Returns true on success, false otherwise. |

## Song Provider

| Function     | Description                                                                                                                |
| ------------ | -------------------------------------------------------------------------------------------------------------------------- |
| `hasSong`    | Returns true if the provider has the song available to pull.                                                               |
| `pushSong`   | Pushes the specified local song file to the specified file name on the provider. Returns true on success, false otherwise. |
| `pullSong`   | Retrieves the song file from the provider and returns the local path to the file or null if failed.                        |
| `removeSong` | Removes the specified song file from the provider. Returns true on success, false otherwise.                               |

## Local Providers

`LocalLibraryProvider` and `LocalSongProvider` are two basic implementations of these providers. By
specifying a path, the providers will manage song library or song files on the local machine.
