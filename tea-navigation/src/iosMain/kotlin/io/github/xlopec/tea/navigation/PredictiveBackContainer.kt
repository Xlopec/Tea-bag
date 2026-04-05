/*
 * MIT License
 *
 * Copyright (c) 2026. Maksym Oliinyk.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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

/**
 * Container that provides predictive back gesture support for a given [coordinator].
 *
 * @param T entry type
 * @param backDispatcher back dispatcher
 * @param coordinator back coordinator
 * @param modifier modifier to be applied to the container
 * @param backIcon optional back icon to be shown during gesture
 * @param startEdgeEnabled whether gesture from start edge is enabled
 * @param endEdgeEnabled whether gesture from end edge is enabled
 * @param edgeWidth width of the edge where gesture can be started
 * @param activationOffsetThreshold threshold to activate back gesture
 * @param confirmationProgressThreshold threshold to confirm back gesture
 * @param onClose optional callback when container is closed
 * @param content content to be shown for each entry
 */
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
