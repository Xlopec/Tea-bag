package io.github.xlopec.tea.time.travel.plugin.feature.component.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.intellij.psi.PsiClass
import io.github.xlopec.tea.time.travel.plugin.feature.component.integration.ApplyMessage
import io.github.xlopec.tea.time.travel.plugin.feature.component.integration.ApplyState
import io.github.xlopec.tea.time.travel.plugin.feature.component.integration.RemoveAllSnapshots
import io.github.xlopec.tea.time.travel.plugin.feature.component.integration.RemoveSnapshots
import io.github.xlopec.tea.time.travel.plugin.model.*
import io.github.xlopec.tea.time.travel.plugin.ui.LocalPlatform
import io.github.xlopec.tea.time.travel.plugin.ui.theme.ActionIcons
import io.github.xlopec.tea.time.travel.plugin.ui.theme.ValueIcon
import io.github.xlopec.tea.time.travel.protocol.ComponentId
import io.kanro.compose.jetbrains.control.DropdownMenuItem
import io.kanro.compose.jetbrains.control.Text
import kotlinx.coroutines.launch

@Composable
fun ValuePopup(
    value: Value,
    formatter: TreeFormatter,
) {
    when (value) {
        is CollectionWrapper -> CopyActionItem(AnnotatedString(formatter(value)))
        is Ref -> RefActionItems(value.type)
        Null, is BooleanWrapper, is CharWrapper, is NumberWrapper, is StringWrapper -> LeafActionItems(value)
    }
}

@Composable
fun LeafActionItems(
    value: Value,
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
    clipboard: AnnotatedString,
) {
    val clipboardManager = LocalClipboardManager.current

    PopupItem(ActionIcons.Copy, "Copy value") {
        clipboardManager.setText(clipboard)
    }
}

@Composable
fun RefActionItems(
    type: Type,
) {
    Column {
        CopyActionItem(AnnotatedString(type.name))
        val platform = LocalPlatform.current
        val psiClass = remember { mutableStateOf<PsiClass?>(null) }

        LaunchedEffect(Unit) {
            psiClass.value = platform.psiClassFor(type)
        }

        val currentPsiClass = psiClass.value

        if (currentPsiClass != null) {
            JumpToSourcesActionItem(currentPsiClass)
        }
    }
}

@Composable
fun JumpToSourcesActionItem(
    psiClass: PsiClass,
) {
    val platform = LocalPlatform.current
    val scope = rememberCoroutineScope()
    PopupItem(ValueIcon.Class, "Jump to sources") {
        scope.launch {
            platform.navigateToSources(psiClass)
        }
    }
}

@Composable
fun SnapshotActionItems(
    componentId: ComponentId,
    snapshotId: SnapshotId,
    serverStarted: Boolean,
    handler: MessageHandler,
) {
    Column {
        PopupItem(
            painter = ActionIcons.Remove,
            text = "Delete all"
        ) {
            handler(RemoveAllSnapshots(componentId))
        }
        PopupItem(
            painter = ActionIcons.Remove,
            text = "Delete"
        ) {
            handler(RemoveSnapshots(componentId, snapshotId))
        }
        if (serverStarted) {
            PopupItem(
                painter = ActionIcons.UpdateRunningApplication,
                text = "Deploy state to all connected clients",
            ) {
                handler(ApplyState(componentId, snapshotId))
            }
            PopupItem(
                painter = ActionIcons.UpdateRunningApplication,
                text = "Deploy message to all connected clients",
            ) {
                handler(ApplyMessage(componentId, snapshotId))
            }
        }
    }
}

@Composable
fun PopupItem(
    painter: Painter,
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        onClick = onClick,
        enabled = enabled
    ) {
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
