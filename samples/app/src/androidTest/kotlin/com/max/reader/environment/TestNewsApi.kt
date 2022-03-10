package com.max.reader.environment

import androidx.compose.ui.test.IdlingResource
import com.oliynick.max.entities.shared.datatypes.Either
import com.oliynick.max.reader.app.AppException
import com.oliynick.max.reader.app.domain.Query
import com.oliynick.max.reader.app.domain.SourceId
import com.oliynick.max.reader.app.feature.article.list.Paging
import com.oliynick.max.reader.app.feature.network.ArticleResponse
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.withContext

typealias InputPredicate = (input: String, paging: Paging) -> Boolean

typealias ResponseProvider = suspend (input: String, paging: Paging) -> Either<ArticleResponse, AppException>

data class MockNewsData(
    val predicate: InputPredicate,
    val response: ResponseProvider
)

class TestNewsApi(
    private val dispatcher: TestCoroutineDispatcher
) : MockNewsApi, IdlingResource {

    private val requests = mutableListOf<MockNewsData>()

    override infix fun InputPredicate.yields(
        provider: ResponseProvider
    ) {
        requests += MockNewsData(this, provider)
    }

    override infix fun InputPredicate.yields(
        result: Either<ArticleResponse, AppException>
    ) {
        requests += MockNewsData(this, { _, _ -> result })
    }

    override suspend fun fetchFromEverything(
        query: Query?,
        sources: ImmutableSet<SourceId>,
        paging: Paging
    ): Either<ArticleResponse, AppException> {
        return dequeResponse(query, paging)
    }

    override suspend fun fetchTopHeadlines(
        query: Query?,
        sources: ImmutableSet<SourceId>,
        paging: Paging
    ): Either<ArticleResponse, AppException> {
        return dequeResponse(query, paging)
    }

    private suspend fun dequeResponse(
        input: String,
        paging: Paging
    ): Either<ArticleResponse, AppException> = withContext(dispatcher) {

        val i = requests.indexOfFirst { (predicate, _) -> predicate(input, paging) }

        if (i < 0) {
            error(
                "Couldn't find matching response for arguments input=$input, paging=$paging,\n" +
                        "registered $requests"
            )
        }

        val (_, response) = requests.removeAt(i)

        response(input, paging)
    }

    override val isIdleNow: Boolean
        get() = runBlocking(dispatcher) { requests.isEmpty() }

}

