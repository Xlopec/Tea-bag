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

@file:Suppress("FunctionName")

package io.github.xlopec.tea.time.travel.plugin.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposePanel
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowExceptionHandler
import com.google.gson.GsonBuilder
import com.intellij.diagnostic.AttachmentFactory
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import io.github.xlopec.tea.time.travel.plugin.feature.component.ui.Component
import io.github.xlopec.tea.time.travel.plugin.feature.component.ui.ComponentTab
import io.github.xlopec.tea.time.travel.plugin.feature.component.ui.MessageHandler
import io.github.xlopec.tea.time.travel.plugin.feature.info.InfoView
import io.github.xlopec.tea.time.travel.plugin.feature.server.StartServer
import io.github.xlopec.tea.time.travel.plugin.feature.server.StopServer
import io.github.xlopec.tea.time.travel.plugin.feature.storage.ImportSession
import io.github.xlopec.tea.time.travel.plugin.integration.Message
import io.github.xlopec.tea.time.travel.plugin.integration.Platform
import io.github.xlopec.tea.time.travel.plugin.model.*
import io.github.xlopec.tea.time.travel.plugin.model.State
import io.github.xlopec.tea.time.travel.plugin.ui.theme.PluginTheme
import io.github.xlopec.tea.time.travel.plugin.util.toJson
import io.kanro.compose.jetbrains.control.JPanel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal val LocalPlatform = staticCompositionLocalOf<Platform> { error("No platform implementation provided") }

context (Logger, Project) internal fun PluginSwingAdapter(
    component: (Flow<Message>) -> Flow<State>,
) = ComposePanel()
    .apply {
        exceptionHandler = WindowExceptionHandler { th -> handleFatalException(component, th) }
        background = null
        setContent {
            PluginTheme {
                val platform = remember { Platform(this@Project, this@Logger) }
                Plugin(platform, component)
            }
        }
    }

@Composable
internal fun Plugin(
    platform: Platform,
    component: (Flow<Message>) -> Flow<State>,
    messages: MutableSharedFlow<Message> = remember { MutableSharedFlow() },
) {
    JPanel(modifier = Modifier.fillMaxSize()) {
        val stateFlow = remember { component(messages) }
        val state = stateFlow.collectAsState(initial = null).value

        if (state != null) {
            CompositionLocalProvider(LocalPlatform provides platform) {
                val scope = rememberCoroutineScope()
                val messageHandler = remember { scope.messageHandlerFor(messages) }

                Plugin(state, messageHandler)
            }
        }
    }
}

@Composable
internal fun Plugin(
    state: State,
    handler: MessageHandler,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier.weight(2f)
        ) {
            if (state.hasAttachedComponents) {
                ComponentsView(state, handler)
            } else {
                InfoView(state, handler)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // settings section
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            SettingsFields(
                modifier = Modifier.fillMaxWidth(),
                state = state,
                handler = handler
            )

            val scope = rememberCoroutineScope()
            val platform = LocalPlatform.current

            ActionsMenu(
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                onImportSession = { scope.launch { handler(ImportSession(platform.chooseSessionFile())) } },
                onExportSession = {
                    scope.launch {
                        platform.chooseExportSessionDirectory(state.debugger.componentIds)?.also(handler::invoke)
                    }
                },
                onServerAction = { handler(if (state.isStarted) StopServer else StartServer) },
                onSettingsAction = { scope.launch { platform.navigateToSettings() } },
                state = state
            )
        }
    }
}

internal fun CoroutineScope.messageHandlerFor(
    messages: FlowCollector<Message>,
): MessageHandler =
    { message -> launch { messages.emit(message) } }

@Composable
private fun ComponentsView(
    state: State,
    events: MessageHandler,
) {
    val debugger = state.debugger

    require(debugger.components.isNotEmpty())
    requireNotNull(debugger.selectedComponent)

    Row {
        debugger.components.values.forEach { component ->
            ComponentTab(component.id, debugger.selectedComponent, events)
        }
    }

    Component(state, debugger.componentOrThrow(debugger.selectedComponent), events)
}

context (Logger) private fun handleFatalException(
    component: (Flow<Message>) -> Flow<State>,
    th: Throwable,
) {
    runBlocking(Dispatchers.IO) {

        val attachment = component.currentStateOrNull()
            ?.let { createCrashLogFile(it) }
            ?.let { LocalFileSystem.getInstance().findFileByIoFile(it) }
            ?.let { AttachmentFactory.createAttachment(it) }

        if (attachment == null) {
            error(
                """
                Fatal error occurred inside Tea Time Travel plugin, no plugin state dump available.
                Please, fill a bug for this issue - $GithubIssuesLink with attached logs, stack traces, etc.
            """.trimIndent(),
                th
            )
        } else {
            error(
                """
                Fatal error occurred inside Tea Time Travel plugin.
                Please, fill a bug for this issue - $GithubIssuesLink with attached logs, stack traces, etc.
            """.trimIndent(),
                th,
                attachment
            )
        }
    }
}

private const val GithubIssuesLink = "https://github.com/Xlopec/Tea-bag/issues"

private suspend fun ((Flow<Message>) -> Flow<State>).currentStateOrNull(
    timeout: Duration = 1.seconds,
) = withTimeoutOrNull(timeout) { invoke(flowOf()).first() }

private suspend fun createCrashLogFile(
    state: State,
    timestamp: LocalDateTime = LocalDateTime.now(),
) = withContext(Dispatchers.IO) {
    File.createTempFile("tea-time-travel-crash-${DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(timestamp)}", ".json")
        .also { GsonBuilder().create().toJson(state, it) }
}
