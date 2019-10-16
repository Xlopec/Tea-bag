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

package com.oliynick.max.elm.time.travel.app.presentation.sidebar

import com.intellij.openapi.project.Project
import com.oliynick.max.elm.core.component.Component
import com.oliynick.max.elm.time.travel.app.domain.*
import com.oliynick.max.elm.time.travel.app.presentation.misc.chooseClassFiles
import com.oliynick.max.elm.time.travel.app.presentation.misc.safe
import com.oliynick.max.elm.time.travel.app.presentation.misc.setOnClickListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import javax.swing.JLabel
import javax.swing.JPanel
import kotlin.coroutines.CoroutineContext

class EmptyComponentsView(component: Component<PluginMessage, PluginState>,
                          context: CoroutineContext,
                          private val project: Project) : CoroutineScope {

    companion object {
        val NAME = EmptyComponentsView::class.simpleName!!
    }

    private lateinit var panel: JPanel
    private lateinit var messageText: JLabel

    override val coroutineContext = context + Job(context[Job.Key])

    val root get() = panel

    init {
        panel.name = NAME
    }

    init {
        launch {
            val uiEvents = Channel<PluginMessage>()

            component(uiEvents.consumeAsFlow()).collect { state ->
                render(state, uiEvents)
            }
        }
    }

    private fun render(state: PluginState, uiEvents: Channel<PluginMessage>) {
        when (state) {
            is Stopped -> render(state, uiEvents)
            is Started -> render(state)
            is Starting, is Stopping -> Unit
        }.safe
    }

    private fun render(state: Started) {
        messageText.text = "There are no attached components yet"
    }

    private fun render(state: Stopped, uiEvents: Channel<PluginMessage>) {
        messageText.text = """<html>Debug server isn't running
            |${if (state.canStart) ". <a href='#'>Add app's .class files or directories to track</a>" else ""}
            |</html>""".trimMargin()

        messageText.setOnClickListener {
            project.chooseClassFiles { files -> uiEvents.offer(AddFiles(files)) }
        }
    }

}
