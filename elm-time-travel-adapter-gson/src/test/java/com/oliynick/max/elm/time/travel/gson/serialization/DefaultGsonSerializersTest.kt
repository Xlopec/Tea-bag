package com.oliynick.max.elm.time.travel.gson.serialization

import com.oliynick.max.elm.time.travel.gson.gson
import core.data.Id
import core.data.Name
import core.data.Photo
import core.data.User
import io.kotlintest.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import protocol.*
import java.util.*

@RunWith(JUnit4::class)
class DefaultGsonSerializersTest {

    private val gsonSerializer = gson()

    private val testUser = User(
        Id(UUID.randomUUID()),
        Name("John"),
        listOf(
            Photo("https://www.google.com"),
            Photo("https://www.google.com1"),
            Photo("https://www.google.com2")
        )
    )

    @Test
    fun `test NotifyComponentAttached gets serialized correctly`() {

        val message = NotifyComponentAttached(testUser)
        val json = gsonSerializer.toJson(message, ServerMessage::class.java)
        val fromJson = gsonSerializer.fromJson(json, ServerMessage::class.java)

        fromJson shouldBe message
    }

    @Test
    fun `test ActionApplied gets serialized correctly`() {

        val message = ActionApplied(UUID.randomUUID())
        val json = gsonSerializer.toJson(message, ServerMessage::class.java)
        val fromJson = gsonSerializer.fromJson(json, ServerMessage::class.java)

        fromJson shouldBe message
    }

    @Test
    fun `test NotifyComponentSnapshot gets serialized correctly`() {

        val message = NotifyComponentSnapshot(
            "Message",
            testUser,
            listOf(
                Photo("https://www.google.com"),
                Photo("https://www.google.com1"),
                Photo("https://www.google.com2")
            )
        )

        val json = gsonSerializer.toJson(message, ServerMessage::class.java)
        val fromJson = gsonSerializer.fromJson(json, ServerMessage::class.java)

        fromJson shouldBe message
    }

    @Test
    fun `test ApplyMessage gets serialized properly`() {

        val applyMessage = ApplyMessage(testUser)

        val json = gsonSerializer.toJson(applyMessage, ClientMessage::class.java)
        val fromJson = gsonSerializer.fromJson(json, ClientMessage::class.java)

        applyMessage shouldBe fromJson
    }

    @Test
    fun `test ApplyState gets serialized properly`() {

        val applyMessage = ApplyState(testUser)

        val json = gsonSerializer.toJson(applyMessage, ClientMessage::class.java)
        val fromJson = gsonSerializer.fromJson(json, ClientMessage::class.java)

        applyMessage shouldBe fromJson
    }

}
