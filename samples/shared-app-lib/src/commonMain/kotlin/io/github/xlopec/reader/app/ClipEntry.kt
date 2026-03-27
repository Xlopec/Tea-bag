package io.github.xlopec.reader.app

import androidx.compose.ui.platform.ClipEntry
import io.github.xlopec.reader.app.model.Url

public expect fun ClipEntry(
    label: String,
    url: Url,
): ClipEntry
