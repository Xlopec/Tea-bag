/*
 * MIT License
 *
 * Copyright (c) 2021. Maksym Oliinyk.
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

package com.oliynick.max.reader.domain

import kotlin.contracts.contract
import kotlin.jvm.JvmInline

expect class Url

expect fun String.toUrl(): Url

expect fun Url.toExternalValue(): String

expect class Date

expect fun now(): Date

expect fun fromMillis(
    millis: Long
): Date

expect fun Date.toMillis(): Long

data class Article(
    val url: Url,
    val title: Title,
    val author: Author?,
    val description: Description?,
    val urlToImage: Url?,
    val published: Date,
    val isFavorite: Boolean,
)

@JvmInline
value class Title(
    val value: String,
) {

    companion object;

    init {
        require(isValid(value)) { "Invalid title value, was $value" }
    }
}

@JvmInline
value class Author(
    val value: String,
) {

    companion object;

    init {
        require(isValid(value)) { "Invalid author value, was $value" }
    }
}

@JvmInline
value class Description(
    val value: String,
) {

    companion object;

    init {
        require(isValid(value)) { "Invalid description value, was $value" }
    }
}

fun Title.Companion.isValid(
    s: String?,
): Boolean {
    contract {
        returns(true) implies (s is String)
    }

    return s.isNonEmpty()
}

fun Title.Companion.tryCreate(
    s: String?,
) = if (isValid(s)) Title(s) else null

fun Author.Companion.isValid(
    s: String?,
): Boolean {
    contract {
        returns(true) implies (s is String)
    }

    return s.isNonEmpty()
}

fun Author.Companion.tryCreate(
    s: String?,
) = if (isValid(s)) Author(s) else null

fun Description.Companion.isValid(
    s: String?,
): Boolean {
    contract {
        returns(true) implies (s is String)
    }

    return s.isNonEmpty()
}

fun Description.Companion.tryCreate(
    s: String?,
) = if (isValid(s)) Description(s) else null

fun Article.toggleFavorite(): Article = copy(isFavorite = !isFavorite)

private fun String?.isNonEmpty(): Boolean {
    contract {
        returns(true) implies (this@isNonEmpty is String)
    }

    return !isNullOrEmpty() && !isNullOrBlank()
}
