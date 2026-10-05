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

package io.github.xlopec.reader.app.ui.screens.filters

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableDefaults
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QueryBuilder
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import io.github.xlopec.reader.app.model.Query
import kotlin.math.abs
import kotlin.math.roundToInt

private val RecentSearchSwipeThreshold = 120.dp

internal fun LazyListScope.recentSearchesSection(
    suggestions: List<Query>,
    childTransitionState: ChildTransitionState,
    onSelect: (Query) -> Unit,
    onDelete: (Query) -> Unit,
) {
    item(key = RecentSearchesSubtitle) {
        FiltersSubtitle(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp)
                .alpha(alpha = childTransitionState.contentAlpha),
            text = "Recent searches",
        )
    }

    items(suggestions, Query::value) { item ->
        val dismissState = remember { AnchoredDraggableState(SwipeValue.Settled) }
        val thresholdPx = with(LocalDensity.current) { RecentSearchSwipeThreshold.toPx() }

        LaunchedEffect(dismissState.settledValue) {
            if (dismissState.settledValue == SwipeValue.Dismissed) {
                onDelete(item)
            }
        }

        BoxWithConstraints(
            modifier = Modifier.fillParentMaxWidth()
                .clickable { onSelect(item) }
                .animateItem()
                .alpha(childTransitionState.contentAlpha)
                .offset(y = childTransitionState.listItemOffsetY),
        ) {
            val width = constraints.maxWidth.toFloat()
            // offset is NaN until the anchors below are applied
            val offset = dismissState.offset.takeUnless(Float::isNaN) ?: 0f

            SideEffect {
                dismissState.updateAnchors(
                    DraggableAnchors {
                        SwipeValue.Settled at 0f
                        SwipeValue.Dismissed at -width
                    },
                )
            }

            if (offset < 0f) {
                val dismissed = with(LocalDensity.current) { abs(offset).toDp() }

                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(x = maxWidth - dismissed)
                        .background(Color.Red)
                        .padding(start = 16.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = null,
                    )
                }
            }

            RecentSearchItem(
                modifier = Modifier
                    .offset { IntOffset(offset.roundToInt(), 0) }
                    .anchoredDraggable(
                        state = dismissState,
                        orientation = Orientation.Horizontal,
                        enabled = dismissState.settledValue == SwipeValue.Settled,
                        flingBehavior = AnchoredDraggableDefaults.flingBehavior(
                            state = dismissState,
                            positionalThreshold = { thresholdPx },
                        ),
                    )
                    .padding(
                        horizontal = 16.dp,
                        vertical = 8.dp,
                    ),
                suggestion = item,
            )
        }
    }
}

private enum class SwipeValue { Settled, Dismissed }

@Composable
internal fun RecentSearchItem(
    modifier: Modifier,
    suggestion: Query,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = Icons.Default.QueryBuilder,
            contentDescription = null,
        )

        Text(
            modifier = Modifier.weight(1f),
            text = suggestion.value,
        )
    }
}
