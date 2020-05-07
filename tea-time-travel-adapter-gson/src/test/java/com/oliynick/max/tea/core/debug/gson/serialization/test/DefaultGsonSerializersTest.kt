package com.oliynick.max.tea.core.debug.gson.serialization.test

import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import com.oliynick.max.tea.core.debug.gson.Gson
import com.oliynick.max.tea.core.debug.gson.serialization.data.Singleton
import core.data.Id
import core.data.Name
import core.data.Photo
import core.data.User
import io.kotlintest.matchers.beInstanceOf
import io.kotlintest.should
import io.kotlintest.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import protocol.ApplyMessage
import protocol.ApplyState
import protocol.ClientMessage
import protocol.ComponentId
import protocol.NotifyClient
import protocol.NotifyComponentAttached
import protocol.NotifyComponentSnapshot
import protocol.NotifyServer
import protocol.ServerMessage
import java.util.*

@RunWith(JUnit4::class)
class DefaultGsonSerializersTest {

    private val gsonSerializer = Gson()

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
        val json = gsonSerializer.toJson(message)
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

        val json = gsonSerializer.toJson(message)
        val fromJson = gsonSerializer.fromJson(json, ServerMessage::class.java)

        fromJson shouldBe message
    }

    @Test
    fun `test ApplyMessage gets serialized properly`() = with(gsonSerializer) {

        val applyMessage =
            NotifyClient(
                ApplyMessage(toJsonTree(testUser))
            )

        val json = gsonSerializer.toJson(applyMessage)
        val fromJson = gsonSerializer.fromJson(json, NotifyClient::class.java)

        fromJson shouldBe applyMessage
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

        val json = toJson(applyMessage)
        val fromJson = fromJson(json, ClientMessage::class.java)

        fromJson shouldBe applyMessage
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

        val json = gsonSerializer.toJson(nullableList)
        val fromJson = gsonSerializer.fromJson<List<Photo?>>(json, TypeToken.getParameterized(List::class.java, Photo::class.java).type)

        fromJson shouldBe nullableList
    }

    @Test
    fun `test null gets serialized properly`() {

        val json = gsonSerializer.toJson(null)
        val fromJson = gsonSerializer.fromJson(json, Any::class.java)

        fromJson shouldBe null
    }

    @Test
    fun `test ApplyState gets serialized properly`() = with(gsonSerializer) {

        val applyMessage = ApplyState(toJsonTree(testUser))

        val json = toJson(applyMessage)
        val fromJson = fromJson(json, ClientMessage::class.java)

        fromJson shouldBe applyMessage
    }

    @Test
    fun `test singleton gets serialized properly`() = with(gsonSerializer) {

        val json = toJson(Singleton)
        val fromJson = fromJson(json, Singleton::class.java)

        fromJson should beInstanceOf(Singleton::class)
    }

    @Test
    fun `test NotifyServer gets serialized correctly`() = with(gsonSerializer) {

        val message = NotifyServer(
            UUID.randomUUID(), ComponentId("some"), NotifyComponentAttached(toJsonTree(testUser)))
        val json = toJson(message)
        val fromJson = fromJson(json, NotifyServer::class.java)

        fromJson shouldBe message
    }

}

@Suppress("TestFunctionName")
private fun NotifyClient(
    message: ClientMessage<JsonElement>
) = NotifyClient(UUID.randomUUID(), ComponentId("test"), message)

