package com.oliynick.max.elm.time.travel.app

import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.oliynick.max.elm.time.travel.app.misc.SetListModel
import com.oliynick.max.elm.time.travel.app.misc.VirtualFileCellRenderer
import com.oliynick.max.elm.time.travel.app.misc.addOnClickListener
import javax.swing.JButton
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.JTree
import javax.swing.tree.DefaultTreeModel

class ToolWindowView(private val project: Project) {

    private companion object {
        val FILE_CHOOSER_DESCRIPTOR: FileChooserDescriptor = FileChooserDescriptor(true, true, false, false, false, true)
            .withFileFilter { it.extension == "class" || it.isDirectory }
    }

    private lateinit var commandsTree: JTree
    private lateinit var panel: JPanel
    private lateinit var directoriesList: JList<VirtualFile>
    private lateinit var removeDirectoryButton: JButton
    private lateinit var addDirectoryButton: JButton

    private val directoriesListModel = SetListModel<VirtualFile>()

    val root: JPanel get() = panel

    init {
        directoriesList.cellRenderer = VirtualFileCellRenderer()
        directoriesList.model = directoriesListModel

        addDirectoryButton.addOnClickListener {
            FileChooser.chooseFiles(FILE_CHOOSER_DESCRIPTOR, project, null, null) { files ->
                directoriesListModel += files
            }
        }

        removeDirectoryButton.addOnClickListener {
            directoriesListModel -= directoriesList.selectedValuesList
        }

        val a = A("max", Compl(IntB(124), StringB("kek"), 1488))

        commandsTree.cellRenderer = ObjectTreeRenderer()
        commandsTree.model = DefaultTreeModel(a.toJTree())
    }

}
