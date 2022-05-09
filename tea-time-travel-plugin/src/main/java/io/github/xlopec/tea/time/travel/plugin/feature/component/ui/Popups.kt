package io.github.xlopec.tea.time.travel.plugin.feature.component.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.PsiNavigateUtil
import io.github.xlopec.tea.time.travel.plugin.feature.component.integration.ApplyMessage
import io.github.xlopec.tea.time.travel.plugin.feature.component.integration.ApplyState
import io.github.xlopec.tea.time.travel.plugin.feature.component.integration.RemoveAllSnapshots
import io.github.xlopec.tea.time.travel.plugin.feature.component.integration.RemoveSnapshots
import io.github.xlopec.tea.time.travel.plugin.model.BooleanWrapper
import io.github.xlopec.tea.time.travel.plugin.model.CharWrapper
import io.github.xlopec.tea.time.travel.plugin.model.CollectionWrapper
import io.github.xlopec.tea.time.travel.plugin.model.Null
import io.github.xlopec.tea.time.travel.plugin.model.NumberWrapper
import io.github.xlopec.tea.time.travel.plugin.model.Ref
import io.github.xlopec.tea.time.travel.plugin.model.SnapshotId
import io.github.xlopec.tea.time.travel.plugin.model.StringWrapper
import io.github.xlopec.tea.time.travel.plugin.model.Type
import io.github.xlopec.tea.time.travel.plugin.model.Value
import io.github.xlopec.tea.time.travel.plugin.ui.theme.ActionIcons
import io.github.xlopec.tea.time.travel.plugin.ui.theme.ValueIcon
import io.github.xlopec.tea.time.travel.plugin.util.javaPsiFacade
import io.github.xlopec.tea.time.travel.protocol.ComponentId
import io.kanro.compose.jetbrains.control.DropdownMenuItem
import io.kanro.compose.jetbrains.control.Text

@Composable
fun ValuePopup(
    value: Value,
    formatter: TreeFormatter,
    project: Project,
) {
    when (value) {
        is CollectionWrapper -> CopyActionItem(AnnotatedString(formatter(value)))
        is Ref -> RefActionItems(project, value.type)
        Null, is BooleanWrapper, is CharWrapper, is NumberWrapper, is StringWrapper -> LeafActionItems(value)
    }
}

@Composable
fun LeafActionItems(
    value: Value
) {

    val clipboardValue = value.clipboardValue

    if (clipboardValue != null) {
        Column {
            CopyActionItem(AnnotatedString(clipboardValue))
        }
    }
}

@Composable
fun CopyActionItem(
    clipboard: AnnotatedString
) {
    val clipboardManager = LocalClipboardManager.current

    PopupItem(ActionIcons.Copy, "Copy value") {
        clipboardManager.setText(clipboard)
    }
}

@Composable
fun RefActionItems(
    project: Project,
    type: Type
) {
    val facade = project.javaPsiFacade

    val psiClass = facade.findClass(type.name, GlobalSearchScope.projectScope(project)) ?: return

    Column {
        CopyActionItem(AnnotatedString(type.name))
        JumpToSourcesActionItem(psiClass)
    }
}

@Composable
fun JumpToSourcesActionItem(
    psiClass: PsiClass
) {
    PopupItem(ValueIcon.Class, "Jump to sources") {
        PsiNavigateUtil.navigate(psiClass)
    }
}

@Composable
fun SnapshotActionItems(
    componentId: ComponentId,
    snapshotId: SnapshotId,
    handler: MessageHandler
) {
    Column {
        PopupItem(ActionIcons.Remove, "Delete all") {
            handler(RemoveAllSnapshots(componentId))
        }
        PopupItem(ActionIcons.Remove, "Delete") {
            handler(RemoveSnapshots(componentId, snapshotId))
        }
        PopupItem(ActionIcons.UpdateRunningApplication, "Apply state") {
            handler(ApplyState(componentId, snapshotId))
        }
        PopupItem(ActionIcons.UpdateRunningApplication, "Apply message") {
            handler(ApplyMessage(componentId, snapshotId))
        }
    }
}

@Composable
fun PopupItem(
    painter: Painter,
    text: String,
    onClick: () -> Unit,
) {
    DropdownMenuItem(onClick = onClick) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.Start)
        ) {
            Image(
                modifier = Modifier.size(16.dp),
                painter = painter,
                contentDescription = text
            )

            Text(text = text)
        }
    }
}

private val Value.clipboardValue: String?
    get() = when (this) {
        is BooleanWrapper -> value.toString()
        is CharWrapper -> value.toString()
        is CollectionWrapper -> null
        Null -> null.toString()
        is NumberWrapper -> value.toString()
        is Ref -> type.name
        is StringWrapper -> value
    }
