package com.oliynick.max.reader.app.domain

import com.oliynick.max.reader.app.misc.coerceIn
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentSetOf
import kotlin.jvm.JvmInline

enum class FilterType {
    Regular, Favorite, Trending
}

/**
 * Represents user query, never empty
 */
@JvmInline
value class Query private constructor(
    val value: String
) {
    companion object {

        private const val MaxQueryLength = 500U
        private const val MinQueryLength = 1U

        fun of(
            input: String?
        ) = input?.coerceIn(MinQueryLength, MaxQueryLength)?.replace("\n", "")?.let(::Query)
    }
}

data class Filter(
    val type: FilterType,
    val query: Query? = null,
    val sources: PersistentSet<SourceId> = persistentSetOf(),
) {
    companion object {
        /**
         * API doesn't accept more than 20 sources per request
         */
        const val StoreSourcesLimit = 20U
    }
}