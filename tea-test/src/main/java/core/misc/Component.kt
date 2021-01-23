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

@file:Suppress("FunctionName")

package core.misc

import com.oliynick.max.tea.core.*
import com.oliynick.max.tea.core.component.*
import core.scope.coroutineDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope

@OptIn(UnstableApi::class)
fun <M, S, C> TestCoroutineScope.TestEnv(
    initializer: Initializer<S, C>,
    resolver: Resolver<C, M>,
    updater: Updater<M, S, C>,
    io: CoroutineDispatcher = coroutineDispatcher,
    computation: CoroutineDispatcher = coroutineDispatcher,
    shareOptions: ShareOptions = ShareStateWhileSubscribed,
) = Env(
    initializer,
    resolver,
    updater,
    this,
    io,
    computation,
    shareOptions
)

@Suppress("RedundantSuspendModifier")
suspend fun <C> throwingResolver(
    c: C,
): Nothing =
    throw IllegalStateException("Unexpected command $c")

fun <M, S> throwingUpdater(
    m: M,
    s: S,
): Nothing =
    throw IllegalStateException("message=$m, state=$s")

fun <S> messageAsStateUpdate(
    message: S,
    @Suppress("UNUSED_PARAMETER") state: S,
): UpdateWith<S, S> =
    message.noCommand()

fun <M, S> messageAsCommand(
    message: M,
    @Suppress("UNUSED_PARAMETER") state: S,
): UpdateWith<S, M> =
    state command message

fun <M, S> ignoringMessageAsStateUpdate(
    message: M,
    @Suppress("UNUSED_PARAMETER") state: S,
): UpdateWith<M, S> =
    message.noCommand()
