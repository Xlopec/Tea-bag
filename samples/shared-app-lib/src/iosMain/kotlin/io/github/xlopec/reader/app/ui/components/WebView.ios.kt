package io.github.xlopec.reader.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import io.github.xlopec.tea.data.Url

@Composable
internal actual fun WebView(modifier: Modifier, url: Url) {
}

@Stable
internal actual class WebViewState {
    actual val title: MutableState<String?>
        get() = TODO("Not yet implemented")
    actual val canGoBack: MutableState<Boolean>
        get() = TODO("Not yet implemented")
    actual val loadProgress: MutableIntState
        get() = TODO("Not yet implemented")
    actual val isLoading: MutableState<Boolean>
        get() = TODO("Not yet implemented")
    actual val isReloading: MutableState<Boolean>
        get() = TODO("Not yet implemented")

    actual fun reload() {
    }

    actual fun navigateBack() {
    }

}

@Composable
internal actual fun rememberWebViewState(
    url: Url,
    javascriptEnabled: Boolean,
    zoomSupportEnabled: Boolean
): WebViewState {
    TODO("Not yet implemented")
}