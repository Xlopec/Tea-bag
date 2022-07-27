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

@file:Suppress("TestFunctionName")

package io.github.xlopec.reader.environment

import arrow.core.Either
import io.github.xlopec.reader.app.feature.network.ArticleElement
import io.github.xlopec.reader.app.feature.network.ArticleResponse
import kotlinx.coroutines.delay

fun foreverWaitingResponse(): ArticleResponseProvider = { _, _ ->
    delay(Long.MAX_VALUE)
    error("Should never get here")
}

fun anyArticleRequest(): ArticlePredicate = { _, _ -> true }

fun ArticleResponse(
    vararg articles: ArticleElement
): Either.Right<ArticleResponse> =
    Either.Right(
        ArticleResponse(
            articles.size,
            articles.toList()
        )
    )
