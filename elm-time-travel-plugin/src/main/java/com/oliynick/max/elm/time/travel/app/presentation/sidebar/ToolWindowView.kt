package com.oliynick.max.elm.time.travel.app.presentation.sidebar

import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.oliynick.max.elm.core.component.Component
import com.oliynick.max.elm.time.travel.app.domain.*
import com.oliynick.max.elm.time.travel.app.presentation.misc.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import java.awt.event.MouseEvent
import java.io.File
import javax.swing.*
import java.awt.Component as AwtComponent

class ToolWindowView(private val project: Project,
                     private val scope: CoroutineScope,
                     private val component: Component<PluginMessage, PluginState>,
                     private val uiEvents: BroadcastChannel<PluginMessage>) : CoroutineScope by scope {

    private companion object {
        val CHOOSER_DESCRIPTOR: FileChooserDescriptor = FileChooserDescriptor(true, true, false, false, false, true)
            .withFileFilter { it.extension == "class" || it.isDirectory }
    }

    private lateinit var componentsTabPane: JTabbedPane
    private lateinit var panel: JPanel
    private lateinit var directoriesList: JList<File>
    private lateinit var removeDirectoryButton: JButton
    private lateinit var addDirectoryButton: JButton
    private lateinit var startButton: JButton
    private lateinit var transientStateProgress: JProgressBar

    private val directoriesListModel = DiffingListModel(FileDiffCallback)

    val root: JPanel get() = panel

    init {

        directoriesList.cellRenderer = FileCellRenderer()
        directoriesList.model = directoriesListModel

        componentsTabPane.model = DefaultSingleSelectionModel()

        launch { component(uiEvents.openSubscription().consumeAsFlow()).collect { state -> render(state, uiEvents) } }

        /*launch {
            component.changes().collect { state ->

                if (state.isTransient) {
                    ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Loh", false) {

                        override fun run(indicator: ProgressIndicator) {
                            indicator.isIndeterminate = true

                            //println("Thread ${Thread.currentThread()}"); Thread.sleep(5000L)
                        }

                    })
                }
            }
        }*/
    }

    private fun render(state: PluginState, messages: BroadcastChannel<PluginMessage>) {
        directoriesListModel.swap(state.settings.classFiles)
        transientStateProgress.isVisible = state.isTransient

        when (state) {
            is Stopped -> render(state, messages)
            is Starting -> render(state)
            is Started -> render(state, messages)
            is Stopping -> render(state)
        }.safe
    }

    private fun render(state: Stopped, messages: BroadcastChannel<PluginMessage>) {
        startButton.text = "Start"

        if (state.canStart) {
            startButton.setOnClickListenerEnabling { messages.offer(StartServer) }
        } else {
            startButton.removeMouseListenersDisabling()
        }

        removeDirectoryButton.setOnClickListenerEnabling {
            messages.offer(RemoveFiles(directoriesList.selectedValuesList))
        }

        addDirectoryButton.setOnClickListenerEnabling {
            project.chooseFiles(CHOOSER_DESCRIPTOR) { files -> messages.offer(AddFiles(files.map { File(it.path) })) }
        }
    }

    private fun render(state: Starting) {
        startButton.text = "Starting"
        startButton.removeMouseListenersDisabling()

        removeDirectoryButton.removeMouseListenersDisabling()
        addDirectoryButton.removeMouseListenersDisabling()
    }

    private fun render(state: Started, messages: BroadcastChannel<PluginMessage>) {
        startButton.text = "Stop"
        startButton.setOnClickListenerEnabling { messages.offer(StopServer) }

        removeDirectoryButton.removeMouseListenersDisabling()
        addDirectoryButton.removeMouseListenersDisabling()

        state.debugState.components
            .filter { e -> componentsTabPane.indexOfTab(e.key.id) == -1 }
            .forEach { (id, s) -> componentsTabPane.addTab(id.id, ComponentView(scope, component, s)._root) }
    }

    private fun render(state: Stopping) {
        startButton.text = "Stopping"
        startButton.removeMouseListenersDisabling()

        removeDirectoryButton.removeMouseListenersDisabling()
        addDirectoryButton.removeMouseListenersDisabling()
    }

}

private val PluginState.isTransient inline get() = this is Starting || this is Stopping

private fun AwtComponent.setOnClickListenerEnabling(l: (MouseEvent) -> Unit) {
    setOnClickListener(l)
    isEnabled = true
}

private fun AwtComponent.removeMouseListenersDisabling() {
    removeMouseListeners()
    isEnabled = false
}