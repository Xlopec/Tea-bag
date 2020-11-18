package com.max.reader.serialization

import com.max.reader.app.ScreenMessage
import com.max.reader.app.AppState
import com.max.reader.app.serialization.PersistentListSerializer
import com.max.reader.domain.Article
import com.max.reader.domain.Description
import com.max.reader.domain.Title
import com.max.reader.screens.article.list.ArticlesLoadingState
import com.max.reader.screens.article.list.LoadArticles
import com.max.reader.screens.article.list.LoadCriteria
import com.max.reader.screens.article.list.ArticlesPreviewState
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

    private val previewScreenState = ArticlesPreviewState(
        UUID.randomUUID(),
        LoadCriteria.Query("android"),
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
        )
    )

    private val loadingScreenState = ArticlesLoadingState(
        UUID.randomUUID(),
        LoadCriteria.Query("test")
    )

    private val testState = AppState(
        persistentListOf(
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

        val message = LoadArticles(UUID.randomUUID())

        val json = toJson(message)
        val fromJson = fromJson(json, ScreenMessage::class.java)

        fromJson shouldBe message
    }

}
