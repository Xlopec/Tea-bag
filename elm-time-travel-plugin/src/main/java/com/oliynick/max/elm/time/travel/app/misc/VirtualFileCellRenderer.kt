package com.oliynick.max.elm.time.travel.app.misc

import com.intellij.openapi.vfs.VirtualFile
import java.awt.Component
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.ListCellRenderer

class VirtualFileCellRenderer : JLabel(), ListCellRenderer<VirtualFile> {

    init {
        isOpaque = true
    }

    override fun getListCellRendererComponent(list: JList<out VirtualFile>, country: VirtualFile, index: Int,
                                              isSelected: Boolean, cellHasFocus: Boolean): Component {

        text = country.path

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