package com.oliynick.max.elm.time.travel.app.misc

import com.oliynick.max.elm.time.travel.app.DefaultMouseListener
import java.awt.Component
import java.awt.event.MouseEvent

inline fun Component.addOnClickListener(crossinline l: (MouseEvent) -> Unit) {
    addMouseListener(object : DefaultMouseListener { override fun mouseClicked(e: MouseEvent) = l(e) })
}