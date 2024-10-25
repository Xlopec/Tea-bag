package io.github.xlopec.tea.time.travel.plugin.util

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.onChild
import androidx.compose.ui.test.onNodeWithText
import io.github.xlopec.tea.time.travel.protocol.ComponentId

internal fun SemanticsNodeInteractionsProvider.onCloseTabNode(id: ComponentId): SemanticsNodeInteraction =
    onNodeWithText(id.value).onChild()
