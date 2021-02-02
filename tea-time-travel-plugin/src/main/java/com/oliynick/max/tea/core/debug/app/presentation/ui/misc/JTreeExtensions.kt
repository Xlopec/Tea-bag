package com.oliynick.max.tea.core.debug.app.presentation.ui.misc

import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode

fun JTree.getSubTreeForRow(row: Int): RenderTree {
    return (getPathForRow(row).lastPathComponent as DefaultMutableTreeNode).userObject as RenderTree
}
