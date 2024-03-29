/*
 * MIT License
 *
 * Copyright (c) 2021. Maksym Oliinyk.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.xlopec.tea.time.travel.gson.serialization.test

import com.google.gson.JsonNull
import com.google.gson.JsonPrimitive
import com.google.gson.reflect.TypeToken
import io.github.xlopec.tea.data.Id
import io.github.xlopec.tea.data.Name
import io.github.xlopec.tea.data.Photo
import io.github.xlopec.tea.data.UUID
import io.github.xlopec.tea.data.User
import io.github.xlopec.tea.time.travel.gson.Gson
import io.github.xlopec.tea.time.travel.gson.GsonClientMessage
import io.github.xlopec.tea.time.travel.gson.serialization.data.Singleton
import io.github.xlopec.tea.time.travel.protocol.ApplyMessage
import io.github.xlopec.tea.time.travel.protocol.ApplyState
import io.github.xlopec.tea.time.travel.protocol.ClientMessage
import io.github.xlopec.tea.time.travel.protocol.ComponentId
import io.github.xlopec.tea.time.travel.protocol.NotifyClient
import io.github.xlopec.tea.time.travel.protocol.NotifyComponentAttached
import io.github.xlopec.tea.time.travel.protocol.NotifyComponentSnapshot
import io.github.xlopec.tea.time.travel.protocol.NotifyServer
import io.github.xlopec.tea.time.travel.protocol.ServerMessage
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
internal class DefaultGsonSerializersTest {

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

        val message = NotifyComponentAttached(
            gsonSerializer.toJsonTree(testUser),
            setOf(JsonNull.INSTANCE, JsonPrimitive(true), gsonSerializer.toJsonTree(testUser))
        )
        val json = gsonSerializer.toJson(message)
        val fromJson = gsonSerializer.fromJson(json, ServerMessage::class.java)

        assertEquals(message, fromJson)
        // fromJson shouldBe message
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
            ),
            setOf(JsonNull.INSTANCE, JsonPrimitive(true), gsonSerializer.toJsonTree(testUser))
        )

        val json = gsonSerializer.toJson(message)
        val fromJson = gsonSerializer.fromJson(json, ServerMessage::class.java)

        assertEquals(message, fromJson)
        // fromJson shouldBe message
    }

    @Test
    fun `test ApplyMessage gets serialized properly`() {
        with(gsonSerializer) {

            val applyMessage =
                NotifyClient(
                    ApplyMessage(toJsonTree(testUser))
                )

            val json = gsonSerializer.toJson(applyMessage)
            val fromJson = gsonSerializer.fromJson(json, NotifyClient::class.java)

            assertEquals(applyMessage, fromJson)
            // fromJson shouldBe applyMessage
        }
    }

    @Test
    fun `test ApplyMessage with NullableListWrapper gets serialized properly`() {
        with(gsonSerializer) {

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

            assertEquals(applyMessage, fromJson)
            // fromJson shouldBe applyMessage
        }
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
        val fromJson = gsonSerializer.fromJson<List<Photo?>>(
            json,
            TypeToken.getParameterized(List::class.java, Photo::class.java).type
        )

        assertEquals(nullableList, fromJson)
        // fromJson shouldBe nullableList
    }

    @Test
    fun `test null gets serialized properly`() {

        val json = gsonSerializer.toJson(null)
        val fromJson = gsonSerializer.fromJson(json, Any::class.java)

        assertNull(fromJson)
        // fromJson shouldBe null
    }

    @Test
    fun `test ApplyState gets serialized properly`() {
        with(gsonSerializer) {

            val applyMessage = ApplyState(toJsonTree(testUser))

            val json = toJson(applyMessage)
            val fromJson = fromJson(json, ClientMessage::class.java)

            assertEquals(applyMessage, fromJson)
            // fromJson shouldBe applyMessage
        }
    }

    @Test
    fun `test singleton gets serialized properly`() {
        with(gsonSerializer) {

            val json = toJson(Singleton)
            val fromJson = fromJson(json, Singleton::class.java)

            assertIs<Singleton>(fromJson)
            // fromJson should beInstanceOf(Singleton::class)
        }
    }

    @Test
    fun `test NotifyServer gets serialized correctly`() = with(gsonSerializer) {

        val message = NotifyServer(
            UUID.randomUUID(),
            ComponentId("some"),
            NotifyComponentAttached(toJsonTree(testUser), setOf())
        )
        val json = toJson(message)
        val fromJson = fromJson(json, NotifyServer::class.java)

        assertEquals(message, fromJson)
        // fromJson shouldBe message
    }
}

@Suppress("TestFunctionName")
private fun NotifyClient(
    message: GsonClientMessage
) = NotifyClient(
    UUID.randomUUID(),
    ComponentId("test"),
    message
)
