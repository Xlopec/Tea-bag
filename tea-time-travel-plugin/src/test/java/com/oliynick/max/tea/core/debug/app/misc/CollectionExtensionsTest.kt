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

package com.oliynick.max.tea.core.debug.app.misc

import io.kotlintest.matchers.collections.shouldContainExactly
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
internal class CollectionExtensionsTest {

    private object DiffCallbackImp : DiffCallback<Int, Int> {
        override fun areItemsTheSame(oldItem: Int, newItem: Int): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Int, newItem: Int): Boolean {
            return oldItem == newItem
        }
    }

    @Test
    fun `test receiver collection should be equal target collection`() {

        val input = mutableListOf(109, 150, 156, 158)
        val expected = listOf(150, 158, 228, 234)

        input.replaceAll(expected, DiffCallbackImp)

        input shouldContainExactly expected
    }

    @Test
    fun `test receiver collection is bigger than target collection`() {

        val input = mutableListOf(263, 110, 109, 156, 158, 161, 166, 170, 176, 186, 150, 189, 194, 197, 199, 204, 176, 186, 150, 189, 194, 197, 199, 204)
        val expected = listOf(263, 110, 109, 156, 158, 161, 166, 170)

        input.replaceAll(expected, DiffCallbackImp)

        input shouldContainExactly expected
    }

    @Test
    fun `test receiver collection equals target collection`() {

        val input = mutableListOf(109, 150, 156, 158)
        val expected = listOf(109, 150, 156, 158)

        input.replaceAll(expected, DiffCallbackImp)

        input shouldContainExactly expected
    }

    @Test
    fun `test receiver collection is empty`() {

        val input = mutableListOf<Int>()
        val expected = listOf(11, 34)

        input.replaceAll(expected, DiffCallbackImp)

        input shouldContainExactly expected
    }

    @Test
    fun `test expected collection is empty`() {

        val input = mutableListOf(1, 2, 3, 4, 5)
        val expected = mutableListOf<Int>()

        input.replaceAll(expected, DiffCallbackImp)

        input shouldContainExactly expected
    }

    @Test
    fun `test both collections are empty`() {

        val input = mutableListOf<Int>()
        val expected = mutableListOf<Int>()

        input.replaceAll(expected, DiffCallbackImp)

        input shouldContainExactly expected
    }
}
