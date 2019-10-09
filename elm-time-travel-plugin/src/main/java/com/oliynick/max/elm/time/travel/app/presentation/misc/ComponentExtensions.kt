package com.oliynick.max.elm.time.travel.app.presentation.misc

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.awt.Component
import java.awt.event.MouseEvent

inline fun Component.setOnClickListener(crossinline l: (MouseEvent) -> Unit) {
    removeMouseListeners()
    addMouseListener(object : DefaultMouseListener { override fun mouseClicked(e: MouseEvent) = l(e) })
}

fun Component.removeMouseListeners() {
    mouseListeners.forEach(this::removeMouseListener)
}

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