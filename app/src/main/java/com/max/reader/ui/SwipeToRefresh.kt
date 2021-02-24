/*
 * Copyright (C) 2021. Maksym Oliinyk.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.max.reader.ui

import androidx.annotation.FloatRange
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp

private val RefreshDistance = 80.dp

@Composable
fun RefreshIndicator(
    isRefreshing: Boolean,
    @FloatRange(from = .0, to = 1.0) progress: Float,
) {
    require(progress in .0..1.0) { "Value was out of range ${.0..1.0}, value: $progress" }
    Surface(elevation = 10.dp, shape = CircleShape) {

        val modifier = Modifier
            .size(36.dp)
            .padding(8.dp)

        val color = MaterialTheme.colors.onSurface

        if (isRefreshing) {
            CircularProgressIndicator(
                modifier = modifier,
                color = color
            )
        } else {
            CircularProgressIndicator(
                progress = progress,
                modifier = modifier,
                color = color
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwipeToRefreshLayout(
    refreshingState: Boolean,
    onRefresh: () -> Unit,
    enabled: Boolean = true,
    refreshIndicator: @Composable (Boolean, Float) -> Unit = { isRefreshing, progress ->
        RefreshIndicator(isRefreshing, progress)
    },
    content: @Composable () -> Unit,
) {
    val refreshDistance = with(LocalDensity.current) { RefreshDistance.toPx() }
    val state = rememberSwipeableState(refreshingState) { newValue ->
        // compare both copies of the swipe state before calling onRefresh(). This is a workaround.
        if (newValue && !refreshingState) onRefresh()
        true
    }

    Box(
        modifier = Modifier
            .nestedScroll(state.PreUpPostDownNestedScrollConnection)
            .swipeable(
            enabled = enabled,
            state = state,
            anchors = mapOf(
                -refreshDistance to false,
                refreshDistance to true
            ),
            thresholds = { _, _ -> FractionalThreshold(0.5f) },
            orientation = Orientation.Vertical
        )
    ) {
        content()
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = state.offset.value
                    .takeUnless(Float::isNaN)
                    ?.let { with(LocalDensity.current) { it.toDp() } } ?: 0.dp)
        ) {
            if (!state.offset.value.isNaN() && state.offset.value != -refreshDistance) {
                refreshIndicator(refreshingState,
                    (state.offset.value / refreshDistance).coerceIn(0f, 1f))
            }
        }

        // TODO (https://issuetracker.google.com/issues/164113834): This state->event trampoline is a
        //  workaround for a bug in the SwipableState API. Currently, state.value is a duplicated
        //  source of truth of refreshingState.
        LaunchedEffect(refreshingState) { state.animateTo(refreshingState) }
    }
}

/**
 * Temporary workaround for nested scrolling behavior. There is no default implementation for
 * pull to refresh yet, this nested scroll connection mimics the behavior.
 */
@ExperimentalMaterialApi
private val <T> SwipeableState<T>.PreUpPostDownNestedScrollConnection: NestedScrollConnection
    get() = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            val delta = available.toFloat()
            return if (delta < 0 && source == NestedScrollSource.Drag) {
                performDrag(delta).toOffset()
            } else {
                Offset.Zero
            }
        }

        override fun onPostScroll(
            consumed: Offset,
            available: Offset,
            source: NestedScrollSource
        ): Offset {
            return if (source == NestedScrollSource.Drag) {
                performDrag(available.toFloat()).toOffset()
            } else {
                Offset.Zero
            }
        }

        override suspend fun onPreFling(available: Velocity): Velocity {
            val toFling = Offset(available.x, available.y).toFloat()
            return if (toFling < 0) {
                performFling(velocity = toFling)
                // since we go to the anchor with tween settling, consume all for the best UX
                available
            } else {
                Velocity.Zero
            }
        }

        override suspend fun onPostFling(
            consumed: Velocity,
            available: Velocity
        ): Velocity {
            performFling(velocity = Offset(available.x, available.y).toFloat())
            return Velocity.Zero
        }

        private fun Float.toOffset(): Offset = Offset(0f, this)

        private fun Offset.toFloat(): Float = this.y
    }
