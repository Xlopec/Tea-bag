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

@file:Suppress("FunctionName")

package io.github.xlopec.reader.app.feature.article.list

import arrow.core.Either
import io.github.xlopec.reader.app.AppException
import io.github.xlopec.reader.app.feature.network.ArticleResponse
import io.github.xlopec.reader.app.feature.network.SourcesResponse
import io.github.xlopec.reader.app.model.Query
import io.github.xlopec.reader.app.model.SourceId
import kotlinx.collections.immutable.ImmutableSet

public interface NewsApi {

    public suspend fun fetchFromEverything(
        query: Query?,
        sources: ImmutableSet<SourceId>,
        paging: Paging
    ): Either<AppException, ArticleResponse>

    public suspend fun fetchTopHeadlines(
        query: Query?,
        sources: ImmutableSet<SourceId>,
        paging: Paging
    ): Either<AppException, ArticleResponse>

    public suspend fun fetchNewsSources(): Either<AppException, SourcesResponse>
}
