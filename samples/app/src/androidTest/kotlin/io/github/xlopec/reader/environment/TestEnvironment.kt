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

import android.app.Application
import androidx.compose.ui.test.IdlingResource
import arrow.core.Either
import io.github.xlopec.reader.app.AppException
import io.github.xlopec.reader.app.AppModule
import io.github.xlopec.reader.app.Environment
import io.github.xlopec.reader.app.feature.article.details.ArticleDetailsModule
import io.github.xlopec.reader.app.feature.article.list.AndroidShareArticle
import io.github.xlopec.reader.app.feature.article.list.ArticlesModule
import io.github.xlopec.reader.app.feature.article.list.NewsApi
import io.github.xlopec.reader.app.feature.filter.FiltersModule
import io.github.xlopec.reader.app.feature.network.ArticleResponse
import io.github.xlopec.reader.app.feature.network.SourcesResponse
import io.github.xlopec.reader.app.feature.storage.LocalStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.DelayController
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineExceptionHandler
import kotlinx.coroutines.test.createTestCoroutineScope

interface TestEnvironment : Environment, MockNewsApi, DelayController

interface MockNewsApi : NewsApi, IdlingResource {
    infix fun ArticlePredicate.yields(
        provider: ArticleResponseProvider
    )

    infix fun ArticlePredicate.yields(
        result: Either<AppException, ArticleResponse>
    )

    fun yieldsSourcesResponse(
        result: Either<AppException, SourcesResponse>
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
        CoroutineScope by createTestCoroutineScope(TestCoroutineDispatcher() + TestCoroutineExceptionHandler() + dispatcher),
        DelayController by dispatcher {

        init {
            pauseDispatcher()
        }
    }
