package com.max.reader.app.ui.screens.suggest

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
import com.oliynick.max.reader.app.feature.article.list.Query

@OptIn(ExperimentalFoundationApi::class)
fun LazyListScope.suggestionsSection(
    suggestions: List<Query>,
    childTransitionState: ChildTransitionState,
    onSuggestionSelected: (Query) -> Unit,
) {
    item {
        SuggestionsSubtitle(
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