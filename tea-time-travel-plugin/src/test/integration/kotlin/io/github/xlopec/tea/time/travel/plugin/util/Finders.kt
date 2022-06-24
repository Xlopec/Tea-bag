package io.github.xlopec.tea.time.travel.plugin.util

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.onChildAt
import androidx.compose.ui.test.onNodeWithTag
import io.github.xlopec.tea.time.travel.plugin.feature.component.ui.ComponentTabTag
import io.github.xlopec.tea.time.travel.protocol.ComponentId

internal fun SemanticsNodeInteractionsProvider.onCloseTabActionNode(id: ComponentId): SemanticsNodeInteraction =
    onNodeWithTag(ComponentTabTag(id), true).onChildAt(1)
