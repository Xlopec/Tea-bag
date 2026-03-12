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

@file:Suppress("FunctionName")

package io.github.xlopec.reader.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.util.fastForEach
import io.github.xlopec.reader.app.command.Command
import io.github.xlopec.reader.app.command.DoLog
import io.github.xlopec.reader.app.command.DoStoreDarkMode
import io.github.xlopec.reader.app.command.ScreenCommand
import io.github.xlopec.reader.app.feature.article.details.ArticleDetailsResolver
import io.github.xlopec.reader.app.feature.article.details.DoOpenInBrowser
import io.github.xlopec.reader.app.feature.article.list.ArticlesCommand
import io.github.xlopec.reader.app.feature.article.list.ArticlesResolver
import io.github.xlopec.reader.app.feature.filter.FilterCommand
import io.github.xlopec.reader.app.feature.filter.FiltersResolver
import io.github.xlopec.reader.app.feature.storage.LocalStorage
import io.github.xlopec.tea.compose.ClockPolicy
import io.github.xlopec.tea.compose.ComposeResolver
import io.github.xlopec.tea.compose.SnapshotNotifierPolicy
import io.github.xlopec.tea.compose.TrackingEffect
import io.github.xlopec.tea.core.ResolveCtx
import io.github.xlopec.tea.core.Snapshot
import io.github.xlopec.tea.core.effects
import io.github.xlopec.tea.core.sideEffect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.transform

public fun <Env> AppResolver(): AppResolver<Env> where
    Env : ArticlesResolver<Env>,
    Env : LocalStorage,
    Env : FiltersResolver<Env>,
    Env : ArticleDetailsResolver =
    AppResolver { snapshots, ctx ->
        ComposeResolver(
            scope = CoroutineScope(ctx.coroutineContext + Job(ctx.coroutineContext[Job.Key]) + Dispatchers.Default),
            // todo do something with clock
            clockPolicy = ClockPolicy.Internal,
            snapshotManagerPolicy = SnapshotNotifierPolicy.External,
        ) {
            val snapshot by snapshots.collectAsState(null)
            val currentSnapshot = snapshot

            if (currentSnapshot != null) {
                val commands = snapshots.rememberCommands(currentSnapshot)

                currentSnapshot.currentState.screens.fastForEach { screen ->
                    key(screen.id) {
                        TrackingEffect(screen.id) {
                            println("Effect for ${screen.id}")
                            try {
                                commands
                                    .onEach { println("Cmd1 detected by ${screen.id} $it ") }
                                    .filter { it is ScreenCommand && it.id == screen.id }
                                    .onEach { println("Cmd11 $it") }
                                    .collect { command ->
                                        println("resolve $command")
                                        resolve(command, ctx)
                                    }
                            } finally {
                                println("Done with ${screen.id}")
                            }
                        }
                    }
                }

                LaunchedEffect(Unit) {
                    commands
                        .onEach { println("Cmd2 $it") }
                        .filter { it !is ScreenCommand || it.id == null }
                        .onEach { println("Cmd22 $it") }
                        .collect { command ->
                            resolve(command, ctx)
                        }
                }
            }
        }
    }

private fun <Env> Env.resolve(
    cmd: Command,
    ctx: ResolveCtx<Message>,
) where Env : ArticlesResolver<Env>,
        Env : LocalStorage,
        Env : FiltersResolver<Env>,
        Env : ArticleDetailsResolver {
    when (cmd) {
        is ArticlesCommand -> ctx effects { resolve(cmd) }
        is DoOpenInBrowser -> ctx sideEffect { resolve(cmd) }
        is DoStoreDarkMode -> ctx sideEffect {
            storeDarkModePreferences(cmd.userDarkModeEnabled, cmd.syncWithSystemDarkModeEnabled)
        }

        is FilterCommand -> ctx effects { resolve(cmd) }
        is DoLog -> ctx sideEffect { log(cmd) }
        else -> error("Shouldn't get here $cmd")
    }
}

@Composable
private fun Flow<Snapshot<*, AppState, Command>>.rememberCommands(
    initialSnapshot: Snapshot<*, AppState, Command>,
): Flow<Command> {
    val scope = rememberCoroutineScope()
    return remember(this, scope) {
        transform { snapshot -> snapshot.commands.forEach { emit(it) } }
            // command is gone by the time we compose the first tracking effect
            .onStart { initialSnapshot.commands.forEach { emit(it) } }
            // command emitted BEFORE LaunchedEffect has a chance to collect items
            .shareIn(scope, SharingStarted.Lazily, 1)
    }
}

private fun log(
    cmd: DoLog,
) {
    val screen = cmd.state.screens.find { it.id == cmd.id }

    val message = """App exception occurred, 
        |screen: ${screen?.let { it::class } ?: "unknown screen"}
        |caused by command ${cmd.causedBy}
    """.trimMargin()

    println(message)
    cmd.throwable.printStackTrace()
}
