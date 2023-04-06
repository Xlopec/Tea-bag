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

package io.github.xlopec.reader.test

import androidx.activity.ComponentActivity
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import io.github.xlopec.reader.app.AppComponent
import io.github.xlopec.reader.app.AppInitializer
import io.github.xlopec.reader.app.feature.article.list.ArticlesState
import io.github.xlopec.reader.app.feature.network.ArticleElement
import io.github.xlopec.reader.app.feature.network.SourceElement
import io.github.xlopec.reader.app.model.Author
import io.github.xlopec.reader.app.model.Description
import io.github.xlopec.reader.app.model.Filter
import io.github.xlopec.reader.app.model.FilterType.Regular
import io.github.xlopec.reader.app.model.Query
import io.github.xlopec.reader.app.model.SourceId
import io.github.xlopec.reader.app.model.Title
import io.github.xlopec.reader.app.ui.screens.App
import io.github.xlopec.reader.app.ui.screens.article.ArticleTestTag
import io.github.xlopec.reader.app.ui.screens.article.Articles
import io.github.xlopec.reader.app.ui.screens.article.ProgressIndicatorTag
import io.github.xlopec.reader.app.ui.theme.AppTheme
import io.github.xlopec.reader.environment.ArticleResponse
import io.github.xlopec.reader.environment.anyArticleRequest
import io.github.xlopec.reader.environment.invoke
import io.github.xlopec.reader.environment.setTestContent
import io.github.xlopec.tea.core.toStatesComponent
import io.github.xlopec.tea.data.RandomUUID
import java.net.URI
import java.util.Date
import kotlinx.collections.immutable.persistentListOf
import org.junit.Rule
import org.junit.Test

internal class AppTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun testProgressIndicatorIsDisplayedOnStart() = rule {
        setContent {
            AppTheme(isDarkModeEnabled = true) {
                Articles(
                    state = ArticlesState.newLoading(
                        id = RandomUUID(),
                        filter = Filter(Regular, Query.of("Input text")),
                        articles = persistentListOf()
                    ),
                    listState = LazyListState(0, 0),
                    modifier = Modifier
                ) {}
            }
        }

        onNodeWithTag(ProgressIndicatorTag).assertIsDisplayed()
    }

    @Test
    fun testArticlesListIsDisplayedCorrectly() = rule {
        setTestContent {

            anyArticleRequest() yields ArticleResponse(TestArticleElement)

            App(
                AppComponent(
                    environment = this,
                    initializer = AppInitializer(systemDarkModeEnabled = false, this)
                ).toStatesComponent()
            )

            resumeDispatcher()
        }

        onNodeWithTag(ArticleTestTag(TestUrl)).assertIsDisplayed()
    }
}

private val TestUrl = URI("https://www.google.com")

private val TestDate = Date(2021, 11, 17)

private val TestArticleElement = ArticleElement(
    author = Author("Max"),
    description = Description("Android description"),
    publishedAt = TestDate,
    title = Title("Android"),
    url = TestUrl,
    urlToImage = null,
    source = SourceElement(SourceId("cnn"))
)
