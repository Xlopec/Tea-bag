package com.max.reader.app.ui.screens.suggest

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.max.reader.app.ui.theme.ThemedPreview
import com.oliynick.max.reader.app.feature.article.list.Query
import com.oliynick.max.reader.app.feature.article.list.QueryType
import com.oliynick.max.reader.app.feature.suggest.SuggestState
import java.util.*

@Preview
@Composable
fun SuggestPreview() {
    ThemedPreview {
        SuggestScreen(
            state = SuggestState(UUID.randomUUID(), Query("Android", QueryType.Regular)),
            onMessage = {})
    }
}