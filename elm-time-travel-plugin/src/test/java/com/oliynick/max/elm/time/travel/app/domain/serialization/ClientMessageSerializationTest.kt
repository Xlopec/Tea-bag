package com.oliynick.max.elm.time.travel.app.domain.serialization

import com.google.gson.JsonElement
import com.oliynick.max.elm.time.travel.app.domain.cms.*
import com.oliynick.max.elm.time.travel.app.transport.serialization.toValue
import com.oliynick.max.elm.time.travel.gson.TypeAppenderAdapterFactory
import com.oliynick.max.elm.time.travel.gson.gson
import core.data.Id
import core.data.Name
import core.data.User
import io.kotlintest.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import protocol.ApplyMessage
import protocol.ClientMessage
import java.io.File
import java.io.FileReader
import java.util.*

@RunWith(JUnit4::class)
class ClientMessageSerializationTest {

    private val gson = gson {
        registerTypeAdapterFactory(TypeAppenderAdapterFactory)
    }

    @Test
    fun `test subclasses of ClientMessage serialized properly`() {

        val user = User(
            Id(UUID.randomUUID()),
            Name("John"),
            listOf()
        )

        val applyMessage = ApplyMessage(gson.toJsonTree(user))

        val json = gson.toJson(applyMessage)
        val fromJson = gson.fromJson(json, ClientMessage::class.java)

        applyMessage shouldBe fromJson
    }

    @Test
    fun `test raw json is parsed properly`() {

        val jsonFile = File("src/test/resources/test_state.json")

        require(jsonFile.exists())

        val tree = gson.fromJson(FileReader(jsonFile), JsonElement::class.java)

        tree.asJsonObject.toValue() shouldBe Ref(
            Type("com.max.weatherviewer.app.State"),
            setOf(
                Property("screens",
                         CollectionWrapper(Type("kotlinx.collections.immutable.implementations.immutableList.SmallPersistentVector"),
                                           listOf(
                                               Ref(Type("com.max.weatherviewer.screens.feed.FeedLoading"),
                                                   setOf(Property("id", StringWrapper(Type.of("java.util.UUID"), "6b1ece05-eefb-44fe-9313-892eb000f0ee")),
                                                         Property("criteria", Ref(
                                                             Type("com.max.weatherviewer.screens.feed.LoadCriteria\$Query"),
                                                             setOf(Property("query", StringWrapper(Type.of("java.lang.String"), "android")))))
                                                   )
                                               )
                                           )
                         )
                )
            )
        )
    }

}