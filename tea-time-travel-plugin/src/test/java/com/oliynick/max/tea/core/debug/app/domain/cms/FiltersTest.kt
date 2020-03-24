package com.oliynick.max.tea.core.debug.app.domain.cms

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
    fun `test no filter is applied, whole Ref remains unchanged`() {

        val ref = Ref(
            Type.of("com.example.Test"),
            testProperties
        )

        val regex: Regex? = null

        regex.applyTo(ref) shouldBe ref
    }

    @Test
    fun `test if top level Ref matches expression, whole Ref remains unchanged`() {

        val ref = Ref(
            Type.of("com.example.Test"),
            testProperties
        )

        val filtered = Regex("com\\.example\\.Test")
            .applyTo(ref)

        filtered shouldBe ref
    }

    @Test
    fun `test if child's Ref type matches expression, only that Ref remains`() {

        val ref = Ref(
            Type.of("com.example.Test"),
            testProperties
        )

        val filtered = Regex("com\\.example\\.another.*")
            .applyTo(ref)

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

        val filtered = Regex(testProperties.first().name)
            .applyTo(ref)

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

        val filtered = Regex("int")
            .applyTo(ref)

        filtered shouldBe Ref(
            Type.of("com.example.Test"),
            setOf(promitiveProperty)
        )
    }


}