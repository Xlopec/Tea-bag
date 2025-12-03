package io.github.xlopec.reader.app

import android.content.ClipData
import androidx.compose.ui.platform.ClipEntry
import androidx.core.net.toUri
import io.github.xlopec.tea.data.Url

public actual fun ClipEntry(
    label: String,
    url: Url,
): ClipEntry {
    return ClipEntry(ClipData.newRawUri(label, url.toString().toUri()))
}
