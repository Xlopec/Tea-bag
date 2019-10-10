package com.oliynick.max.elm.time.travel.app.presentation.misc

import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.oliynick.max.elm.time.travel.app.domain.AddFiles
import java.io.File

val CLASS_FILES_CHOOSER_DESCRIPTOR: FileChooserDescriptor =
    FileChooserDescriptor(true, true, false, false, false, true)
        .withFileFilter { it.extension == "class" || it.isDirectory }

inline fun Project.chooseFiles(descriptor: FileChooserDescriptor, crossinline callback: (List<File>) -> Unit) {
    FileChooser.chooseFiles(descriptor, this, null, null) { virtualFiles ->
        callback(virtualFiles.map { File(it.path) })
    }
}

inline fun Project.chooseClassFiles(crossinline callback: (List<File>) -> Unit) {
    chooseFiles(CLASS_FILES_CHOOSER_DESCRIPTOR, callback)
}

/** forces compiler to check `when` clause is exhaustive */
val Unit?.safe get() = this