@file:Suppress("FunctionName")

package com.max.weatherviewer.presentation

import android.graphics.Bitmap
import androidx.compose.*
import androidx.ui.core.*
import androidx.ui.engine.geometry.RRect
import androidx.ui.foundation.Clickable
import androidx.ui.foundation.DrawImage
import androidx.ui.foundation.VerticalScroller
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.graphics.Color
import androidx.ui.graphics.Paint
import androidx.ui.layout.*
import androidx.ui.material.Button
import androidx.ui.material.CircularProgressIndicator
import androidx.ui.material.MaterialTheme
import androidx.ui.material.ripple.Ripple
import androidx.ui.material.withOpacity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.Target
import com.max.weatherviewer.R
import com.max.weatherviewer.app.ScreenId
import com.max.weatherviewer.domain.Article
import com.max.weatherviewer.home.*
import com.max.weatherviewer.presentation.main.ImageButton
import com.max.weatherviewer.safe
import kotlinx.coroutines.*
import java.net.URL
import kotlin.coroutines.resume

@Composable
fun FeedScreen(screen: Feed, onMessage: (FeedMessage) -> Unit) {
    VerticalScroller {

        Column(
            modifier = Spacing(16.dp)
        ) {

            when (screen) {
                is FeedLoading -> FeedArticlesProgress()
                is Preview -> screen.FeedArticles(onMessage)
                is Error -> screen.FeedError(onMessage)
            }.safe
        }
    }
}

@Composable
private fun FeedArticlesProgress() {
    CircularProgressIndicator()
}

@Composable
private fun Preview.FeedArticles(
    onMessage: (FeedMessage) -> Unit
) {

    if (articles.isEmpty()) {
        FeedMessage(id, "Feed is empty", onMessage)
    } else {
        articles.forEach { article -> ArticleCard(id, article, onMessage) }
    }
}

@Composable
private fun Error.FeedError(
    onMessage: (FeedMessage) -> Unit
) = FeedMessage(
    id,
    "Failed to load articles, message: '${cause.message?.decapitalize() ?: "unknown exception"}'",
    onMessage
)

@Composable
private fun FeedMessage(
    id: ScreenId,
    message: String,
    onMessage: (FeedMessage) -> Unit
) {
    Column(
        modifier = ExpandedWidth
    ) {

        Text(
            text = message,
            style = (+MaterialTheme.typography()).subtitle2.withOpacity(0.87f)
        )

        HeightSpacer(16.dp)

        Button(
            text = "Reload",
            onClick = { onMessage(LoadArticles(id)) }
        )

    }
}

fun <T> observe(data: Deferred<T>) = effectOf<T?> {
    val result = +state<T?> { null }
    val observer = +memo { GlobalScope.launch(start = CoroutineStart.LAZY) { result.value = data.await() } }
    +onCommit(data) {
        val j = GlobalScope.launch { observer.join() }
        onDispose { j.cancel() }
    }
    result.value
}

@Composable
private fun ArticleCard(
    id: ScreenId,
    article: Article,
    onMessage: (FeedMessage) -> Unit
) {
    Column(modifier = ExpandedWidth) {

        Clip(shape = RoundedCornerShape(8.dp)) {

            Ripple(
                bounded = true
            ) {

                Clickable(onClick = { }) {

                    Column {

                        Clip(shape = RoundedCornerShape(8.dp)) {

                            if (article.urlToImage != null) {

                                Container(modifier = MinHeight(180.dp) wraps ExpandedWidth) {
                                    Clip(shape = RoundedCornerShape(8.dp)) {
                                        // fixme Composition requires active composition context
                                        // fixme Not in frame
                                        val o = +observe(GlobalScope.async(start = CoroutineStart.LAZY) { loadImage(article.urlToImage, 100, 180) })

                                            if (o == null) {
                                                DrawImagePlaceholder()
                                            } else {
                                                DrawImage(MyImage(o))
                                            }
                                    }
                                }
                            }
                        }

                        val typography = +MaterialTheme.typography()


                        Text(
                            text = article.title.value,
                            style = typography.h6.withOpacity(0.87f)
                        )

                        Text(
                            text = article.author.value,
                            style = typography.subtitle2.withOpacity(0.87f)
                        )

                        HeightSpacer(8.dp)

                        Text(
                            text = article.description.value,
                            style = typography.body2.withOpacity(0.6f)
                        )

                        HeightSpacer(8.dp)

                        Row(
                            modifier = ExpandedWidth,
                            arrangement = Arrangement.End
                        ) {
                            ImageButton(
                                R.drawable.ic_favorite_border_black_24dp,
                                if (article.isFavorite) Color.Red else Color.Black
                            ) {
                                onMessage(ToggleArticleIsFavorite(id, article))
                            }
                        }

                    }
                }
            }
        }

        HeightSpacer(16.dp)
    }
}

@Composable
private fun DrawImagePlaceholder() {
    val paint = +memo { Paint().apply { color = Color.Gray } }

    Draw { canvas, parentSize -> canvas.drawRRect(parentSize.toRRect(), paint) }
}

private fun PxSize.toRRect() = RRect(0f, 0f, width.value, height.value)

// fixme Composition requires active composition context
private fun observe(url: URL) = effectOf<Bitmap?> {
    val result = +state<Bitmap?> { null }

    val def = +memo { GlobalScope.async(start = CoroutineStart.LAZY) { url.loadImage() } }

    +onCommit(url) {
        val job = GlobalScope.launch { result.value = def.await() }

        onDispose(job::cancel)
    }

    result.value
}

private suspend fun URL.loadImage(
    width: Int = Target.SIZE_ORIGINAL,
    height: Int = 180.dp.value.toInt()
) =
    suspendCancellableCoroutine<Bitmap?> { c ->

        val bitmapFuture = Glide.with(+ambient(ContextAmbient))
            .asBitmap()
            .load(toExternalForm())
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .centerCrop()
            .submit(width, height)

        c.invokeOnCancellation { bitmapFuture.cancel(true) }
        c.resume(bitmapFuture.get())
    }