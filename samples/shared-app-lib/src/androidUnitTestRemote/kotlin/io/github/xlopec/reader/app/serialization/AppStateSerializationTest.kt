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

package io.github.xlopec.reader.app.serialization

import io.github.xlopec.reader.app.AppState
import io.github.xlopec.reader.app.ScreenMessage
import io.github.xlopec.reader.app.Settings
import io.github.xlopec.reader.app.feature.article.list.ArticlesLoadable
import io.github.xlopec.reader.app.feature.article.list.ArticlesState
import io.github.xlopec.reader.app.feature.article.list.LoadArticles
import io.github.xlopec.reader.app.feature.navigation.Tab
import io.github.xlopec.reader.app.misc.Idle
import io.github.xlopec.reader.app.model.Article
import io.github.xlopec.reader.app.model.Description
import io.github.xlopec.reader.app.model.Filter
import io.github.xlopec.reader.app.model.FilterType
import io.github.xlopec.reader.app.model.Query
import io.github.xlopec.reader.app.model.Title
import io.github.xlopec.tea.navigation.stackOf
import io.github.xlopec.tea.time.travel.gson.Gson
import io.github.xlopec.tea.time.travel.protocol.NotifyComponentAttached
import io.github.xlopec.tea.time.travel.protocol.NotifyComponentSnapshot
import io.github.xlopec.tea.time.travel.protocol.ServerMessage
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.net.URI
import java.util.*
import kotlin.test.assertEquals
import kotlin.uuid.Uuid

@RunWith(JUnit4::class)
internal class AppStateSerializationTest {

    private val gsonSerializer = Gson {
        setPrettyPrinting()
        registerTypeHierarchyAdapter(PersistentList::class.java, PersistentListSerializer)
    }

    private val previewScreenState = ArticlesState(
        tab = Tab.Feed,
        filter = Filter(FilterType.Regular, Query.of("android")),
        loadable = ArticlesLoadable(
            data = persistentListOf(
                Article(
                    url = URI("http://www.google.com"),
                    title = Title("test"),
                    author = null,
                    description = Description("test"),
                    urlToImage = null,
                    published = Date(),
                    isFavorite = false,
                    source = null
                )
            ),
            hasMore = false,
            loadableState = Idle
        )
    )

    private val loadingScreenState = ArticlesState.newLoading(
        Tab.Feed,
        Filter(FilterType.Regular, Query.of("test"))
    )

    private val testState = AppState(
        screens = stackOf(
            previewScreenState,
            loadingScreenState
        ),
        settings = Settings(
            userDarkModeEnabled = true,
            systemDarkModeEnabled = false,
            syncWithSystemDarkModeEnabled = false
        )
    )

    @Test
    fun `test NotifyComponentAttached is serialized correctly`() = with(gsonSerializer) {

        val message = NotifyComponentAttached(
            state = toJsonTree(testState),
            commands = setOf()
        )
        val json = toJson(message)

        val fromJson = fromJson(json, ServerMessage::class.java)

        assertEquals(message, fromJson)
    }

    @Test
    fun `test NotifyComponentSnapshot is serialized correctly`() = with(gsonSerializer) {

        val message = NotifyComponentSnapshot(
            message = toJsonTree("Message"),
            oldState = toJsonTree(testState),
            newState = toJsonTree(loadingScreenState),
            commands = setOf()
        )

        val json = toJson(message)
        val fromJson = fromJson(json, ServerMessage::class.java)

        assertEquals(message, fromJson)
    }

    @Test
    fun `test ScreenMessage is serialized correctly`() = with(gsonSerializer) {

        val message = LoadArticles(Uuid.random())

        val json = toJson(message)
        val fromJson = fromJson(json, ScreenMessage::class.java)

        assertEquals(message, fromJson)
    }
}
