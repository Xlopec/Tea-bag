package io.github.xlopec.reader.app.ui.misc

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.github.xlopec.reader.app.ui.theme.ThemedPreview

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
