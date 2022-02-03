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

package com.max.reader

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.max.reader.environment.ArticleResponse
import com.max.reader.environment.anyRequest
import com.max.reader.screens.article.list.ui.ArticleTestTag
import com.max.reader.screens.article.list.ui.ArticlesScreen
import com.max.reader.screens.article.list.ui.ProgressIndicatorTag
import com.max.reader.screens.main.Application
import com.max.reader.test.invoke
import com.max.reader.test.setTestContent
import com.max.reader.ui.theme.AppTheme
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

            Application(AppComponent(this, AppInitializer(this)))

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