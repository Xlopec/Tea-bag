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

package io.github.xlopec.tea.time.travel.plugin.feature.notification

import com.intellij.openapi.ui.popup.Balloon
import io.github.xlopec.tea.time.travel.plugin.Command
import io.github.xlopec.tea.time.travel.plugin.Message
import io.github.xlopec.tea.time.travel.plugin.PluginException
import io.github.xlopec.tea.time.travel.plugin.feature.presentation.ui.components.balloon.createErrorBalloon
import io.github.xlopec.tea.time.travel.plugin.feature.presentation.ui.components.balloon.createNotificationBalloon
import io.github.xlopec.tea.time.travel.plugin.model.state.State
import io.github.xlopec.tea.time.travel.protocol.ComponentId
import java.util.Locale

fun ExceptionBalloon(
    cause: PluginException,
    operation: Command?,
    description: String?,
): Balloon =
    createErrorBalloon(htmlDescription(cause, operation, description))

fun StateAppliedBalloon(
    componentId: ComponentId
): Balloon =
    createNotificationBalloon("""<html>
        <p>State was reapplied successfully for component "${componentId.value}"</p>
        </html>
    """.trimIndent())

fun ComponentAttachedBalloon(componentId: ComponentId): Balloon =
    createNotificationBalloon("""<html>
        <p>Component "${componentId.value}" attached to the plugin</p>
        </html>
    """.trimIndent())

fun UnacceptableMessageBalloon(
    message: Message,
    state: State
): Balloon =
    createErrorBalloon("""<html>
    |<p>Message $message can't be applied to 
    |state $state</p>
    |</html>
""".trimMargin())

private fun htmlDescription(
    cause: PluginException,
    operation: Command?,
    description: String?
): String =
    """<html>
        <p>An exception occurred ${(description ?: operation?.toString())?.let { ", $it" } ?: ""}</p>
        ${
        cause.message?.replaceFirstChar { it.lowercase(Locale.ENGLISH) }?.let { causeMessage -> """<p>Reason: $causeMessage</p>""" }
    }
        </html>
    """.trimMargin()
