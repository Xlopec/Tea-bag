/*
 * MIT License
 *
 * Copyright (c) 2021. Maksym Oliinyk.
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

package com.max.reader.test

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.max.reader.app.ui.screens.AppView
import com.max.reader.app.ui.screens.article.ArticleTestTag
import com.max.reader.app.ui.screens.article.ArticlesScreen
import com.max.reader.app.ui.screens.article.ProgressIndicatorTag
import com.max.reader.app.ui.theme.AppTheme
import com.max.reader.environment.ArticleResponse
import com.max.reader.environment.anyRequest
import com.oliynick.max.entities.shared.randomUUID
import com.oliynick.max.reader.app.AppComponent
import com.oliynick.max.reader.app.AppInitializer
import com.oliynick.max.reader.article.list.ArticlesState
import com.oliynick.max.reader.article.list.Query
import com.oliynick.max.reader.article.list.QueryType.Regular
import com.oliynick.max.reader.domain.Author
import com.oliynick.max.reader.domain.Description
import com.oliynick.max.reader.domain.Title
import com.oliynick.max.reader.network.ArticleElement
import org.junit.Rule
import org.junit.Test
import java.net.URL
import java.util.*

internal class AppTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun testProgressIndicatorIsDisplayedOnStart() = composeTestRule {
        setContent {
            AppTheme(isDarkModeEnabled = true) {
                ArticlesScreen(
                    ArticlesState.newLoading(randomUUID(), Query("Input text", Regular), listOf()),
                    {}
                )
            }
        }

        onNode(hasTestTag(ProgressIndicatorTag)).assertIsDisplayed()
    }

    @Test
    fun testArticlesListIsDisplayedCorrectly() = composeTestRule {
        setTestContent {

            anyRequest() yields ArticleResponse(TestArticleElement)

            AppView(AppComponent(this, AppInitializer(this)))

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
    TestUrl
)
