package core.data

import java.net.URL
import java.util.*

data class User(
    val id: Id,
    val name: Name,
    val photos: List<Photo>,
    val avatar: URL? = null
)

inline class Id(val uuid: UUID)

data class Name(val value: String) {
    init {
        require(value.isNotEmpty())
    }
}

inline class Photo(val url: URL)

fun randomId() = Id(UUID.randomUUID())

fun photo(urlSpec: String) = Photo(URL(urlSpec))