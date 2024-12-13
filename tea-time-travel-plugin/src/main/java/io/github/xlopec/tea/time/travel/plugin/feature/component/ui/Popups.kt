package io.github.xlopec.tea.time.travel.plugin.feature.component.ui

import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.text.AnnotatedString
import io.github.xlopec.tea.time.travel.plugin.feature.component.integration.ApplyMessage
import io.github.xlopec.tea.time.travel.plugin.feature.component.integration.RemoveAllSnapshots
import io.github.xlopec.tea.time.travel.plugin.feature.component.integration.RemoveSnapshots
import io.github.xlopec.tea.time.travel.plugin.integration.Platform
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
import io.github.xlopec.tea.time.travel.protocol.ComponentId
import org.jetbrains.jewel.ui.component.MenuScope
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.icons.AllIconsKeys

data class PopupDependencies(
    val formatter: TreeFormatter,
    val clipboardManager: ClipboardManager,
    val platform: Platform,
)

fun MenuScope.valuePopup(
    value: Value,
    dependencies: PopupDependencies,
) {
    when (value) {
        is CollectionWrapper -> copyActionItem(AnnotatedString(dependencies.formatter(value)), dependencies)
        is Ref -> refActionItems(value.type, dependencies)
        Null, is BooleanWrapper, is CharWrapper, is NumberWrapper, is StringWrapper -> leafActionItems(value, dependencies)
    }
}

fun MenuScope.snapshotActionItems(
    componentId: ComponentId,
    snapshotId: SnapshotId,
    serverStarted: Boolean,
    handler: MessageHandler,
) {
    selectableItem(
        selected = false,
        iconKey = AllIconsKeys.General.Remove,
        onClick = { handler(RemoveAllSnapshots(componentId)) }
    ) {
        Text(text = "Delete all")
    }

    selectableItem(
        selected = false,
        iconKey = AllIconsKeys.General.Remove,
        onClick = { handler(RemoveSnapshots(componentId, snapshotId)) }
    ) {
        Text(text = "Delete")
    }

    if (serverStarted) {
        selectableItem(
            selected = false,
            iconKey = null,
            onClick = { handler(ApplyMessage(componentId, snapshotId)) },
        ) {
            Text(text = "Deploy message to all connected clients")
        }
    }
}

private fun MenuScope.leafActionItems(
    value: Value,
    dependencies: PopupDependencies,
) {
    val clipboardValue = value.clipboardValue

    if (clipboardValue != null) {
        copyActionItem(AnnotatedString(clipboardValue), dependencies)
    }
}

private fun MenuScope.copyActionItem(
    clipboard: AnnotatedString,
    dependencies: PopupDependencies,
) {
    selectableItem(
        selected = false,
        iconKey = AllIconsKeys.General.Copy,
        onClick = { dependencies.clipboardManager.setText(clipboard) },
    ) {
        Text("Copy value")
    }
}

private fun MenuScope.refActionItems(
    type: Type,
    dependencies: PopupDependencies,
) {
    copyActionItem(AnnotatedString(type.name), dependencies)

    val psiClass = dependencies.platform.psiClassFor(type)

    if (psiClass != null) {
        selectableItem(
            selected = false,
            iconKey = AllIconsKeys.Nodes.Class,
            onClick = {
                dependencies.platform.navigateToSources(psiClass)
            }
        ) {
            Text("Jump to sources")
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
