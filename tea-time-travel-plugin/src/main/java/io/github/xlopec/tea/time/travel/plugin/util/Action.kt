@file:Suppress("FunctionName")

package io.github.xlopec.tea.time.travel.plugin.util

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

fun Action(
    text: String,
    onAction: (AnActionEvent) -> Unit
): AnAction = object : AnAction(text) {
    override fun actionPerformed(p0: AnActionEvent) = onAction(p0)
}
