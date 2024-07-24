package io.github.xlopec.reader.app.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.view.ViewGroup.LayoutParams
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.widget.NestedScrollView
import io.github.xlopec.tea.data.Url

@Composable
internal actual fun WebView(modifier: Modifier, url: Url) {
    val webViewState = rememberWebViewState(url)

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            NestedScrollView(it).apply {
                layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                addView(webViewState.webView, 0, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
            }
        },
    )
}

@Composable
internal actual fun rememberWebViewState(
    url: Url,
    javascriptEnabled: Boolean,
    zoomSupportEnabled: Boolean,
): WebViewState {
    val context = LocalContext.current
    val state = remember {
        WebViewState(
            url = url,
            context = context,
            javascriptEnabled = javascriptEnabled,
            zoomSupportEnabled = zoomSupportEnabled
        )
    }

    DisposableEffect(Unit) {
        onDispose(state.webView::destroy)
    }

    return state
}

@Stable
internal actual class WebViewState(
    url: Url,
    context: Context,
    javascriptEnabled: Boolean = true,
    zoomSupportEnabled: Boolean = true,
) {
    actual val title = mutableStateOf<String?>(null)
    actual val canGoBack = mutableStateOf(false)
    actual val loadProgress = mutableIntStateOf(0)
    actual val isLoading = mutableStateOf(false)
    actual val isReloading = mutableStateOf(false)

    val webView = WebView(context).apply {
        settings.javaScriptEnabled = javascriptEnabled
        settings.setSupportZoom(zoomSupportEnabled)
        webChromeClient = object : WebChromeClient() {
            override fun onReceivedTitle(
                view: WebView,
                title: String,
            ) {
                this@WebViewState.title.value = title
            }

            override fun onProgressChanged(
                view: WebView,
                newProgress: Int,
            ) {
                loadProgress.intValue = newProgress
            }
        }
        webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                isLoading.value = true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                isLoading.value = false
                isReloading.value = false
            }

            override fun doUpdateVisitedHistory(
                view: WebView,
                url: String?,
                isReload: Boolean,
            ) {
                if (!isReload) {
                    canGoBack.value = view.canGoBack()
                }
            }
        }

        loadUrl(url.toString())
    }

    actual fun reload() {
        isReloading.value = true
        webView.reload()
    }

    actual fun navigateBack() = webView.goBack()
}