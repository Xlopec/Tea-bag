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

    data class NullableListWrapper(
        val nullablePhotos: List<Photo?>
    )

    @Test
    fun `test NotifyComponentAttached gets serialized correctly`() {

        val message = NotifyComponentAttached(gsonSerializer.toJsonTree(testUser))
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
            gsonSerializer.toJsonTree("Message"),
            gsonSerializer.toJsonTree(testUser),
            gsonSerializer.toJsonTree(
                listOf(
                    Photo("https://www.google.com"),
                    Photo("https://www.google.com1"),
                    Photo("https://www.google.com2")
                )
            )
        )

        val json = gsonSerializer.toJson(message, ServerMessage::class.java)
        val fromJson = gsonSerializer.fromJson(json, ServerMessage::class.java)

        fromJson shouldBe message
    }

    @Test
    fun `test ApplyMessage gets serialized properly`() = with(gsonSerializer) {

        val applyMessage = ApplyMessage(toJsonTree(testUser))

        val json = gsonSerializer.toJson(applyMessage, ClientMessage::class.java)
        val fromJson = gsonSerializer.fromJson(json, ClientMessage::class.java)

        applyMessage shouldBe fromJson
    }

    @Test
    fun `test ApplyMessage with NullableListWrapper gets serialized properly`() = with(gsonSerializer) {

        val applyMessage = ApplyMessage(
            toJsonTree(
                NullableListWrapper(
                    listOf(
                        Photo("https://www.google.com"),
                        null,
                        Photo("https://www.google.com1"),
                        Photo("https://www.google.com2"),
                        null
                    )
                )
            )
        )

        val json = toJson(applyMessage, ClientMessage::class.java)
        val fromJson = fromJson(json, ClientMessage::class.java)

        applyMessage shouldBe fromJson
    }

    @Test
    fun `test nullable list gets serialized properly`() {

        val nullableList = listOf(
            Photo("https://www.google.com"),
            null,
            Photo("https://www.google.com1"),
            Photo("https://www.google.com2"),
            null
        )

        val json = gsonSerializer.toJson(nullableList, List::class.java)
        val fromJson = gsonSerializer.fromJson(json, List::class.java)

        nullableList shouldBe fromJson
    }

    @Test
    fun `test null gets serialized properly`() {

        val json = gsonSerializer.toJson(null, Nothing::class.java)
        val fromJson = gsonSerializer.fromJson(json, Nothing::class.java)

        null shouldBe fromJson
    }

    @Test
    fun `test ApplyState gets serialized properly`() = with(gsonSerializer) {

        val applyMessage = ApplyState(toJsonTree(testUser))

        val json = toJson(applyMessage, ClientMessage::class.java)
        val fromJson = fromJson(json, ClientMessage::class.java)

        applyMessage shouldBe fromJson
    }

}
