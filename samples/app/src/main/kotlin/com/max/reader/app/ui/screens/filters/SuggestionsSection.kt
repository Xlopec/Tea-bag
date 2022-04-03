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

package com.max.reader.app.ui.screens.filters

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QueryBuilder
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.oliynick.max.reader.app.domain.Query

@OptIn(ExperimentalFoundationApi::class)
fun LazyListScope.suggestionsSection(
    suggestions: List<Query>,
    childTransitionState: ChildTransitionState,
    onSuggestionSelected: (Query) -> Unit,
) {
    item {
        FiltersSubtitle(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp)
                .alpha(alpha = childTransitionState.contentAlpha),
            text = "Recent searches"
        )
    }

    items(suggestions, Query::value) { item ->
        SuggestionItem(
            modifier = Modifier
                .fillParentMaxWidth()
                .clickable { onSuggestionSelected(item) }
                .animateItemPlacement()
                .alpha(childTransitionState.contentAlpha)
                .padding(all = 16.dp)
                .offset(y = childTransitionState.listItemOffsetY),
            suggestion = item
        )
    }
}

@Composable
private fun SuggestionItem(
    modifier: Modifier,
    suggestion: Query,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(imageVector = Icons.Default.QueryBuilder, contentDescription = null)

        Spacer(modifier = Modifier.width(8.dp))

        Text(text = suggestion.value)
    }
}
