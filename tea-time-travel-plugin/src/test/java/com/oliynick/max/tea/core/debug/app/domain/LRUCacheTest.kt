package com.oliynick.max.tea.core.debug.app.domain

import com.oliynick.max.tea.core.debug.app.misc.LruCache
import io.kotlintest.matchers.types.shouldBeSameInstanceAs
import io.kotlintest.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

// todo implement property-based tests
@RunWith(JUnit4::class)
internal class LRUCacheTest {

    @Test
    fun `test cache should return same reference for the same input`() {
        val lru =
            LruCache<String, Int>(10U)

        lru.getOrPut("") { 0 } shouldBeSameInstanceAs lru.getOrPut("") { 0 }
        lru.size shouldBe 1U
    }

    @Test
    fun `test cache should return same reference for the same input no matter how many elements it has`() {
        val lru =
            LruCache<String, Int>(10U)

        lru.getOrPut("a") { 1 }
        lru.getOrPut("b") { 2 }
        lru.getOrPut("") { 0 } shouldBeSameInstanceAs lru.getOrPut("") { 0 }

        lru.size shouldBe 3U
    }

    @Test
    fun `test cache should return same null reference for the same null input no matter how many elements it has`() {
        val lru =
            LruCache<String?, Int?>(10U)

        lru.getOrPut("a") { 1 }
        lru.getOrPut("b") { 2 }
        lru.getOrPut(null) { null } shouldBeSameInstanceAs lru.getOrPut(null) { null }

        lru.size shouldBe 3U
    }

    @Test
    fun `test cache should handle capacity overflow correctly`() {

        val lru =
            LruCache<String?, Int?>(5U)

        lru.getOrPut("a") { 1 } shouldBeSameInstanceAs lru.getOrPut("a") { 1 }
        lru.getOrPut("b") { 2 } shouldBeSameInstanceAs lru.getOrPut("b") { 2 }
        lru.getOrPut("c") { 2 } shouldBeSameInstanceAs lru.getOrPut("c") { 2 }
        lru.getOrPut("d") { 2 } shouldBeSameInstanceAs lru.getOrPut("d") { 2 }
        lru.getOrPut("e") { 2 } shouldBeSameInstanceAs lru.getOrPut("e") { 2 }
        lru.getOrPut("f") { 2 } shouldBeSameInstanceAs lru.getOrPut("f") { 2 }
        lru.getOrPut(null) { null } shouldBeSameInstanceAs lru.getOrPut(null) { null }

        lru.size shouldBe 5U
    }

}