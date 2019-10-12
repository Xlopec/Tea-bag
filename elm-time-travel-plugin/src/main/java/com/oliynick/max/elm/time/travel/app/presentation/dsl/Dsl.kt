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