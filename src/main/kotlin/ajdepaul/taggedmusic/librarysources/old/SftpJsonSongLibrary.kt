///*
// * Copyright © 2021 Anthony DePaul
// * Licensed under the MIT License https://ajdepaul.mit-license.org/
// */
//package ajdepaul.taggedmusic.librarysources.old
//
//import ajdepaul.taggedmusic.Song
//import ajdepaul.taggedmusic.Tag
//import ajdepaul.taggedmusic.TagType
//import ajdepaul.taggedmusic.songlibraries.CachedSongLibrary
//import kotlinx.collections.immutable.PersistentMap
//import kotlinx.collections.immutable.PersistentSet
//
//class SftpJsonSongLibrary : CachedSongLibrary() {
//
//    override var defaultTagType: TagType
//        get() = TODO("Not yet implemented")
//        set(value) {}
//    override val version: String
//        get() = TODO("Not yet implemented")
//
///* -------------------------------------------- Songs ------------------------------------------- */
//
//    override fun putSongImpl(fileName: String, song: Song) {
//        TODO("Not yet implemented")
//    }
//
//    override fun removeSong(fileName: String) {
//        TODO("Not yet implemented")
//    }
//
//    override fun hasSong(fileName: String): Boolean {
//        TODO("Not yet implemented")
//    }
//
//    override fun getSong(fileName: String): Song? {
//        TODO("Not yet implemented")
//    }
//
//    override fun getAllSongs(): PersistentMap<String, Song> {
//        TODO("Not yet implemented")
//    }
//
//    override fun getSongsByTags(
//        includeTags: PersistentSet<String>,
//        excludeTags: PersistentSet<String>
//    ): PersistentMap<String, Song> {
//        TODO("Not yet implemented")
//    }
//
///* -------------------------------------------- Tags -------------------------------------------- */
//
//    override fun putTagImpl(tagName: String, tag: Tag) {
//        TODO("Not yet implemented")
//    }
//
//    override fun removeTag(tagName: String) {
//        TODO("Not yet implemented")
//    }
//
//    override fun hasTag(tagName: String): Boolean {
//        TODO("Not yet implemented")
//    }
//
//    override fun getTag(tagName: String): Tag? {
//        TODO("Not yet implemented")
//    }
//
//    override fun getAllTags(): PersistentMap<String, Tag> {
//        TODO("Not yet implemented")
//    }
//
///* ------------------------------------------ Tag Types ----------------------------------------- */
//
//    override fun putTagTypeImpl(tagTypeName: String, tagType: TagType) {
//        TODO("Not yet implemented")
//    }
//
//    override fun removeTagType(tagTypeName: String) {
//        TODO("Not yet implemented")
//    }
//
//    override fun hasTagType(tagTypeName: String): Boolean {
//        TODO("Not yet implemented")
//    }
//
//    override fun getTagType(tagTypeName: String): TagType? {
//        TODO("Not yet implemented")
//    }
//
//    override fun getAllTagTypes(): PersistentMap<String, TagType> {
//        TODO("Not yet implemented")
//    }
//}
