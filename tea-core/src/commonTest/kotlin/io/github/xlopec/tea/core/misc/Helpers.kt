/*
 * MIT License
 *
 * Copyright (c) 2022. Maksym Oliinyk.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

@file:Suppress("FunctionName", "TestFunctionName")

package io.github.xlopec.tea.core.misc

import app.cash.turbine.ReceiveTurbine
import io.github.xlopec.tea.core.Component
import io.github.xlopec.tea.core.Env
import io.github.xlopec.tea.core.Initializer
import io.github.xlopec.tea.core.Resolver
import io.github.xlopec.tea.core.ShareOptions
import io.github.xlopec.tea.core.ShareStateWhileSubscribed
import io.github.xlopec.tea.core.Snapshot
import io.github.xlopec.tea.core.Updater
import io.github.xlopec.tea.core.invoke
import io.github.xlopec.tea.core.noCommand
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlin.math.abs
import kotlin.test.assertNotEquals

const val TestTimeoutMillis = 10 * 1000L

inline val CoroutineScope.job: Job
    get() = coroutineContext[Job.Key] ?: error("scope doesn't have job $this")

fun <M, S, C> TestEnv(
    initializer: Initializer<S, C>,
    resolver: Resolver<M, S, C>,
    updater: Updater<M, S, C>,
    scope: CoroutineScope,
    shareOptions: ShareOptions<Snapshot<M, S, C>> = ShareStateWhileSubscribed(),
) = Env(
    initializer = initializer,
    resolver = { snapshot -> resolver(snapshot) },
    updater = updater,
    scope = scope,
    shareOptions = shareOptions
)

fun ThrowingInitializer(
    th: Throwable,
): Initializer<Nothing, Nothing> = { throw th }

fun <M, S> CheckingUpdater(
    mainThreadName: String,
): Updater<M, S, Nothing> = { _, s ->

    val actualThreadNamePrefix = currentThreadName().replaceAfterLast('@', "")
    val mainThreadNamePrefix = mainThreadName.replaceAfterLast('@', "")

    assertNotEquals(mainThreadNamePrefix, actualThreadNamePrefix)

    s.noCommand()
}

internal val CharRange.size: Int
    get() = 1 + abs(last - first)

internal suspend fun Component<Char, String, Char>.collectRanged(
    messages: CharRange,
) = this(messages).take(messages.size + 1).collect() // plus initial snapshot

internal suspend fun ReceiveTurbine<*>.expectCompletionAndCancel() {
    awaitComplete()
    cancel()
}
