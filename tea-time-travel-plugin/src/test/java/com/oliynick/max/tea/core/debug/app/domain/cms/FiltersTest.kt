@file:Suppress("TestFunctionName")

package com.oliynick.max.tea.core.debug.app.domain.cms

import com.oliynick.max.tea.core.debug.app.domain.IntWrapper
import com.oliynick.max.tea.core.debug.app.domain.Property
import com.oliynick.max.tea.core.debug.app.domain.Ref
import com.oliynick.max.tea.core.debug.app.domain.RegexPredicate
import com.oliynick.max.tea.core.debug.app.domain.Type
import com.oliynick.max.tea.core.debug.app.domain.Valid
import com.oliynick.max.tea.core.debug.app.domain.applyTo
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
    fun `test if Primitive property type matches expression, whole Ref remains remains unchanged`() {

        val promitiveProperty = Property("primitive", IntWrapper(10))

        val ref = Ref(
            Type.of("com.example.Test"),
            testProperties + promitiveProperty
        )

        val filtered = applyTo(ref, UnsafeRegexPredicate("int"))

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