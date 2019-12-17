package com.oliynick.max.elm.time.travel.protocol

import core.data.Name
import core.data.User
import core.data.photo
import core.data.randomId
import io.kotlintest.shouldBe
import kotlinx.collections.immutable.persistentListOf
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import protocol.converters
import protocol.fromValue
import protocol.toValue
import java.util.*

private data class IterableByDelegate(val src: Iterable<String>) : Iterable<String> by src

@RunWith(JUnit4::class)
class ValueTest {

    @Test
    fun `test unpacking of pojo is correct`() {
        val initial = User(
            randomId(), Name("John"), listOf(
                photo("https://www.google.com"),
                photo("https://www.google1.com"), photo("https://www.google3.com")
            )
        )

        val converters = converters {
            +URLConverter
            +UUIDConverter
        }

        val value = initial.toValue(converters)
        val unparsed = value.fromValue(converters)

        initial shouldBe unparsed
    }

    @Test
    fun `test unpacking of iterable implemented by a list delegate is correct`() {
        val initial = IterableByDelegate(listOf("a", "b", "c"))

        val converters = converters()

        val value = initial.toValue(converters)
        val unparsed = value.fromValue(converters)

        initial shouldBe unparsed
    }

    @Test
    fun `test unpacking of iterable implemented by a set delegate is correct`() {
        val initial = IterableByDelegate(setOf("a", "b", "c"))

        val converters = converters()

        val value = initial.toValue(converters)
        val unparsed = value.fromValue(converters)

        initial shouldBe unparsed
    }

    @Test
    fun `test unpacking of non-nullable collection of primitives is correct`() {
        val initial = listOf("a", "b", "c")

        val converters = converters()

        val value = initial.toValue(converters)
        val unparsed = value.fromValue(converters)

        initial shouldBe unparsed
    }

    @Test
    fun `test unpacking of non-nullable map of primitives is correct`() {
        val initial = mapOf("a" to listOf(1, 2, 3), "b" to listOf(4, 5, 6), "c" to listOf(7), "d" to listOf())

        val converters = converters()

        val value = initial.toValue(converters)
        val unparsed = value.fromValue(converters)

        initial shouldBe unparsed
    }

    @Test
    fun `test unpacking of collection that contains nullable elements is correct`() {
        val initial = listOf("a", null, "b", "c", null)

        val converters = converters()

        val value = initial.toValue(converters)
        val unparsed = value.fromValue(converters)

        initial shouldBe unparsed
    }

    @Test
    fun `test unpacking of map that contains nullable elements is correct`() {
        val initial = mapOf("a" to listOf(1, 2, 3), null to null, null to listOf(7), "b" to listOf(4, 5))

        val converters = converters()

        val value = initial.toValue(converters)
        val unparsed = value.fromValue(converters)

        initial shouldBe unparsed
    }

    @Test
    fun `test unpacking of collection that contains only nullable elements is correct`() {
        val initial = listOf(null, null, null)

        val converters = converters()

        val value = initial.toValue(converters)
        val unparsed = value.fromValue(converters)

        initial shouldBe unparsed
    }

    @Test
    fun `test unpacking of map that contains only nullable elements is correct`() {
        val initial = mapOf(null to null)

        val converters = converters()

        val value = initial.toValue(converters)
        val unparsed = value.fromValue(converters)

        initial shouldBe unparsed
    }

    @Test
    fun `test unpacking of 'null' is correct`() {
        val initial = null

        val converters = converters()

        val value = initial.toValue(converters)
        val unparsed = value.fromValue(converters)

        initial shouldBe unparsed
    }

    @Test
    fun `test unpacking of a primitive is correct`() {
        val initial = "a"

        val converters = converters()

        val value = initial.toValue(converters)
        val unparsed = value.fromValue(converters)

        initial shouldBe unparsed
    }

    private data class Holder(val uuid0: UUID, val uuid1: UUID?)

    @Test
    fun `test unpacking of custom fields is correct`() {
        val initial = Holder(UUID.randomUUID(), null)

        val converters = converters {
            +UUIDConverter
        }

        val value = initial.toValue(converters)
        val unparsed = value.fromValue(converters)

        initial shouldBe unparsed
    }

    @Test
    fun `test unpacking of external library class instance is correct`() {

        val listOfStrings = persistentListOf("a", "b", "c")

        val converters = converters()

        val value = listOfStrings.toValue(converters)
        val unparsed = value.fromValue(converters)

        listOfStrings shouldBe unparsed
    }

}