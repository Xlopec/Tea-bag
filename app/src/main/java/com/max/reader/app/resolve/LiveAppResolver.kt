/*
 * MIT License
 *
 * Copyright (c) 2021. Maksym Oliinyk.
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

@file:Suppress("FunctionName")

package com.max.reader.app.resolve

import com.max.reader.app.*
import com.max.reader.screens.article.details.resolve.ArticleDetailsResolver
import com.max.reader.screens.article.list.resolve.ArticlesResolver
import com.oliynick.max.tea.core.component.sideEffect
import kotlinx.coroutines.channels.BroadcastChannel

@Deprecated("wait until it'll be fixed")
fun <Env> AppResolver(): AppResolver<Env> where Env : HasCommandTransport,
                                                Env : ArticlesResolver<Env>,
                                                Env : ArticleDetailsResolver<Env> = object : AppResolver<Env> {
    override suspend fun Env.resolve(command: Command): Set<Message> =
        when (command) {
            is CloseApp -> close(command)
            is ArticlesCommand -> resolve(command)
            is ArticleDetailsCommand -> resolve(command)
        }

    suspend fun Env.close(
        command: CloseApp
    ): Set<Message> = command.sideEffect { closeCommands.send(command) }
}

interface HasCommandTransport {
    val closeCommands: BroadcastChannel<CloseApp>
}

fun CommandTransport() = object : HasCommandTransport {
    override val closeCommands: BroadcastChannel<CloseApp> = BroadcastChannel(1)
}
