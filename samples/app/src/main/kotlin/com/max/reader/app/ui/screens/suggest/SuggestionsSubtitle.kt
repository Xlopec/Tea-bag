package com.max.reader.app.ui.screens.suggest

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign

@Composable
fun SuggestionsSubtitle(
    modifier: Modifier,
    text: String,
) {
    Text(
        text = text,
        modifier = modifier,
        textAlign = TextAlign.Start,
        style = MaterialTheme.typography.subtitle1
    )
}