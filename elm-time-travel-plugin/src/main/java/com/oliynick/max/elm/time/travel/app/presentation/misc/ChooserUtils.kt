package com.oliynick.max.elm.time.travel.app.presentation.misc

import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

fun Project.chooseFiles(descriptor: FileChooserDescriptor, callback: (List<VirtualFile>) -> Unit) {
    FileChooser.chooseFiles(descriptor, this, null, null, callback)
}

/** forces compiler to check `when` clause is exhaustive */
val Unit?.safe get() = this