package com.max.reader.app.ui.screens.suggest

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.max.reader.app.ui.theme.ThemedPreview
import com.oliynick.max.reader.app.feature.suggest.SuggestState

@Preview
@Composable
fun SuggestPreview() {
    ThemedPreview {
        SuggestScreen(state = SuggestState(), onMessage = {})
    }
}