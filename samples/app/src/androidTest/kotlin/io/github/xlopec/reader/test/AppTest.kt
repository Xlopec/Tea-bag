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
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import io.github.xlopec.reader.app.AppComponent
import io.github.xlopec.reader.app.AppInitializer
import io.github.xlopec.reader.app.domain.*
import io.github.xlopec.reader.app.domain.FilterType.Regular
import io.github.xlopec.reader.app.feature.article.list.ArticlesState
import io.github.xlopec.reader.app.feature.network.ArticleElement
import io.github.xlopec.reader.app.feature.network.SourceElement
import io.github.xlopec.reader.app.ui.screens.*
import io.github.xlopec.reader.app.ui.screens.article.ArticleTestTag
import io.github.xlopec.reader.app.ui.screens.article.ArticlesScreen
import io.github.xlopec.reader.app.ui.screens.article.ProgressIndicatorTag
import io.github.xlopec.reader.app.ui.theme.AppTheme
import io.github.xlopec.reader.environment.ArticleResponse
import io.github.xlopec.reader.environment.anyArticleRequest
import io.github.xlopec.reader.environment.invoke
import io.github.xlopec.reader.environment.setTestContent
import io.github.xlopec.tea.core.states
import io.github.xlopec.tea.data.RandomUUID
import java.net.URL
import java.util.*
import kotlinx.collections.immutable.persistentListOf
import org.junit.Rule
import org.junit.Test

internal class AppTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun testProgressIndicatorIsDisplayedOnStart() = composeTestRule {
        setContent {
            AppTheme(isDarkModeEnabled = true) {
                ArticlesScreen(
                    ArticlesState.newLoading(RandomUUID(),
                        Filter(Regular, Query.of("Input text")),
                        persistentListOf()),
                    LazyListState(0, 0),
                    Modifier
                ) {}
            }
        }

        onNode(hasTestTag(ProgressIndicatorTag)).assertIsDisplayed()
    }

    @Test
    fun testArticlesListIsDisplayedCorrectly() = composeTestRule {
        setTestContent {

            anyArticleRequest() yields ArticleResponse(TestArticleElement)

            AppView(
                AppComponent(
                    this,
                    AppInitializer(systemDarkModeEnabled = false, this)
                ).states()
            )

            resumeDispatcher()
        }

        onNode(hasTestTag(ArticleTestTag(TestUrl))).assertIsDisplayed()
    }

}

private val TestUrl = URL("https://www.google.com")

private val TestDate = Date(2021, 11, 17)

private val TestArticleElement = ArticleElement(
    Author("Max"),
    Description("Android description"),
    TestDate,
    Title("Android"),
    TestUrl,
    null,
    SourceElement(SourceId("cnn"))
)
