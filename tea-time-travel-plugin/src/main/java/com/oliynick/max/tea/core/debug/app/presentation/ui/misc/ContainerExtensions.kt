package com.oliynick.max.tea.core.debug.app.presentation.ui.misc

import java.awt.Component
import java.awt.Container

operator fun Container.plusAssign(component: Component) {
    add(component)
}

inline val Container.isEmpty: Boolean
    inline get() = componentCount == 0

inline val Container.isNotEmpty: Boolean
    inline get() = !isEmpty

operator fun Container.get(
    i: Int
): Component = getComponent(i)

inline val Container.children: List<Component>
    get() = (0 until componentCount).map { i -> this[i] }
