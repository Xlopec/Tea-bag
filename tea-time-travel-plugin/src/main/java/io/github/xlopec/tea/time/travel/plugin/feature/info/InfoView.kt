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

package io.github.xlopec.tea.time.travel.plugin.feature.info

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign.Companion.Justify
import androidx.compose.ui.unit.sp
import io.github.xlopec.tea.time.travel.plugin.feature.component.ui.MessageHandler
import io.github.xlopec.tea.time.travel.plugin.feature.server.StartServer
import io.github.xlopec.tea.time.travel.plugin.model.Invalid
import io.github.xlopec.tea.time.travel.plugin.model.State
import io.github.xlopec.tea.time.travel.plugin.model.canStart
import io.github.xlopec.tea.time.travel.plugin.ui.theme.ActionIcons
import io.kanro.compose.jetbrains.control.ActionButton
import io.kanro.compose.jetbrains.control.Icon
import io.kanro.compose.jetbrains.control.Text
import java.util.*

internal const val InfoViewTag = "info view"

@Composable
fun InfoView(
    state: State,
    handler: MessageHandler,
) {
    Column(
        modifier = Modifier.fillMaxSize().testTag(InfoViewTag),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val content = remember(state.server, state.settings) { state.toContent(handler) }

        Text(text = content.description, textAlign = Justify, inlineContent = content.inlineContent)
    }
}

private data class InfoViewContent(
    val description: AnnotatedString,
    val inlineContent: Map<String, InlineTextContent> = mapOf(),
) {
    constructor(description: String) : this(AnnotatedString(description))
}

private fun State.toContent(
    handler: MessageHandler
): InfoViewContent =
    when {
        server != null -> serverRunningContent()
        canStart -> serverCanRunContent(handler)
        else -> invalidSettingsContent()
    }

private fun State.invalidSettingsContent() = InfoViewContent(
    description = "Can't start debug server: ${
        listOf(settings.port, settings.host)
            .filterIsInstance<Invalid>()
            .joinToString(postfix = "\n") { v -> v.message.replaceFirstChar { it.lowercase(Locale.getDefault()) } }
    }")

private fun serverRunningContent() = InfoViewContent(description = "There are no attached components yet")

private fun serverCanRunContent(handler: MessageHandler): InfoViewContent {
    val text = buildAnnotatedString {
        append("Debug server isn't running. Press ")
        appendInlineContent("run_icon")
        append(" to start")
    }

    val inlineContent = mapOf(
        "run_icon" to InlineTextContent(
            Placeholder(20.sp, 20.sp, PlaceholderVerticalAlign.TextCenter)
        ) {
            ActionButton(
                modifier = Modifier.fillMaxSize(),
                onClick = { handler(StartServer) }
            ) {
                Icon(
                    painter = ActionIcons.Execute,
                    modifier = Modifier.fillMaxSize(),
                    contentDescription = "Start server"
                )
            }
        }
    )

    return InfoViewContent(description = text, inlineContent = inlineContent)
}
