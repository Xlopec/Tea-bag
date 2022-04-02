package com.max.reader.environment

import androidx.compose.ui.test.IdlingResource
import com.oliynick.max.reader.app.AppException
import com.oliynick.max.reader.app.domain.Query
import com.oliynick.max.reader.app.domain.SourceId
import com.oliynick.max.reader.app.feature.article.list.Paging
import com.oliynick.max.reader.app.feature.network.ArticleResponse
import com.oliynick.max.reader.app.feature.network.SourcesResponse
import com.oliynick.max.tea.data.Either
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

