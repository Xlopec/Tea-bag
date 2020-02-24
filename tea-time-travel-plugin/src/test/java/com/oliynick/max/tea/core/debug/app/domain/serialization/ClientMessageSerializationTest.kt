package com.oliynick.max.tea.core.debug.app.domain.serialization

import com.google.gson.JsonElement
import com.oliynick.max.tea.core.debug.app.domain.cms.*
import com.oliynick.max.tea.core.debug.app.transport.serialization.toJsonElement
import com.oliynick.max.tea.core.debug.app.transport.serialization.toValue
import com.oliynick.max.tea.core.debug.gson.Gson
import com.oliynick.max.tea.core.debug.gson.TypeAppenderAdapterFactory
import core.data.*
import io.kotlintest.shouldBe
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import protocol.ApplyMessage
import protocol.ClientMessage
import protocol.ComponentId
import protocol.NotifyClient
import java.io.File
import java.io.FileReader
import java.util.*

@RunWith(JUnit4::class)
class ClientMessageSerializationTest {

    private val gson = Gson()

    @Test
    fun `test subclasses of ClientMessage serialized properly`() = with(gson) {

        val user = User(
            Id(UUID.randomUUID()),
            Name("John"),
            listOf()
        )

        val applyMessage = ApplyMessage(toJsonTree(user))

        val json = toJson(applyMessage)
        val fromJson = fromJson(json, ClientMessage::class.java)

        applyMessage shouldBe fromJson
    }

    @Test
    fun `test from Value is mapped to Gson tree properly`() = with(gson) {

        val user = User(
            Id(UUID.randomUUID()),
            Name("John"),
            listOf(
                Photo("https://www.google.com")
            ),
            Avatar("https://www.google.com")
        )

        val expectedMessageTree = toJsonTree(
            NotifyClient(
                UUID.randomUUID(),
                ComponentId("test"),
                ApplyMessage(toJsonTree(user))
            )
        )

        val actualMessageTree = expectedMessageTree.toValue().toJsonElement()

        actualMessageTree shouldBe expectedMessageTree
    }

    @Test
    @Ignore("this test probably won't needed anymore")
    fun `test raw json is parsed properly`() {

        val jsonFile = File("src/test/resources/test_state.json")

        require(jsonFile.exists())

        val tree = gson.fromJson(FileReader(jsonFile), JsonElement::class.java)

        tree.asJsonObject.toValue() shouldBe Ref(
            Type("com.max.weatherviewer.app.State"),
            setOf(
                Property(
                    "screens",
                    CollectionWrapper(
                        listOf(
                            Ref(
                                Type("com.max.weatherviewer.screens.feed.FeedLoading"),
                                setOf(
                                    Property(
                                        "id",
                                        StringWrapper("6b1ece05-eefb-44fe-9313-892eb000f0ee")
                                    ),
                                    Property(
                                        "criteria", Ref(
                                            Type("com.max.weatherviewer.screens.feed.LoadCriteria\$Query"),
                                            setOf(Property("query", StringWrapper("android")))
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )
    }

}