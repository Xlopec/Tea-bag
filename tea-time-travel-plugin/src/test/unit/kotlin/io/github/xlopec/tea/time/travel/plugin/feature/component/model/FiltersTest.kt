/*
 * Copyright (C) 2021. Maksym Oliinyk.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("TestFunctionName")

package io.github.xlopec.tea.time.travel.plugin.feature.component.model

import com.google.gson.internal.LazilyParsedNumber
import io.github.xlopec.tea.time.travel.plugin.model.NumberWrapper
import io.github.xlopec.tea.time.travel.plugin.model.Property
import io.github.xlopec.tea.time.travel.plugin.model.Ref
import io.github.xlopec.tea.time.travel.plugin.model.Type
import io.github.xlopec.tea.time.travel.plugin.model.Valid
import kotlin.test.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
internal class FiltersTest {

    private val testProperties = setOf(
        Property(
            "testField",
            Ref(
                Type.of("com.example.another.Test"),
                emptySet()
            )
        ),
        Property(
            "yetAnotherField",
            Ref(
                Type.of("com.example.yet.another"),
                emptySet()
            )
        )
    )

    @Test
    fun `test if top level Ref matches expression, whole Ref remains unchanged`() {

        val ref = Ref(
            Type.of("com.example.Test"),
            testProperties
        )

        val filtered = (RegexPredicate("com\\.example\\.Test", false) as Valid).t.applyTo(ref)

        assertEquals(ref, filtered)
    }

    @Test
    fun `test if child's Ref type matches expression, only that Ref remains`() {

        val ref = Ref(
            Type.of("com.example.Test"),
            testProperties
        )

        val filtered = UnsafeRegexPredicate("com\\.example\\.another.*").applyTo(ref)

        assertEquals(
            Ref(
                Type.of("com.example.Test"),
                setOf(testProperties.first())
            ),
            filtered
        )
    }

    @Test
    fun `test if child's Ref property name matches expression, only that Ref remains`() {

        val ref = Ref(
            Type.of("com.example.Test"),
            testProperties
        )

        val filtered = UnsafeRegexPredicate(testProperties.first().name).applyTo(ref)

        assertEquals(
            Ref(
                Type.of("com.example.Test"),
                setOf(testProperties.first())
            ),
            filtered
        )
    }

    @Test
    fun `test if number property contains custom Number implementation, given regex is 'number' only this property remains in the Ref`() {

        val promitiveProperty = Property("primitive", NumberWrapper(LazilyParsedNumber(10.toString())))

        val ref = Ref(
            Type.of("com.example.Test"),
            testProperties + promitiveProperty
        )

        val filtered = UnsafeRegexPredicate("number").applyTo(ref)

        assertEquals(
            Ref(
                Type.of("com.example.Test"),
                setOf(promitiveProperty)
            ),

            filtered
        )
    }

    @Test
    fun `test if number property contains Int, given regex is 'int' only this property remains in the Ref`() {

        val promitiveProperty = Property("primitive", NumberWrapper(10))

        val ref = Ref(
            Type.of("com.example.Test"),
            testProperties + promitiveProperty
        )

        val filtered = UnsafeRegexPredicate("java.lang.Integer").applyTo(ref)

        assertEquals(
            Ref(
                Type.of("com.example.Test"),
                setOf(promitiveProperty)
            ),
            filtered
        )
    }
}

private fun UnsafeRegexPredicate(
    input: String,
    ignoreCase: Boolean = false,
) = (RegexPredicate(input, ignoreCase) as Valid).t
