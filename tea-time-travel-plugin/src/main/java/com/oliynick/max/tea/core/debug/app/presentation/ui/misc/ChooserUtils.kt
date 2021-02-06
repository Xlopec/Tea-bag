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

package com.oliynick.max.tea.core.debug.app.presentation.ui.misc

import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import java.io.File

val CLASS_FILES_CHOOSER_DESCRIPTOR: FileChooserDescriptor =
    FileChooserDescriptor(true, true, false, false, false, true)
        .withFileFilter { it.extension == "class" || it.isDirectory }

inline fun Project.chooseFiles(
    descriptor: FileChooserDescriptor,
    crossinline callback: (List<File>) -> Unit
) {
    FileChooser.chooseFiles(descriptor, this, null, null) { virtualFiles ->
        callback(virtualFiles.map { File(it.path) })
    }
}

inline fun Project.chooseClassFiles(crossinline callback: (List<File>) -> Unit) {
    chooseFiles(CLASS_FILES_CHOOSER_DESCRIPTOR, callback)
}

/** forces compiler to check `when` clause is exhaustive */
val <T> T.safe get() = this