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

package com.oliynick.max.tea.core.debug.app.domain.cms

import com.google.gson.internal.LazilyParsedNumber
import com.oliynick.max.tea.core.debug.app.domain.*
import io.kotlintest.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class FiltersTest {

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

        val filtered = applyTo(ref, (RegexPredicate("com\\.example\\.Test", false) as Valid).t)

        filtered shouldBe ref
    }

    @Test
    fun `test if child's Ref type matches expression, only that Ref remains`() {

        val ref = Ref(
            Type.of("com.example.Test"),
            testProperties
        )

        val filtered = applyTo(ref, UnsafeRegexPredicate("com\\.example\\.another.*"))

        filtered shouldBe Ref(
            Type.of("com.example.Test"),
            setOf(testProperties.first())
        )
    }

    @Test
    fun `test if child's Ref property name matches expression, only that Ref remains`() {

        val ref = Ref(
            Type.of("com.example.Test"),
            testProperties
        )

        val filtered = applyTo(ref, UnsafeRegexPredicate(testProperties.first().name))

        filtered shouldBe Ref(
            Type.of("com.example.Test"),
            setOf(testProperties.first())
        )
    }

    @Test
    fun `test if number property contains some custom Number implementation, given regex is 'number' only this property remains in the Ref`() {

        val promitiveProperty = Property("primitive", NumberWrapper(LazilyParsedNumber(10.toString())))

        val ref = Ref(
            Type.of("com.example.Test"),
            testProperties + promitiveProperty
        )

        val filtered = applyTo(ref, UnsafeRegexPredicate("number"))

        filtered shouldBe Ref(
            Type.of("com.example.Test"),
            setOf(promitiveProperty)
        )
    }

    @Test
    fun `test if number property contains Int, given regex is 'int' only this property remains in the Ref`() {

        val promitiveProperty = Property("primitive", NumberWrapper(10))

        val ref = Ref(
            Type.of("com.example.Test"),
            testProperties + promitiveProperty
        )

        val filtered = applyTo(ref, UnsafeRegexPredicate("java.lang.Integer"))

        filtered shouldBe Ref(
            Type.of("com.example.Test"),
            setOf(promitiveProperty)
        )
    }


}

private fun UnsafeRegexPredicate(
    input: String,
    ignoreCase: Boolean = false
) =
    (RegexPredicate(input, ignoreCase) as Valid).t