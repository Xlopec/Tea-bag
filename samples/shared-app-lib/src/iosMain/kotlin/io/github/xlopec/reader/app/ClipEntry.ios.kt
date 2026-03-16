package io.github.xlopec.reader.app

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ClipEntry
import io.github.xlopec.tea.data.Url

@OptIn(ExperimentalComposeUiApi::class)
public actual fun ClipEntry(
    label: String,
    url: Url,
): ClipEntry {
    return ClipEntry.withPlainText(url.toString())
}
