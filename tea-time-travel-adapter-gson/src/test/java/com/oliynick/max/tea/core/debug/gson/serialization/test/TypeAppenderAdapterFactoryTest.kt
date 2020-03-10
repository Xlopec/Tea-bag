package com.oliynick.max.tea.core.debug.gson.serialization.test

import com.oliynick.max.tea.core.debug.gson.Gson
import com.oliynick.max.tea.core.debug.gson.serialization.data.A
import com.oliynick.max.tea.core.debug.gson.serialization.data.Container
import com.oliynick.max.tea.core.debug.gson.serialization.data.D
import com.oliynick.max.tea.core.debug.gson.serialization.data.PolyA
import com.oliynick.max.tea.core.debug.gson.serialization.data.PolyB
import com.oliynick.max.tea.core.debug.gson.serialization.data.PolyContainer
import com.oliynick.max.tea.core.debug.gson.serialization.serializer.MapDeserializer
import com.oliynick.max.tea.core.debug.gson.serialization.serializer.PersistentListSerializer
import io.kotlintest.shouldBe
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class TypeAppenderAdapterFactoryTest {

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

        fromJson shouldBe message
    }

    @Test
    fun `test simple string gets serialized correctly`() = with(serializer) {

        val message = "a"
        val json = toJson(message)
        val fromJson = fromJson(json, String::class.java)

        fromJson shouldBe message
    }

    @Test
    fun `test AppenderFactory delegates serialization to a user defined serializer`() =
        with(Gson {
            registerTypeAdapter(PersistentList::class.java, PersistentListSerializer)
        }) {

            val container = Container(persistentListOf("a", "b", "c"))

            val json = toJson(container)

            val fromJson = fromJson(json, Container::class.java)

            fromJson shouldBe container
        }

    @Test
    fun `test AppenderFactory serializes polymorphic values`() =
        with(Gson {
            registerTypeAdapter(PersistentList::class.java, PersistentListSerializer)
        }) {

            val container = PolyContainer(persistentListOf(PolyA(), PolyB()))

            val json = toJson(container)

            val fromJson = fromJson(json, PolyContainer::class.java)

            fromJson shouldBe container
        }

}
