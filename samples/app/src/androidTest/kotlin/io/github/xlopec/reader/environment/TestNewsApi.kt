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

package io.github.xlopec.reader.environment

import androidx.compose.ui.test.IdlingResource
import io.github.xlopec.reader.app.AppException
import io.github.xlopec.reader.app.feature.article.list.Paging
import io.github.xlopec.reader.app.feature.network.ArticleResponse
import io.github.xlopec.reader.app.feature.network.SourcesResponse
import io.github.xlopec.reader.app.model.Query
import io.github.xlopec.reader.app.model.SourceId
import io.github.xlopec.tea.data.Either
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.withContext

typealias ArticlePredicate = (input: Query?, paging: Paging) -> Boolean

typealias ArticleResponseProvider = suspend (input: Query?, paging: Paging) -> Either<ArticleResponse, AppException>

data class ArticlesMockData(
    val predicate: ArticlePredicate,
    val response: ArticleResponseProvider,
)

@JvmInline
value class SourcesMockData(
    val response: Either<SourcesResponse, AppException>,
)

class TestNewsApi(
    private val dispatcher: TestCoroutineDispatcher,
) : MockNewsApi, IdlingResource {

    private val articlesMockData = mutableListOf<ArticlesMockData>()
    private val sourcesMockData = mutableListOf<SourcesMockData>()

    override infix fun ArticlePredicate.yields(
        provider: ArticleResponseProvider,
    ) {
        articlesMockData += ArticlesMockData(this, provider)
    }

    override infix fun ArticlePredicate.yields(
        result: Either<ArticleResponse, AppException>,
    ) {
        articlesMockData += ArticlesMockData(this, { _, _ -> result })
    }

    override fun yieldsSourcesResponse(result: Either<SourcesResponse, AppException>) {
        sourcesMockData += SourcesMockData(result)
    }

    override suspend fun fetchFromEverything(
        query: Query?,
        sources: ImmutableSet<SourceId>,
        paging: Paging,
    ): Either<ArticleResponse, AppException> {
        return dequeArticlesResponse(query, paging)
    }

    override suspend fun fetchTopHeadlines(
        query: Query?,
        sources: ImmutableSet<SourceId>,
        paging: Paging,
    ): Either<ArticleResponse, AppException> {
        return dequeArticlesResponse(query, paging)
    }

    override suspend fun fetchNewsSources(): Either<SourcesResponse, AppException> {
        return dequeSourcesResponse()
    }

    private suspend fun dequeSourcesResponse() =
        withContext(dispatcher) {
            require(sourcesMockData.isNotEmpty()) { "there are no mock data for sources api" }
            sourcesMockData.removeAt(0).response
        }

    private suspend fun dequeArticlesResponse(
        input: Query?,
        paging: Paging,
    ) = withContext(dispatcher) {

        val i = articlesMockData.indexOfFirst { (predicate, _) -> predicate(input, paging) }

        if (i < 0) {
            error(
                "Couldn't find matching response for arguments input=$input, paging=$paging,\n" +
                        "registered $articlesMockData"
            )
        }

        val (_, response) = articlesMockData.removeAt(i)

        response(input, paging)
    }

    override val isIdleNow: Boolean
        get() = runBlocking(dispatcher) { articlesMockData.isEmpty() && sourcesMockData.isEmpty() }
}
