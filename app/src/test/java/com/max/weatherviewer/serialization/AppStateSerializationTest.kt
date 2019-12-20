package com.max.weatherviewer.serialization

import com.max.weatherviewer.app.AppGsonSerializer
import com.max.weatherviewer.app.State
import com.max.weatherviewer.domain.Article
import com.max.weatherviewer.domain.Description
import com.max.weatherviewer.domain.Title
import com.max.weatherviewer.screens.feed.FeedLoading
import com.max.weatherviewer.screens.feed.LoadCriteria
import com.max.weatherviewer.screens.feed.Preview
import io.kotlintest.shouldBe
import kotlinx.collections.immutable.persistentListOf
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import protocol.ActionApplied
import protocol.NotifyComponentAttached
import protocol.NotifyComponentSnapshot
import protocol.ServerMessage
import java.net.URL
import java.util.*

@RunWith(JUnit4::class)
class AppStateSerializationTest {

    private val gsonSerializer = AppGsonSerializer()

    private val previewScreenState = Preview(
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

    private val loadingScreenState = FeedLoading(
        UUID.randomUUID(),
        LoadCriteria.Query("test")
    )

    private val testState = State(
        persistentListOf(
            previewScreenState,
            loadingScreenState
        )
    )

    @Test
    fun `test NotifyComponentAttached is serializing correctly`() {

        val message = NotifyComponentAttached(testState)
        val json = gsonSerializer.toJson(message, ServerMessage::class.java)
        val fromJson = gsonSerializer.fromJson(json, ServerMessage::class.java)

        fromJson shouldBe message
    }

    @Test
    fun `test ActionApplied is serializing correctly`() {

        val message = ActionApplied(UUID.randomUUID())
        val json = gsonSerializer.toJson(message, ServerMessage::class.java)
        val fromJson = gsonSerializer.fromJson(json, ServerMessage::class.java)

        fromJson shouldBe message
    }

    @Test
    fun `test NotifyComponentSnapshot is serializing correctly`() {

        val message = NotifyComponentSnapshot(
            "Message",
            testState,
            loadingScreenState
        )

        val json = gsonSerializer.toJson(message, ServerMessage::class.java)
        val fromJson = gsonSerializer.fromJson(json, ServerMessage::class.java)

        fromJson shouldBe message
    }

}
