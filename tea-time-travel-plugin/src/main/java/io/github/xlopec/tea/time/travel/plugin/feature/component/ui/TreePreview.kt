package io.github.xlopec.tea.time.travel.plugin.feature.component.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import io.github.xlopec.tea.time.travel.plugin.model.*
import io.github.xlopec.tea.time.travel.plugin.ui.theme.PluginPreviewTheme

private val PreviewTreeRoot = Ref(
    Type.of("io.github.xlopec.Developer"),
    Property("name", StringWrapper("Max")),
    Property("surname", StringWrapper("Oliynick")),
    Property(
        "interests",
        CollectionWrapper(
            listOf(
                StringWrapper("Jetpack Compose"),
                StringWrapper("Programming"),
                StringWrapper("FP")
            )
        )
    ),
    Property("emptyCollection", CollectionWrapper())
)

@Preview
@Composable
private fun ValueTreePreviewExpandedShort() {
    PluginPreviewTheme {
        CompositionLocalProvider(
            LocalInitialExpandState provides true
        ) {
            Tree(
                root = PreviewTreeRoot,
                formatter = ::toReadableStringShort,
                valuePopupContent = {}
            )
        }
    }
}

@Preview
@Composable
private fun ValueTreePreviewExpandedLong() {
    PluginPreviewTheme {
        CompositionLocalProvider(
            LocalInitialExpandState provides true
        ) {
            Tree(
                root = PreviewTreeRoot,
                formatter = ::toReadableStringLong,
                valuePopupContent = {}
            )
        }
    }
}
