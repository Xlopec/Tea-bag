package com.oliynick.max.tea.core.debug.app.presentation.ui.tabs

import com.intellij.icons.AllIcons
import com.intellij.ide.IdeBundle
import com.intellij.ide.ui.UISettings
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.keymap.KeymapUtil
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.ui.ShadowAction
import com.intellij.util.ObjectUtils
import javax.swing.JComponent

@Suppress("ComponentNotRegistered")
class CloseableTab(
    c: JComponent,
    private val onClick: () -> Unit,
) : AnAction(), DumbAware {

    init {
        ShadowAction(this, ActionManager.getInstance().getAction(IdeActions.ACTION_CLOSE), c) { }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.icon = AllIcons.Actions.Close
        e.presentation.hoveredIcon = AllIcons.Actions.CloseHovered
        e.presentation.isVisible = UISettings.instance.showCloseButton || false
        shortcutSet =
            ObjectUtils.notNull(KeymapUtil.getActiveKeymapShortcuts(IdeActions.ACTION_CLOSE),
                CustomShortcutSet.EMPTY)
        e.presentation.setText(IdeBundle.messagePointer("action.presentation.EditorTabbedContainer.text"))
    }

    override fun actionPerformed(e: AnActionEvent) = onClick()
}
