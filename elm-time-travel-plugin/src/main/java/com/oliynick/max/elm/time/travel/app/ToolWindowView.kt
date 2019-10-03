package com.oliynick.max.elm.time.travel.app

import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.oliynick.max.elm.core.component.androidLogger
import com.oliynick.max.elm.core.component.component
import com.oliynick.max.elm.time.travel.app.editor.*
import com.oliynick.max.elm.time.travel.app.misc.DiffCallback
import com.oliynick.max.elm.time.travel.app.misc.DiffingListModel
import com.oliynick.max.elm.time.travel.app.misc.VirtualFileCellRenderer
import com.oliynick.max.elm.time.travel.app.misc.addOnClickListener
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import javax.swing.*
import javax.swing.tree.DefaultTreeModel

@UseExperimental(InternalCoroutinesApi::class)
class ToolWindowView(private val project: Project) {

    private companion object {
        val FILE_CHOOSER_DESCRIPTOR: FileChooserDescriptor =
            FileChooserDescriptor(true, true, false, false, false, true)
                .withFileFilter { it.extension == "class" || it.isDirectory }
    }

    private lateinit var commandsTree: JTree
    private lateinit var panel: JPanel
    private lateinit var directoriesList: JList<VirtualFile>
    private lateinit var removeDirectoryButton: JButton
    private lateinit var addDirectoryButton: JButton
    private lateinit var startButton: JButton
    private lateinit var transientStateProgress: JProgressBar

    object VirtualFileDiffCallback : DiffCallback<VirtualFile, VirtualFile> {
        override fun areContentsTheSame(oldItem: VirtualFile, newItem: VirtualFile): Boolean = oldItem == newItem
        override fun areItemsTheSame(oldItem: VirtualFile, newItem: VirtualFile): Boolean = oldItem.path == newItem.path
    }

    private val directoriesListModel = DiffingListModel(VirtualFileDiffCallback)

    val root: JPanel get() = panel

    init {

        val uiEvents = Channel<PluginMessage>()

        directoriesList.cellRenderer = VirtualFileCellRenderer()
        directoriesList.model = directoriesListModel

        addDirectoryButton.addOnClickListener {
            FileChooser.chooseFiles(FILE_CHOOSER_DESCRIPTOR, project, null, null) { files ->
                uiEvents.offer(AddFiles(files))
            }
        }

        removeDirectoryButton.addOnClickListener {
            uiEvents.offer(RemoveFiles(directoriesList.selectedValuesList))
        }

        startButton.addOnClickListener {
            uiEvents.offer(StartServer)
        }

        val a = A("max", Compl(IntB(124), StringB("kek"), 1488))

        commandsTree.cellRenderer = ObjectTreeRenderer()
        commandsTree.model = DefaultTreeModel(a.toJTree())

        GlobalScope.launch {
            val component = component(
                Stopped(Settings(ServerSettings())),
                ::resolve,
                ::update,
                androidLogger("Plugin Component")
            )

            component(uiEvents.consumeAsFlow()).collect { state ->
                withContext(Dispatchers.Main) {
                    render(state, uiEvents)
                }
            }
        }
    }

    private fun render(state: PluginState, messages: Channel<PluginMessage>) {
        directoriesListModel.swap(state.settings.classFiles)

        when (state) {
            is Stopped -> render(state)
            is Starting -> render(state)
            is Running -> render(state)
            is Stopping -> render(state)
        }
    }

    private fun render(state: Stopped) {
        startButton.text = "Start"
        startButton.isEnabled = state.canStart
        removeDirectoryButton.isEnabled = state.settings.classFiles.isNotEmpty()
        transientStateProgress.isVisible = false
    }

    private fun render(state: Starting) {
        startButton.text = "Starting"
        startButton.isEnabled = false
        removeDirectoryButton.isEnabled = false
        addDirectoryButton.isEnabled = false
        transientStateProgress.isVisible = true
    }

    private fun render(state: Running) {
        startButton.text = "Stop"
        startButton.isEnabled = true
        removeDirectoryButton.isEnabled = false
        addDirectoryButton.isEnabled = false
        transientStateProgress.isVisible = false
    }

    private fun render(state: Stopping) {
        startButton.text = "Stopping"
        startButton.isEnabled = false
        removeDirectoryButton.isEnabled = false
        addDirectoryButton.isEnabled = false
        transientStateProgress.isVisible = true
    }

}
