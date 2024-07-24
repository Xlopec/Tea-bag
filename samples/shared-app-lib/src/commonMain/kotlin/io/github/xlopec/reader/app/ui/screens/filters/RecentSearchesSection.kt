/*
 * MIT License
 *
 * Copyright (c) 2022. Maksym Oliinyk.
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

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.DismissDirection
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FixedThreshold
import androidx.compose.material.Icon
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QueryBuilder
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import io.github.xlopec.reader.app.model.Query
import kotlin.math.abs

private val RecentSearchSwipeThreshold = 120.dp

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
fun LazyListScope.recentSearchesSection(
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
            text = "Recent searches"
        )
    }

    items(suggestions, Query::value) { item ->
        val dismissState = rememberDismissState()

        LaunchedEffect(dismissState.isDismissed(DismissDirection.EndToStart)) {
            if (dismissState.isDismissed(DismissDirection.EndToStart)) {
                onDelete(item)
            }
        }

        SwipeToDismiss(
            modifier = Modifier.fillParentMaxWidth()
                .clickable { onSelect(item) }
                .animateItemPlacement()
                .alpha(childTransitionState.contentAlpha)
                .offset(y = childTransitionState.listItemOffsetY),
            state = dismissState,
            dismissThresholds = { FixedThreshold(RecentSearchSwipeThreshold) },
            directions = setOf(DismissDirection.EndToStart),
            background = {
                if (dismissState.dismissDirection == DismissDirection.EndToStart) {
                    BoxWithConstraints {
                        val dismissed = with(LocalDensity.current) { abs(dismissState.offset.value).toDp() }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .offset(x = maxWidth - dismissed)
                                .background(Color.Red)
                                .padding(start = 16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = null
                            )
                        }
                    }
                }
            }
        ) {
            RecentSearchItem(
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 8.dp
                ),
                suggestion = item
            )
        }
    }
}

@Composable
fun RecentSearchItem(
    modifier: Modifier,
    suggestion: Query,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.QueryBuilder,
            contentDescription = null
        )

        Text(
            modifier = Modifier.weight(1f),
            text = suggestion.value
        )
    }
}
