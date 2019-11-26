package com.oliynick.max.elm.time.travel.protocol

import core.data.Name
import core.data.User
import core.data.photo
import core.data.randomId
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import protocol.converters
import protocol.fromValue
import protocol.toValue
import java.util.*
import kotlin.test.assertEquals

@RunWith(JUnit4::class)
class ValueTest {

    @Test
    fun `test unpacking of pojo is correct`() {
        val initial = User(randomId(), Name("John"), listOf(photo("https://www.google.com"),
            photo("https://www.google1.com"), photo("https://www.google3.com")))

        val converters = converters {
            +URLConverter
            +UUIDConverter
        }

        val value = initial.toValue(converters)
        val unparsed = value.fromValue(converters)

        assertEquals(initial, unparsed, """Initial $initial 
            |isn't equal to
            |$unparsed""".trimMargin())
    }

    @Test
    fun `test unpacking of non-nullable collection of primitives is correct`() {
        val initial = listOf("a", "b", "c")

        val converters = converters()

        val value = initial.toValue(converters)
        val unparsed = value.fromValue(converters)

        assertEquals(initial, unparsed, """Initial $initial 
            |isn't equal to
            |$unparsed""".trimMargin())
    }

    @Test
    fun `test unpacking of collection that contains nullable elements is correct`() {
        val initial = listOf("a", null, "b", "c", null)

        val converters = converters()

        val value = initial.toValue(converters)
        val unparsed = value.fromValue(converters)

        assertEquals(initial, unparsed, """Initial $initial 
            |isn't equal to
            |$unparsed""".trimMargin())
    }

    @Test
    fun `test unpacking of collection that contains only nullable elements is correct`() {
        val initial = listOf(null, null, null)

        val converters = converters()

        val value = initial.toValue(converters)
        val unparsed = value.fromValue(converters)

        assertEquals(initial, unparsed, """Initial $initial 
            |isn't equal to
            |$unparsed""".trimMargin())
    }

    @Test
    fun `test unpacking of 'null' is correct`() {
        val initial = null

        val converters = converters()

        val value = initial.toValue(converters)
        val unparsed = value.fromValue(converters)

        assertEquals(initial, unparsed, """Initial $initial 
            |isn't equal to
            |$unparsed""".trimMargin())
    }

    @Test
    fun `test unpacking of a primitive is correct`() {
        val initial = "a"

        val converters = converters()

        val value = initial.toValue(converters)
        val unparsed = value.fromValue(converters)

        assertEquals(initial, unparsed, """Initial $initial 
            |isn't equal to
            |$unparsed""".trimMargin())
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

        assertEquals(initial, unparsed, """Initial $initial 
            |isn't equal to
            |$unparsed""".trimMargin())
    }

}