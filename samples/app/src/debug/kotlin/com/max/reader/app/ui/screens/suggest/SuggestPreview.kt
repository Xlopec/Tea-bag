package com.max.reader.app.ui.screens.suggest

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.max.reader.app.ui.theme.ThemedPreview
import com.oliynick.max.reader.app.feature.article.list.Filter
import com.oliynick.max.reader.app.feature.article.list.FilterType
import com.oliynick.max.reader.app.feature.article.list.Query
import com.oliynick.max.reader.app.feature.suggest.SuggestState
import com.oliynick.max.reader.app.misc.Loadable
import java.util.*

private val PreviewState = SuggestState(
    UUID.randomUUID(),
    Filter(FilterType.Regular, Query.of("Android")),
    Loadable.newLoading()
)

@Preview("Suggestions preview dark mode")
@Composable
fun SuggestPreviewDark() {
    ThemedPreview {
        SuggestScreen(
            state = PreviewState,
            handler = {}
        )
    }
}

@Preview("Suggestions preview dark mode")
@Composable
fun SuggestPreviewLight() {
    ThemedPreview(isDarkModeEnabled = false) {
        SuggestScreen(
            state = PreviewState,
            handler = {}
        )
    }
}