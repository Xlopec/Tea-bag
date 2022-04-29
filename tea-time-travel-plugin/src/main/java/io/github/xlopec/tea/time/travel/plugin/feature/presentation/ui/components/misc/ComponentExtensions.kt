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
import java.awt.Component
import java.awt.event.MouseEvent
import javax.swing.SwingUtilities
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

inline fun Component.setOnClickListener(crossinline l: (MouseEvent) -> Unit) {
    removeMouseListeners()
    addMouseListener(object : DefaultMouseListener {
        override fun mouseClicked(e: MouseEvent) = l(e)
    })
}

fun Component.removeMouseListeners() {
    mouseListeners.forEach(this::removeMouseListener)
}

fun Component.mouseEvents(): Flow<MouseEvent> =
    callbackFlow {

        val l = object : DefaultMouseListener {
            override fun mouseClicked(e: MouseEvent) {
                offer(e)
            }
        }

        addMouseListener(l)

        awaitClose { removeMouseListener(l) }
    }

fun Component.rightClicks(): Flow<MouseEvent> =
    mouseEvents().filter { SwingUtilities.isRightMouseButton(it) }

fun Component.leftClicks(): Flow<MouseEvent> =
    mouseEvents().filter { SwingUtilities.isLeftMouseButton(it) }

fun <T> Flow<T>.mergeWith(other: Flow<T>): Flow<T> =
    channelFlow {
        coroutineScope {
            launch {
                other.collect {
                    offer(it)
                }
            }

            launch {
                collect {
                    offer(it)
                }
            }
        }
    }
