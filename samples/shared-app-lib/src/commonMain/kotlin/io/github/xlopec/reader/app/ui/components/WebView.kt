package io.github.xlopec.reader.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import io.github.xlopec.tea.data.Url

@Stable
internal expect class WebViewState {
    val title: MutableState<String?>

    val canGoBack: MutableState<Boolean>

    val loadProgress: MutableIntState

    val isLoading: MutableState<Boolean>

    val isReloading: MutableState<Boolean>

    fun reload()

    fun navigateBack()
}

@Composable
internal expect fun WebView(
    modifier: Modifier = Modifier,
    url: Url,
)

@Composable
internal expect fun rememberWebViewState(
    url: Url,
    javascriptEnabled: Boolean = true,
    zoomSupportEnabled: Boolean = true,
): WebViewState