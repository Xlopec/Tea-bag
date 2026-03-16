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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.util.fastForEach
import io.github.xlopec.reader.app.command.Command
import io.github.xlopec.reader.app.command.DoLog
import io.github.xlopec.reader.app.command.DoStoreDarkMode
import io.github.xlopec.reader.app.command.ScreenCommand
import io.github.xlopec.reader.app.feature.article.details.BrowserLauncher
import io.github.xlopec.reader.app.feature.article.details.DoOpenInBrowser
import io.github.xlopec.reader.app.feature.article.details.resolveForOpenInBrowser
import io.github.xlopec.reader.app.feature.article.list.ArticlesCommand
import io.github.xlopec.reader.app.feature.article.list.NewsApi
import io.github.xlopec.reader.app.feature.article.list.ShareArticle
import io.github.xlopec.reader.app.feature.article.list.resolveForArticles
import io.github.xlopec.reader.app.feature.filter.FilterCommand
import io.github.xlopec.reader.app.feature.filter.resolveForFilter
import io.github.xlopec.reader.app.feature.storage.LocalStorage
import io.github.xlopec.tea.compose.ClockPolicy
import io.github.xlopec.tea.compose.ComposeResolver
import io.github.xlopec.tea.compose.SnapshotNotifierPolicy
import io.github.xlopec.tea.compose.TrackingEffect
import io.github.xlopec.tea.compose.TrackingScope
import io.github.xlopec.tea.core.Sink
import io.github.xlopec.tea.core.Snapshot
import io.github.xlopec.tea.core.sideEffect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.transform

context(sink: Sink<Message>, scope: CoroutineScope)
public fun <Env> Env.resolve(
    snapshots: Flow<Snapshot<Message, AppState, Command>>,
) where Env : NewsApi,
        Env : LocalStorage,
        Env : ShareArticle,
        Env : BrowserLauncher {

    val appScope = contextOf<CoroutineScope>()
    val sink = contextOf<Sink<Message>>()
    val compositionScope = CoroutineScope(appScope.coroutineContext + Job(appScope.coroutineContext[Job.Key]) + Dispatchers.Default)
    ComposeResolver(
        scope = compositionScope,
        // todo do something with clock
        clockPolicy = ClockPolicy.Internal,
        snapshotManagerPolicy = SnapshotNotifierPolicy.External,
    ) {
        val snapshot by snapshots.collectAsState(null)
        val currentSnapshot = snapshot

        if (currentSnapshot != null) {
            val commands = snapshots.rememberCommands()

            currentSnapshot.currentState.screens.fastForEach { screen ->
                key(screen.id) {
                    TrackingEffect(screen.id) {
                        commands
                            .filter { it is ScreenCommand && it.id == screen.id }
                            .collect { command ->
                                context(sink) { resolve(command) }
                            }
                    }
                }
            }

            TrackingEffect(Unit) {
                commands
                    .filter { it !is ScreenCommand || it.id == null }
                    .collect { command ->
                        context(sink) { resolve(command) }
                    }
            }
        }
    }
}

context(_: Sink<Message>, _: TrackingScope)
private fun <Env> Env.resolve(
    cmd: Command,
) where Env : LocalStorage,
        Env : NewsApi,
        Env : ShareArticle,
        Env : BrowserLauncher {
    when (cmd) {
        is ArticlesCommand -> resolveForArticles(cmd)
        is DoOpenInBrowser -> resolveForOpenInBrowser(cmd)
        is DoStoreDarkMode -> sideEffect {
            storeDarkModePreferences(cmd.userDarkModeEnabled, cmd.syncWithSystemDarkModeEnabled)
        }

        is FilterCommand -> resolveForFilter(cmd)
        is DoLog -> sideEffect { log(cmd) }
        else -> error("Shouldn't get here $cmd")
    }
}

@Composable
private fun Flow<Snapshot<*, AppState, Command>>.rememberCommands(): Flow<Command> {
    return remember(this) {
        transform { snapshot -> snapshot.commands.forEach { emit(it) } }
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
