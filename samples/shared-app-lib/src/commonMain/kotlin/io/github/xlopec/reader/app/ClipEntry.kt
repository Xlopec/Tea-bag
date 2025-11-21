package io.github.xlopec.reader.app

import androidx.compose.ui.platform.ClipEntry
import io.github.xlopec.tea.data.Url

public expect fun ClipEntry(
    label: String,
    url: Url,
): ClipEntry