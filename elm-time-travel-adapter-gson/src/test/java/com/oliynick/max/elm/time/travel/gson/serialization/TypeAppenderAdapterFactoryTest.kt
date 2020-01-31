package com.oliynick.max.elm.time.travel.gson.serialization

import com.oliynick.max.elm.time.travel.gson.Gson
import com.oliynick.max.elm.time.travel.gson.serialization.serializer.PersistentListSerializer
import io.kotlintest.shouldBe
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class TypeAppenderAdapterFactoryTest {

    private data class Container(val list: PersistentList<String>)

    private val serializer = Gson()

    @Test
    fun `test class hierarchy gets serialized correctly`() {

        val message = D()
        val json = serializer.toJson(message)

        val fromJson = serializer.fromJson(json, A::class.java)

        fromJson shouldBe message
    }

    @Test
    fun `test simple string gets serialized correctly`() {

        val message = "a"
        val json = serializer.toJson(message)
        val fromJson = serializer.fromJson(json, String::class.java)

        fromJson shouldBe message
    }

    @Test
    fun `test AppenderFactory delegates serialization to a user defined serializer`() = with(Gson { registerTypeAdapter(PersistentList::class.java, PersistentListSerializer) }) {

        val container = Container(persistentListOf("a", "b", "c"))

        val json = toJson(container)
        val fromJson = fromJson(json, Container::class.java)

        fromJson shouldBe container
    }

}
