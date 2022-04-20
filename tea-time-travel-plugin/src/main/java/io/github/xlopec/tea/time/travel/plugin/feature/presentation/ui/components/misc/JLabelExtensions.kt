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

package io.github.xlopec.tea.time.travel.plugin.feature.presentation.ui.components.misc

import io.github.xlopec.tea.time.travel.plugin.feature.presentation.ui.components.action.DefaultMouseListener
import java.awt.Color
import java.awt.event.MouseEvent
import javax.swing.Icon
import javax.swing.JLabel

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
