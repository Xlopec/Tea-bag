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

package com.oliynick.max.tea.core.debug.app.presentation.sidebar

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory
import com.oliynick.max.tea.core.component.Component
import com.oliynick.max.tea.core.component.states
import com.oliynick.max.tea.core.debug.app.domain.cms.PluginCommand
import com.oliynick.max.tea.core.debug.app.domain.cms.PluginMessage
import com.oliynick.max.tea.core.debug.app.domain.cms.PluginState
import com.oliynick.max.tea.core.debug.app.env.Environment
import com.oliynick.max.tea.core.debug.app.env.PluginComponent
import com.oliynick.max.tea.core.debug.app.storage.properties
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class SideToolWindowFactory : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(
        project: Project,
        toolWindow: ToolWindow
    ) {
        val environment = Environment(project.properties, project)
        val component = PluginComponent(environment)
        val content = createToolWindowContent(project, environment, component)

        toolWindow.contentManager.addContent(content)
        // todo: find a better approach
        environment.launch {
            component(environment.events.asFlow()).collect()
        }
    }

    override fun shouldBeAvailable(project: Project): Boolean = true
}

private fun createToolWindowContent(
    project: Project,
    environment: Environment,
    component: Component<PluginMessage, PluginState, PluginCommand>
): Content {

    val myToolWindow = ToolWindowView(project, environment, component.states())
    val contentFactory = ContentFactory.SERVICE.getInstance()

    return contentFactory.createContent(myToolWindow.root, null, false)
}