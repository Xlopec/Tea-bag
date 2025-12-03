package io.github.xlopec.reader.app.ui.misc

import androidx.compose.runtime.Composable
import io.github.xlopec.reader.app.ui.theme.ThemedPreview
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@Composable
internal fun RowMessagePreview() {
    ThemedPreview {
        RowMessage(
            message = "No articles",
            onClick = {},
        )
    }
}

@Preview
@Composable
internal fun ColumnMessagePreview() {
    ThemedPreview {
        ColumnMessage(
            title = "Title",
            message = "No articles",
            onClick = {},
        )
    }
}
