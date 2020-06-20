package TaggedMusic

import com.google.gson.Gson

class SongLibrary() {
    
    private val _tags = mutableSetOf<String>()
    public val tags: Set<String> get() = _tags

    public val songs = mutableSetOf<Song>()

    /**
     * @param inclTags tags to include
     * @param exclTags tags to exclude
     */
    fun tagFilter(inclTags: Set<String>, exclTags: Set<String>): Set<Song> {
        return songs.filter { song -> song.tags.any { tag -> inclTags.contains(tag) }}
                    .filter { song -> song.tags.any { tag -> !exclTags.contains(tag) }}
                    .toSet()
    }

    fun toJson(): String {
        return Gson().toJson(this)
    }

    companion object {

        fun fromJson(json: String): SongLibrary {
            return Gson().fromJson(json, SongLibrary::class.java)
        }
    }
}
