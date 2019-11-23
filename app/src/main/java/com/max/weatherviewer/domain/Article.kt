package com.max.weatherviewer.domain

import java.net.URL

data class Article(
    val url: URL,
    val title: Title,
    val author: Author,
    val description: Description,
    val urlToImage: URL?
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

private inline val String.isNonEmpty get() = isNotEmpty() && isNotBlank()