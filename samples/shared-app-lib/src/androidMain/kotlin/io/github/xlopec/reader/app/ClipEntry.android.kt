package io.github.xlopec.reader.app

import android.content.ClipData
import androidx.compose.ui.platform.ClipEntry
import androidx.core.net.toUri
import io.github.xlopec.reader.app.model.Url
import io.github.xlopec.reader.app.model.toExternalValue

public actual fun ClipEntry(
    label: String,
    url: Url,
): ClipEntry {
    return ClipEntry(ClipData.newRawUri(label, url.toExternalValue().toUri()))
}
