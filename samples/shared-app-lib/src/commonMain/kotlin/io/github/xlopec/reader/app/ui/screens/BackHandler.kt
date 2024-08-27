package io.github.xlopec.reader.app.ui.screens

import androidx.compose.runtime.Composable

@Composable
internal expect fun BackHandler(
    onBack: () -> Unit
)
