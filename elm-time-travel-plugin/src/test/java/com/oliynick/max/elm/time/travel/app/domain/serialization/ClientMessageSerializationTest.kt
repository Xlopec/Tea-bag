package com.oliynick.max.elm.time.travel.app.domain.serialization

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
import java.util.*

@RunWith(JUnit4::class)
class ClientMessageSerializationTest {

    val gson = gson()

    @Test
    fun `test subclasses of ClientMessage serialized properly`() {

        val user = User(
            Id(UUID.randomUUID()),
            Name("John"),
            listOf()
        )

        val applyMessage = ApplyMessage(user)

        val json = gson.toJson(applyMessage, ClientMessage::class.java)
        val fromJson = gson.fromJson(json, ClientMessage::class.java)

        applyMessage shouldBe fromJson
    }

}