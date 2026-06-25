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

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

public enum class SwipeEdge {
    LEFT,
    RIGHT,
}

/**
 * Detects edge-swipe back gestures and surfaces their lifecycle as callbacks.
 *
 * On every gesture the overlay calls [onStart] with the [SwipeEdge] that the
 * gesture originated from, [onProgress] on every movement with a normalized
 * `[0f, 1f]` progress, then exactly one of [onConfirm] (release past
 * [confirmationProgressThreshold]) or [onCancel] (release before threshold or
 * pointer cancelled).
 *
 * @param onStart invoked when the activation threshold is crossed and a gesture begins
 * @param onProgress invoked with normalized progress while the gesture is in flight
 * @param onConfirm invoked once when the user releases past [confirmationProgressThreshold]
 * @param onCancel invoked once when the user releases before [confirmationProgressThreshold] or the pointer is cancelled
 * @param enabled when `false`, all gestures are ignored
 * @param startEdgeEnabled enables the start edge (left in LTR, right in RTL)
 * @param endEdgeEnabled enables the end edge (right in LTR, left in RTL)
 * @param edgeWidth horizontal distance from each enabled edge in which the first down-touch is recognized
 * @param activationOffsetThreshold distance from the initial touch point along the gesture axis required to start a gesture
 * @param confirmationProgressThreshold progress fraction at release that confirms instead of cancels
 */
@Composable
public fun PredictiveBackGestureOverlay(
    onStart: (edge: SwipeEdge) -> Unit,
    onProgress: (progress: Float) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    startEdgeEnabled: Boolean = true,
    endEdgeEnabled: Boolean = true,
    edgeWidth: Dp = 16.dp,
    activationOffsetThreshold: Dp = 16.dp,
    confirmationProgressThreshold: Float = 0.2F,
    content: @Composable () -> Unit,
) {
    val layoutDirection = LocalLayoutDirection.current
    val currentOnStart by rememberUpdatedState(onStart)
    val currentOnProgress by rememberUpdatedState(onProgress)
    val currentOnConfirm by rememberUpdatedState(onConfirm)
    val currentOnCancel by rememberUpdatedState(onCancel)

    Box(
        modifier = modifier.handleBackGestures(
            enabled = enabled,
            leftEdgeEnabled = when (layoutDirection) {
                LayoutDirection.Ltr -> startEdgeEnabled
                LayoutDirection.Rtl -> endEdgeEnabled
            },
            rightEdgeEnabled = when (layoutDirection) {
                LayoutDirection.Ltr -> endEdgeEnabled
                LayoutDirection.Rtl -> startEdgeEnabled
            },
            edgeWidth = edgeWidth,
            activationOffsetThreshold = activationOffsetThreshold,
            confirmationProgressThreshold = confirmationProgressThreshold,
            onStart = { edge -> currentOnStart(edge) },
            onProgress = { currentOnProgress(it) },
            onConfirm = { currentOnConfirm() },
            onCancel = { currentOnCancel() },
        ),
    ) {
        content()
    }
}

private fun Modifier.handleBackGestures(
    enabled: Boolean,
    leftEdgeEnabled: Boolean,
    rightEdgeEnabled: Boolean,
    edgeWidth: Dp,
    activationOffsetThreshold: Dp,
    confirmationProgressThreshold: Float,
    onStart: (SwipeEdge) -> Unit,
    onProgress: (Float) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
): Modifier =
    pointerInput(enabled, leftEdgeEnabled, rightEdgeEnabled) {
        if (!enabled) return@pointerInput
        awaitEachGesture {
            val down = awaitFirstDown(pass = PointerEventPass.Initial)
            val startPosition = down.position

            val isLeftEdge = leftEdgeEnabled && (startPosition.x < edgeWidth.toPx())
            val isRightEdge = rightEdgeEnabled && (startPosition.x > size.width - edgeWidth.toPx())

            val edge =
                when {
                    isLeftEdge && isRightEdge -> if (startPosition.x < size.width / 2F) SwipeEdge.LEFT else SwipeEdge.RIGHT
                    isLeftEdge -> SwipeEdge.LEFT
                    isRightEdge -> SwipeEdge.RIGHT
                    else -> return@awaitEachGesture
                }

            val handler =
                BackGestureHandler(
                    pointerId = down.id,
                    startPosition = startPosition,
                    size = size,
                    edge = edge,
                    ignoreOffsetThreshold = 16.dp.toPx(),
                    activationOffsetThreshold = activationOffsetThreshold.toPx(),
                    progressConfirmationThreshold = confirmationProgressThreshold,
                    onStart = onStart,
                    onProgress = onProgress,
                    onConfirm = onConfirm,
                    onCancel = onCancel,
                )

            with(handler) { handleGesture() }
        }
    }

