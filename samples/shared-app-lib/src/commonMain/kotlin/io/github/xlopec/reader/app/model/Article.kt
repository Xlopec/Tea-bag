/*
 * MIT License
 *
 * Copyright (c) 2022. Maksym Oliinyk.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
@file:OptIn(kotlin.contracts.ExperimentalContracts::class)

package io.github.xlopec.reader.app.model

import androidx.compose.runtime.Immutable
import io.github.xlopec.tea.data.Date
import io.github.xlopec.tea.data.Url
import kotlin.contracts.contract
import kotlin.jvm.JvmInline

@Immutable
public data class Article(
    val url: Url,
    val title: Title,
    val author: Author?,
    val description: Description?,
    val urlToImage: Url?,
    val published: Date,
    val isFavorite: Boolean,
    val source: SourceId?
)

@JvmInline
public value class Title(
    public val value: String,
) {

    internal companion object;

    init {
        require(isValid(value)) { "Invalid title value, was $value" }
    }
}

@JvmInline
public value class Author(
    public val value: String,
) {

    internal companion object;

    init {
        require(isValid(value)) { "Invalid author value, was $value" }
    }
}

@JvmInline
public value class Description(
    public val value: String,
) {

    internal companion object;

    init {
        require(isValid(value)) { "Invalid description value, was $value" }
    }
}

internal fun Title.Companion.isValid(
    s: String?,
): Boolean {
    contract {
        returns(true) implies (s is String)
    }

    return s.isNonEmpty()
}

internal fun Title.Companion.tryCreate(
    s: String?,
) = if (isValid(s)) Title(s) else null

internal fun Author.Companion.isValid(
    s: String?,
): Boolean {
    contract {
        returns(true) implies (s is String)
    }

    return s.isNonEmpty()
}

internal fun Author.Companion.tryCreate(
    s: String?,
) = if (isValid(s)) Author(s) else null

internal fun Description.Companion.isValid(
    s: String?,
): Boolean {
    contract {
        returns(true) implies (s is String)
    }

    return s.isNonEmpty()
}

internal fun Description.Companion.tryCreate(
    s: String?,
) = if (isValid(s)) Description(s) else null

internal fun Article.toggleFavorite(): Article = copy(isFavorite = !isFavorite)

internal fun <T> tryCreate(
    s: String?,
    constructor: (String) -> T
) = if (s?.isNonEmpty() == true) constructor(s) else null

private fun String?.isNonEmpty(): Boolean {
    contract {
        returns(true) implies (this@isNonEmpty is String)
    }

    return !isNullOrEmpty() && !isNullOrBlank()
}
