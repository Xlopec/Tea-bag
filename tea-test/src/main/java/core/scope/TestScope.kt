/*
 * Copyright (C) 2019 Maksym Oliinyk.
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

package core.scope

import kotlinx.coroutines.*
import kotlinx.coroutines.test.TestCoroutineScope
import kotlin.coroutines.CoroutineContext

inline val TestCoroutineScope.coroutineDispatcher
    get() = coroutineContext[CoroutineDispatcher.Key]!!

fun runBlockingInTestScope(
    context: CoroutineContext = Job(),
    block: suspend TestCoroutineScope.() -> Unit,
) {
    runBlocking { with(TestCoroutineScope(context)) { block() } }
}

fun runBlockingInNewScope(
    context: CoroutineContext = Dispatchers.Main + Job(),
    block: suspend CoroutineScope.() -> Unit,
) {
    runBlocking {

        val testScope = CoroutineScope(context)

        with(testScope) { block() }

        testScope.cancel()
    }
}