private class BackGestureHandler(
    private val pointerId: PointerId,
    private val startPosition: Offset,
    private val size: IntSize,
    private val edge: SwipeEdge,
    private val ignoreOffsetThreshold: Float,
    private val activationOffsetThreshold: Float,
    private val progressConfirmationThreshold: Float,
    private val onStart: (SwipeEdge) -> Unit,
    private val onProgress: (Float) -> Unit,
    private val onConfirm: () -> Unit,
    private val onCancel: () -> Unit,
) {

    private var changesIterator: Iterator<PointerInputChange>? = null
    private var resolved = false

    private suspend fun AwaitPointerEventScope.awaitChange(): PointerInputChange {
        while (true) {
            var iterator = changesIterator

            while ((iterator == null) || !iterator.hasNext()) {
                iterator = awaitPointerEvent(pass = PointerEventPass.Initial).changes.iterator()
                changesIterator = iterator
            }

            iterator.next().takeIf { it.id == pointerId }?.also {
                return it
            }
        }
    }

    suspend fun AwaitPointerEventScope.handleGesture() {
        if (!awaitStart()) return
        try {
            processGesture()
        } finally {
            // If the pointerInput modifier restarts mid-gesture (enabled flipped,
            // layout direction changed, parent disposed, …), the suspending block
            // is cancelled before processGesture reaches its release branch.
            // Synthesize a cancel so the caller can tear down gesture state.
            if (!resolved) onCancel()
        }
    }

    private suspend fun AwaitPointerEventScope.awaitStartChange(): PointerInputChange? {
        while (true) {
            val change = awaitChange()
            val position = change.position

            if (!change.pressed ||
                (position.y < startPosition.y - ignoreOffsetThreshold) ||
                (position.y > startPosition.y + ignoreOffsetThreshold)
            ) {
                return null
            }

            when (edge) {
                SwipeEdge.LEFT ->
                    when {
                        position.x < startPosition.x - ignoreOffsetThreshold -> return null
                        position.x > startPosition.x + activationOffsetThreshold -> return change
                    }

                SwipeEdge.RIGHT ->
                    when {
                        position.x > startPosition.x + ignoreOffsetThreshold -> return null
                        position.x < startPosition.x - activationOffsetThreshold -> return change
                    }
            }
        }
    }

    private suspend fun AwaitPointerEventScope.awaitStart(): Boolean {
        val change = awaitStartChange() ?: return false
        change.consume()
        onStart(edge)
        onProgress(getProgress(position = change.position))
        return true
    }

    private suspend fun AwaitPointerEventScope.processGesture() {
        while (true) {
            val change = awaitChange()
            val position = change.position
            change.consume()

            val progress = getProgress(position = position)
            onProgress(progress)

            if (!change.pressed) {
                resolved = true
                if (progress > progressConfirmationThreshold) onConfirm() else onCancel()
                return
            }
        }
    }

    private fun getProgress(position: Offset): Float =
        when (edge) {
            SwipeEdge.LEFT -> {
                val startX = startPosition.x + activationOffsetThreshold
                (position.x - startX) / (size.width - startX)
            }

            SwipeEdge.RIGHT -> {
                val startX = startPosition.x - activationOffsetThreshold
                (startX - position.x) / startX
            }
        }.coerceIn(0F, 1F)
}
