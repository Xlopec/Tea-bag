package com.oliynick.max.elm.time.travel.gson.serialization.test

import com.oliynick.max.elm.time.travel.gson.Gson
import com.oliynick.max.elm.time.travel.gson.TypeAppenderAdapterFactory
import com.oliynick.max.elm.time.travel.gson.serialization.data.*
import com.oliynick.max.elm.time.travel.gson.serialization.serializer.PersistentListSerializer
import io.kotlintest.shouldBe
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class TypeAppenderAdapterFactoryTest {

    private val serializer = Gson {
        registerTypeAdapterFactory(TypeAppenderAdapterFactory)
    }

    @Test
    fun `test class hierarchy gets serialized correctly`() = with(serializer) {

        val message =
            D()
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
            registerTypeAdapterFactory(TypeAppenderAdapterFactory)
            registerTypeAdapter(PersistentList::class.java, PersistentListSerializer)
        }) {

            val container = Container(persistentListOf("a", "b", "c"))

            val json = toJson(container)

            val fromJson = fromJson(json, Container::class.java)

            fromJson shouldBe container
        }

    @Test
    fun `test AppenderFactory serializes polymorphic values`() =
        with(Gson {// todo add clone method
            registerTypeAdapterFactory(TypeAppenderAdapterFactory)
            registerTypeAdapter(PersistentList::class.java, PersistentListSerializer)
        }) {

            val container = PolyContainer(persistentListOf(PolyA(), PolyB()))

            val json = toJson(container)

            println(json)

            val fromJson = fromJson(json, PolyContainer::class.java)

            fromJson shouldBe container
        }

}
