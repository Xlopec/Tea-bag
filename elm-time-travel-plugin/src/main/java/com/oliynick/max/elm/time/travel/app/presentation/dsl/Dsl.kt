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

package com.oliynick.max.elm.time.travel.app.presentation.dsl

import com.intellij.ui.layout.CCFlags
import com.intellij.ui.layout.Cell
import com.oliynick.max.elm.time.travel.app.presentation.misc.DefaultMouseListener
import java.awt.Color
import java.awt.event.MouseEvent
import javax.swing.Icon
import javax.swing.JLabel
import javax.swing.JTextField

inline fun Cell.textField(
    text: String = "",
    vararg constraints: CCFlags,
    init: JTextField.() -> Unit = {}
): JTextField {
    return JTextField(text).apply(init).also { it(*constraints) }
}

inline fun Cell.text(text: String = "", vararg constraints: CCFlags, init: JLabel.() -> Unit = {}): JLabel {
    return JLabel(text).apply(init).also { it(*constraints) }
}

inline fun Cell.iconButton(
    icon: Icon,
    disabled: Icon? = null,
    hover: Icon? = null,
    vararg constraints: CCFlags,
    init: JLabel.() -> Unit = {}
): JLabel {
    return JLabel(icon).apply(init).apply {
        disabledIcon = disabled
        if (hover != null) {
            setHover(hover)
        }
        this(*constraints)
    }
}

fun JLabel.setHover(hover: Icon) {

    val original = icon

    val l = object : DefaultMouseListener {
        override fun mouseEntered(e: MouseEvent) {
            if (isEnabled) {
                foreground = Color.WHITE
                background = Color.YELLOW
                icon = hover
            }
        }

        override fun mouseExited(e: MouseEvent) {
            if (isEnabled) {
                icon = original
            }
        }
    }

    addMouseListener(l)
}