package com.oliynick.max.elm.time.travel.app.presentation.misc

import java.awt.Component
import java.awt.event.MouseEvent

inline fun Component.setOnClickListener(crossinline l: (MouseEvent) -> Unit) {
    removeMouseListeners()
    addMouseListener(object : DefaultMouseListener { override fun mouseClicked(e: MouseEvent) = l(e) })
}

fun Component.removeMouseListeners() {
    mouseListeners.forEach(this::removeMouseListener)
}