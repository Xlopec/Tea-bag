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

package io.github.xlopec.tea.time.travel.plugin.util

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
