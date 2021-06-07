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

package com.max.reader.serialization

import com.max.reader.app.AppState
import com.max.reader.app.ScreenMessage
import com.max.reader.app.serialization.PersistentListSerializer
import com.max.reader.domain.Article
import com.max.reader.domain.Description
import com.max.reader.domain.Title
import com.max.reader.screens.article.list.ArticlesState
import com.max.reader.screens.article.list.LoadArticlesFromScratch
import com.max.reader.screens.article.list.Query
import com.max.reader.screens.article.list.QueryType
import com.oliynick.max.tea.core.debug.gson.Gson
import com.oliynick.max.tea.core.debug.protocol.NotifyComponentAttached
import com.oliynick.max.tea.core.debug.protocol.NotifyComponentSnapshot
import com.oliynick.max.tea.core.debug.protocol.ServerMessage
import io.kotlintest.shouldBe
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.net.URL
import java.util.*

@RunWith(JUnit4::class)
internal class AppStateSerializationTest {

    private val gsonSerializer = Gson {
        setPrettyPrinting()
        registerTypeHierarchyAdapter(PersistentList::class.java, PersistentListSerializer)
    }

    private val previewScreenState = ArticlesState(
        UUID.randomUUID(),
        Query("android", QueryType.Regular),
        listOf(
            Article(
                URL("http://www.google.com"),
                Title("test"),
                null,
                Description("test"),
                null,
                Date(),
                false
            )
        ),
        false,
        ArticlesState.TransientState.Preview
    )

    private val loadingScreenState = ArticlesState.newLoading(
        UUID.randomUUID(),
        Query("test", QueryType.Regular)
    )

    private val testState = AppState(
        isInDarkMode = false,
        screens = persistentListOf(
            previewScreenState,
            loadingScreenState
        )
    )

    @Test
    fun `test NotifyComponentAttached is serializing correctly`() = with(gsonSerializer) {

        val message = NotifyComponentAttached(toJsonTree(testState))
        val json = toJson(message)

        val fromJson = fromJson(json, ServerMessage::class.java)

        fromJson shouldBe message
    }

    @Test
    fun `test NotifyComponentSnapshot is serializing correctly`() = with(gsonSerializer) {

        val message = NotifyComponentSnapshot(
            toJsonTree("Message"),
            toJsonTree(testState),
            toJsonTree(loadingScreenState)
        )

        val json = toJson(message)
        val fromJson = fromJson(json, ServerMessage::class.java)

        fromJson shouldBe message
    }

    @Test
    fun `test ScreenMessage is serializing correctly`() = with(gsonSerializer) {

        val message = LoadArticlesFromScratch(UUID.randomUUID())

        val json = toJson(message)
        val fromJson = fromJson(json, ScreenMessage::class.java)

        fromJson shouldBe message
    }

}
