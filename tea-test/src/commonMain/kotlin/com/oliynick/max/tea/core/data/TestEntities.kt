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

@file:Suppress("FunctionName")

package com.oliynick.max.tea.core.data

import com.oliynick.max.entities.shared.RandomUUID
import com.oliynick.max.entities.shared.UUID
import com.oliynick.max.entities.shared.Url
import com.oliynick.max.entities.shared.UrlFor
import kotlin.jvm.JvmInline

data class User(
    val id: Id,
    val name: Name,
    val photos: List<Photo>,
    val avatar: Url? = null
)

@JvmInline
value class Id(
    val uuid: UUID
)

data class Name(
    val value: String
) {
    init {
        require(value.isNotEmpty())
    }
}

@JvmInline
value class Photo(
    val url: Url
)

fun RandomId() = Id(RandomUUID())

fun Photo(urlSpec: String) = Photo(UrlFor(urlSpec))

fun Avatar(s: String) = UrlFor(s)
