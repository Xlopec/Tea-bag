package io.github.xlopec.tea.time.travel.plugin.util

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.MouseClickScope
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.isOutOfBounds
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChangeConsumed
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpOffset
import kotlinx.coroutines.coroutineScope

@OptIn(ExperimentalFoundationApi::class)
internal fun Modifier.clickable(
    onClick: MouseClickScope.(downOffset: DpOffset, upOffset: DpOffset) -> Unit
) = composed {
    val density = LocalDensity.current
    val onClickState = rememberUpdatedState(onClick)

    pointerInput(Unit) {
        detectTapWithContext { down, up ->
            with(density) {
                onClickState.value(
                    MouseClickScope(down.buttons, down.keyboardModifiers),
                    DpOffset(up.changes.last().position),
                    DpOffset(down.changes.last().position)
                )
            }
        }
    }
}

internal suspend fun PointerInputScope.detectTapWithContext(
    onTap: ((PointerEvent, PointerEvent) -> Unit)? = null
) {
    forEachGesture {
        coroutineScope {
            awaitPointerEventScope {

                val down = awaitEventFirstDown().also {
                    it.changes.forEach { it.consume() }
                }

                val up = waitForFirstInboundUpOrCancellation()
                if (up != null) {
                    up.changes.forEach { it.consume() }
                    onTap?.invoke(down, up)
                }
            }
        }
    }
}

private suspend fun AwaitPointerEventScope.awaitEventFirstDown(): PointerEvent {
    var event: PointerEvent
    do {
        event = awaitPointerEvent()
    } while (
        !event.changes.all { it.changedToDown() }
    )
    return event
}

@Suppress("ReturnCount")
private suspend fun AwaitPointerEventScope.waitForFirstInboundUpOrCancellation(): PointerEvent? {
    while (true) {
        val event = awaitPointerEvent(PointerEventPass.Main)
        if (event.changes.all { it.changedToUp() }) {
            // All pointers are up
            return event
        }

        if (event.changes.any {
                it.consumed.downChange || it.isOutOfBounds(size, extendedTouchPadding)
            }
        ) {
            return null // Canceled
        }

        // Check for cancel by position consumption. We can look on the Final pass of the
        // existing pointer event because it comes after the Main pass we checked above.
        val consumeCheck = awaitPointerEvent(PointerEventPass.Final)
        if (consumeCheck.changes.any { it.positionChangeConsumed() }) {
            return null
        }
    }
}

context(Density) private fun DpOffset(offset: Offset): DpOffset = DpOffset(offset.x.toDp(), offset.y.toDp())
