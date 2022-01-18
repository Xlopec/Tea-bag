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

package com.oliynick.max.tea.core.debug.app.domain.serialization

import com.google.gson.JsonElement
import com.oliynick.max.tea.core.data.*
import com.oliynick.max.tea.core.debug.app.domain.*
import com.oliynick.max.tea.core.debug.app.transport.toJsonElement
import com.oliynick.max.tea.core.debug.app.transport.toValue
import com.oliynick.max.tea.core.debug.gson.Gson
import com.oliynick.max.tea.core.debug.protocol.ApplyMessage
import com.oliynick.max.tea.core.debug.protocol.ClientMessage
import com.oliynick.max.tea.core.debug.protocol.ComponentId
import com.oliynick.max.tea.core.debug.protocol.NotifyClient
import io.kotlintest.shouldBe
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.io.File
import java.io.FileReader
import java.util.*

@RunWith(JUnit4::class)
internal class ClientMessageSerializationTest {

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
            Type.of("com.max.weatherviewer.app.State"),
            setOf(
                Property(
                    "screens",
                    CollectionWrapper(
                        listOf(
                            Ref(
                                Type.of("com.max.weatherviewer.screens.feed.FeedLoading"),
                                setOf(
                                    Property(
                                        "id",
                                        StringWrapper("6b1ece05-eefb-44fe-9313-892eb000f0ee")
                                    ),
                                    Property(
                                        "criteria", Ref(
                                            Type.of("com.max.weatherviewer.screens.feed.LoadCriteria\$Query"),
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
