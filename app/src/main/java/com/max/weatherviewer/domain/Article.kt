package com.max.weatherviewer.domain

import java.net.URL

data class Article(
    val url: URL,
    val title: Title,
    val author: Author,
    val description: Description,
    val urlToImage: URL?,
    val isFavorite: Boolean = false
)

data class Title(val value: String) {
    init {
        require(value.isNonEmpty)
    }
}

data class Author(val value: String) {
    init {
        require(value.isNonEmpty)
    }
}

data class Description(val value: String) {
    init {
        require(value.isNonEmpty)
    }
}

fun Article.toggleFavorite(): Article = copy(isFavorite = !isFavorite)

private inline val String.isNonEmpty get() = isNotEmpty() && isNotBlank()