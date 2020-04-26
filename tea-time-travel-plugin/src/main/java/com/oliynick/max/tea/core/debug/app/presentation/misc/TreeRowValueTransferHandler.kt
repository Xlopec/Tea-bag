package com.oliynick.max.tea.core.debug.app.presentation.misc

import java.awt.datatransfer.StringSelection
import java.awt.datatransfer.Transferable
import javax.swing.JComponent
import javax.swing.JTree
import javax.swing.TransferHandler

class TreeRowValueTransferHandler (
    var formatter: ValueFormatter
) : TransferHandler() {
    override fun getSourceActions(c: JComponent?): Int = COPY
    override fun createTransferable(c: JComponent): Transferable? {

        val tree = c as JTree

        return tree.selectionRows
            ?.map(tree::getSubTreeForRow)
            ?.joinToString { r -> r.toReadableString(tree.model, formatter) }
            ?.let(::StringSelection)
    }
}
