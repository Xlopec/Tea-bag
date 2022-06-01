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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposePanel
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowExceptionHandler
import com.google.gson.GsonBuilder
import com.intellij.diagnostic.AttachmentFactory
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import io.github.xlopec.tea.time.travel.plugin.feature.component.ui.Component
import io.github.xlopec.tea.time.travel.plugin.feature.component.ui.ComponentTab
import io.github.xlopec.tea.time.travel.plugin.feature.component.ui.MessageHandler
import io.github.xlopec.tea.time.travel.plugin.feature.info.InfoView
import io.github.xlopec.tea.time.travel.plugin.feature.settings.Settings
import io.github.xlopec.tea.time.travel.plugin.feature.storage.ImportSession
import io.github.xlopec.tea.time.travel.plugin.integration.Message
import io.github.xlopec.tea.time.travel.plugin.integration.Platform
import io.github.xlopec.tea.time.travel.plugin.model.Debugger
import io.github.xlopec.tea.time.travel.plugin.model.State
import io.github.xlopec.tea.time.travel.plugin.model.component
import io.github.xlopec.tea.time.travel.plugin.model.componentIds
import io.github.xlopec.tea.time.travel.plugin.model.hasAttachedComponents
import io.github.xlopec.tea.time.travel.plugin.ui.theme.PluginTheme
import io.github.xlopec.tea.time.travel.plugin.util.PluginId
import io.github.xlopec.tea.time.travel.plugin.util.toJson
import io.kanro.compose.jetbrains.control.JPanel
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

internal val LocalPlatform = staticCompositionLocalOf<Platform> { error("No platform implementation provided") }

@OptIn(ExperimentalComposeUiApi::class)
internal fun PluginSwingAdapter(
    project: Project,
    component: (Flow<Message>) -> Flow<State>,
) = ComposePanel()
    .apply {
        exceptionHandler = WindowExceptionHandler { th -> handleFatalException(component, th) }
        background = null
        setContent {
            PluginTheme {
                val platform = remember { Platform(project) }
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
    events: MessageHandler,
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
                ComponentsView(state.settings, state.debugger, events)
            } else {
                InfoView(state, events)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // settings section
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            SettingsFields(state, events)

            val scope = rememberCoroutineScope()
            val platform = LocalPlatform.current

            BottomActionMenu(
                state = state,
                events = events,
                onImportSession = { scope.launch { events(ImportSession(platform.chooseSessionFile())) } },
                onExportSession = {
                    scope.launch {
                        platform.chooseExportSessionDirectory(state.debugger.componentIds)?.also(events::invoke)
                    }
                }
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
    settings: Settings,
    debugger: Debugger,
    events: MessageHandler,
) {
    require(debugger.components.isNotEmpty())

    val selectedId = remember { mutableStateOf(debugger.components.keys.first()) }
    val selectedIndex by derivedStateOf { debugger.components.keys.indexOf(selectedId.value) }

    require(selectedIndex >= 0) {
        """Inconsistency in tab indexing detected, 
                            |selected id: ${selectedId.value}, 
                            |selected index: $selectedIndex
                            |component ids: ${debugger.componentIds}"""".trimMargin()
    }

    Row {
        debugger.componentIds.forEachIndexed { index, id ->
            ComponentTab(id, selectedId, debugger, index, events)
        }
    }

    val component by derivedStateOf { debugger.component(selectedId.value) }

    Component(settings, component, events)
}

private fun handleFatalException(
    component: (Flow<Message>) -> Flow<State>,
    th: Throwable,
) {
    runBlocking(Dispatchers.IO) {
        val attachment = component.currentStateOrNull()
            ?.let { createCrashLogFile(it) }
            ?.let { AttachmentFactory.createAttachment(it, false) }

        val logger = Logger.getInstance(PluginId)

        if (attachment == null) {
            logger.error("""
                Fatal error occurred inside Tea Time Travel plugin, no plugin state dump available.
                Please, fill a bug for this issue - $GithubIssuesLink with attached logs, stack traces, etc.
            """.trimIndent(), th)
        } else {
            logger.error("""
                Fatal error occurred inside Tea Time Travel plugin.
                Please, fill a bug for this issue - $GithubIssuesLink with attached logs, stack traces, etc.
            """.trimIndent(), th, attachment)
        }
    }
}

private const val GithubIssuesLink = "https://github.com/Xlopec/Tea-bag/issues"

@OptIn(ExperimentalTime::class)
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
