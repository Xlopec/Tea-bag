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

package com.oliynick.max.elm.time.travel.app.presentation.misc

import com.oliynick.max.elm.time.travel.app.presentation.sidebar.icon
import java.awt.Component
import java.io.File
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.ListCellRenderer

class FileCellRenderer : JLabel(), ListCellRenderer<File> {

    init {
        isOpaque = true
    }

    override fun getListCellRendererComponent(list: JList<out File>, value: File, index: Int,
                                              isSelected: Boolean, cellHasFocus: Boolean): Component {

        text = value.path

        icon = when {
            !value.exists() -> icon("javaOutsideSource")
            value.isDirectory -> icon("sourceFolder")
            value.isFile && value.extension.equals("class", true) -> icon("javaClass")
            value.isFile && value.extension.equals("zip", true) -> icon("archive")
            else -> icon("any_type")
        }

        if (isSelected) {
            background = list.selectionBackground
            foreground = list.selectionForeground
        } else {
            background = list.background
            foreground = list.foreground
        }

        return this
    }

}