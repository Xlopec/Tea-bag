@file:Suppress("TestFunctionName")

package com.max.reader.environment

import android.app.Application
import androidx.compose.ui.test.IdlingResource
import com.oliynick.max.entities.shared.datatypes.Either
import com.oliynick.max.reader.app.AppException
import com.oliynick.max.reader.app.AppModule
import com.oliynick.max.reader.app.CloseCommandsSink
import com.oliynick.max.reader.app.Environment
import com.oliynick.max.reader.app.storage.LocalStorage
import com.oliynick.max.reader.article.details.ArticleDetailsModule
import com.oliynick.max.reader.article.list.ArticlesModule
import com.oliynick.max.reader.article.list.NewsApi
import com.oliynick.max.reader.network.ArticleResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.DelayController
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope

interface TestEnvironment : Environment, MockNewsApi, DelayController

interface MockNewsApi : NewsApi, IdlingResource {
    infix fun InputPredicate.yields(
        provider: ResponseProvider
    )

    infix fun InputPredicate.yields(
        result: Either<ArticleResponse, AppException>
    )
}

fun TestEnvironment(
    application: Application,
    closeCommands: CloseCommandsSink,
    dispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()
): TestEnvironment =
    object : TestEnvironment,
        AppModule<Environment> by AppModule(closeCommands),
        ArticlesModule<Environment> by ArticlesModule(application),
        ArticleDetailsModule<Environment> by ArticleDetailsModule(application),
        MockNewsApi by TestNewsApi(dispatcher),
        LocalStorage by LocalStorage(application),
        CoroutineScope by TestCoroutineScope(dispatcher),
        DelayController by dispatcher {

        init {
            pauseDispatcher()
        }
    }
