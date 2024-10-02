package io.github.xlopec.tea.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.PredictiveBackGestureOverlay
import com.arkivanov.essenty.backhandler.BackDispatcher
import com.arkivanov.essenty.backhandler.BackEvent

@Composable
public fun <T : NavStackEntry<*>> PredictiveBackContainer(
    backDispatcher: BackDispatcher,
    coordinator: BackCoordinator<T>,
    modifier: Modifier = Modifier,
    backIcon: (@Composable (progress: Float, edge: BackEvent.SwipeEdge) -> Unit)? = null,
    startEdgeEnabled: Boolean = true,
    endEdgeEnabled: Boolean = true,
    edgeWidth: Dp = 16.dp,
    activationOffsetThreshold: Dp = 16.dp,
    confirmationProgressThreshold: Float = 0.2F,
    onClose: (() -> Unit)? = null,
    content: @Composable (Modifier, T) -> Unit,
) {
    PredictiveBackGestureOverlay(
        modifier = modifier,
        backDispatcher = backDispatcher,
        backIcon = backIcon,
        startEdgeEnabled = startEdgeEnabled,
        endEdgeEnabled = endEdgeEnabled,
        edgeWidth = edgeWidth,
        activationOffsetThreshold = activationOffsetThreshold,
        confirmationProgressThreshold = confirmationProgressThreshold,
        onClose = onClose,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            val movableContent = remember(content) { movableContentOf(content) }
            val previous = coordinator.previous

            if (previous != null) {
                key(previous.id) {
                    movableContent(coordinator.previousModifier, previous)
                }
            }

            val current = coordinator.current

            key(current.id) {
                movableContent(coordinator.currentModifier, current)
            }
        }
    }
}
