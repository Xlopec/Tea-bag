@file:Suppress("TestFunctionName")

package com.max.reader.environment

import android.app.Application
import androidx.compose.ui.test.IdlingResource
import com.oliynick.max.reader.app.AppException
import com.oliynick.max.reader.app.AppModule
import com.oliynick.max.reader.app.Environment
import com.oliynick.max.reader.app.feature.article.details.ArticleDetailsModule
import com.oliynick.max.reader.app.feature.article.list.AndroidShareArticle
import com.oliynick.max.reader.app.feature.article.list.ArticlesModule
import com.oliynick.max.reader.app.feature.article.list.NewsApi
import com.oliynick.max.reader.app.feature.filter.FiltersModule
import com.oliynick.max.reader.app.feature.network.ArticleResponse
import com.oliynick.max.reader.app.feature.network.SourcesResponse
import com.oliynick.max.reader.app.feature.storage.LocalStorage
import com.oliynick.max.tea.data.Either
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.DelayController
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope

interface TestEnvironment : Environment, MockNewsApi, DelayController

interface MockNewsApi : NewsApi, IdlingResource {
    infix fun ArticlePredicate.yields(
        provider: ArticleResponseProvider
    )

    infix fun ArticlePredicate.yields(
        result: Either<ArticleResponse, AppException>
    )

    fun yieldsSourcesResponse(
        result: Either<SourcesResponse, AppException>
    )
}

fun TestEnvironment(
    application: Application,
    dispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()
): TestEnvironment =
    object : TestEnvironment,
        AppModule<Environment> by AppModule(),
        ArticlesModule<Environment> by ArticlesModule(AndroidShareArticle(application)),
        ArticleDetailsModule by ArticleDetailsModule(application),
        FiltersModule<Environment> by FiltersModule(),
        MockNewsApi by TestNewsApi(dispatcher),
        LocalStorage by LocalStorage(application),
        CoroutineScope by TestCoroutineScope(dispatcher),
        DelayController by dispatcher {

        init {
            pauseDispatcher()
        }
    }
