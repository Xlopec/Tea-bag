/*
 * Copyright (C) 2021. Maksym Oliinyk.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
class AppStateSerializationTest {

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
        isDarkModeEnabled = false,
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
