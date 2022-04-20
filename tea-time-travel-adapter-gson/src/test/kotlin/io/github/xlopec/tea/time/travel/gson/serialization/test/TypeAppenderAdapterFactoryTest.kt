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

import io.github.xlopec.tea.time.travel.gson.Gson
import io.github.xlopec.tea.time.travel.gson.serialization.data.A
import io.github.xlopec.tea.time.travel.gson.serialization.data.Container
import io.github.xlopec.tea.time.travel.gson.serialization.data.D
import io.github.xlopec.tea.time.travel.gson.serialization.data.PolyA
import io.github.xlopec.tea.time.travel.gson.serialization.data.PolyB
import io.github.xlopec.tea.time.travel.gson.serialization.data.PolyContainer
import io.github.xlopec.tea.time.travel.gson.serialization.serializer.MapDeserializer
import io.github.xlopec.tea.time.travel.gson.serialization.serializer.PersistentListSerializer
import kotlin.test.assertEquals
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
internal class TypeAppenderAdapterFactoryTest {

    private val serializer = Gson {
        setPrettyPrinting()
        enableComplexMapKeySerialization()
    }

    @Test
    fun `test class hierarchy gets serialized correctly`() = with(Gson {
        registerTypeAdapter(Map::class.java, MapDeserializer)
    }) {

        val message = D()
        val json = toJson(message)

        val fromJson = fromJson(json, A::class.java)

        assertEquals(message, fromJson)
        //fromJson shouldBe message
    }

    @Test
    fun `test simple string gets serialized correctly`() = with(serializer) {

        val message = "a"
        val json = toJson(message)
        val fromJson = fromJson(json, String::class.java)

        assertEquals(message, fromJson)
        //fromJson shouldBe message
    }

    @Test
    fun `test AppenderFactory delegates serialization to a user defined serializer`() =
        with(Gson {
            registerTypeAdapter(PersistentList::class.java, PersistentListSerializer)
        }) {

            val container = Container(persistentListOf("a", "b", "c"))

            val json = toJson(container)

            val fromJson = fromJson(json, Container::class.java)

            assertEquals(container, fromJson)
            //fromJson shouldBe container
        }

    @Test
    fun `test AppenderFactory serializes polymorphic values`() =
        with(Gson {
            registerTypeAdapter(PersistentList::class.java, PersistentListSerializer)
        }) {

            val container = PolyContainer(persistentListOf(PolyA(), PolyB()))

            val json = toJson(container)

            val fromJson = fromJson(json, PolyContainer::class.java)

            assertEquals(container, fromJson)
            //fromJson shouldBe container
        }

}
