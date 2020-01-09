@file:Suppress("FunctionName")

package com.max.weatherviewer.screens.feed.ui

import android.graphics.Bitmap
import androidx.compose.*
import androidx.ui.core.*
import androidx.ui.engine.geometry.RRect
import androidx.ui.foundation.Clickable
import androidx.ui.foundation.VerticalScroller
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.graphics.Color
import androidx.ui.graphics.Paint
import androidx.ui.layout.*
import androidx.ui.material.Button
import androidx.ui.material.CircularProgressIndicator
import androidx.ui.material.MaterialTheme
import androidx.ui.material.ripple.Ripple
import androidx.ui.material.surface.Card
import androidx.ui.material.withOpacity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.Target
import com.max.weatherviewer.R
import com.max.weatherviewer.app.Message
import com.max.weatherviewer.app.ScreenId
import com.max.weatherviewer.domain.Article
import com.max.weatherviewer.misc.safe
import com.max.weatherviewer.screens.feed.*
import com.max.weatherviewer.screens.home.ImageButton
import com.max.weatherviewer.ui.loadImage2
import kotlinx.coroutines.*
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import kotlin.coroutines.resume

@Composable
fun FeedSearchHeader(
    id: ScreenId,
    criteria: LoadCriteria.Query,
    onMessage: (Message) -> Unit
) {

    Card(
        modifier = Spacing(all = 4.dp),
        shape = RoundedCornerShape(4.dp)
    ) {

        FlexRow(crossAxisAlignment = CrossAxisAlignment.Center) {

            flexible(1f) {

                Padding(4.dp) {
                    TextField(
                        value = criteria.query,
                        textStyle = (+MaterialTheme.typography()).subtitle2,
                        onValueChange = { query ->
                            onMessage(
                                OnQueryUpdated(
                                    id,
                                    query
                                )
                            )
                        }
                    )
                }
            }
            inflexible {
                ImageButton(
                    id = R.drawable.ic_search_black_24dp
                ) {
                    onMessage(LoadArticles(id))
                }
            }
        }
    }

}

@Composable
fun FeedScreen(
    screen: Feed,
    onMessage: (Message) -> Unit
) {

    VerticalScroller(modifier = Expanded) {

        Column(
            modifier = Spacing(16.dp) wraps ExpandedHeight
        ) {

            val criteria = screen.criteria

            if (criteria is LoadCriteria.Query) {
                FeedSearchHeader(screen.id, criteria, onMessage)
            }

            when (screen) {
                is FeedLoading -> FeedArticlesProgress()
                is Preview -> FeedArticles(screen, onMessage)
                is Error -> FeedError(screen, onMessage)
            }.safe
        }
    }
}

@Composable
private fun FeedArticlesProgress() {
    Row(modifier = Expanded, arrangement = Arrangement.Center) {
        Center {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun FeedArticles(
    screen: Preview,
    onMessage: (Message) -> Unit
) {

    if (screen.articles.isEmpty()) {
        FeedMessage(
            screen.id,
            "Feed is empty",
            onMessage
        )
    } else {
        screen.articles.forEachIndexed { index, article ->
            if (index != 0 && index != screen.articles.lastIndex) {
                HeightSpacer(8.dp)
            }
            ArticleCard(
                screen.id,
                article,
                onMessage
            )
        }
    }
}

@Composable
private fun FeedError(
    screen: Error,
    onMessage: (Message) -> Unit
) = FeedMessage(
    screen.id,
    "Failed to load articles, message: '${screen.cause.message?.decapitalize()
        ?: "unknown exception"}'",
    onMessage
)

@Composable
private fun FeedMessage(
    id: ScreenId,
    message: String,
    onMessage: (Message) -> Unit
) {

    Column(modifier = ExpandedHeight) {

        Align(Alignment.Center) {

            Text(
                text = message,
                style = (+MaterialTheme.typography()).subtitle2.withOpacity(0.87f)
            )
        }

        HeightSpacer(16.dp)

        Align(Alignment.Center) {

            Button(
                text = "Reload",
                onClick = {
                    onMessage(
                        LoadArticles(
                            id
                        )
                    )
                }
            )
        }

    }
}

fun <T> observe(data: Deferred<T>) = effectOf<T?> {
    val result = +state<T?> { null }
    val observer =
        +memo { GlobalScope.launch(start = CoroutineStart.LAZY) { result.value = data.await() } }
    +onCommit(data) {
        val j = GlobalScope.launch { observer.join() }
        onDispose { j.cancel() }
    }
    result.value
}

fun observe(url: URL) = effectOf<Bitmap?> {
    val result = +state<Bitmap?> { null }

    val observer = +memo {
        val cb: (Bitmap?) -> Unit = {
            result.value = it
        }

        cb
    }

    +onCommit(observer) {

        url.loadImage(callback = observer)
        //onDispose { j.cancel() }
    }
    result.value
}

@Composable
private fun ArticleCard(
    id: ScreenId,
    article: Article,
    onMessage: (Message) -> Unit
) {

    ClickableCard(
        onClick = {
            onMessage(
                OpenArticle(
                    article
                )
            )
        }
    ) {
        Column {
            ArticleImage(article)

            Padding(left = 8.dp, right = 8.dp) {
                ArticleTextContent(
                    article
                )
            }

            HeightSpacer(8.dp)

            ArticleActionsMenu(
                id,
                article,
                onMessage
            )
        }
    }
}

@Composable
fun ClickableCard(
    onClick: (() -> Unit)? = null,
    children: @Composable() () -> Unit
) {
    Card(
        modifier = Spacing(all = 4.dp),
        elevation = 2.dp,
        shape = RoundedCornerShape(8.dp)
    ) {

        Column(modifier = ExpandedWidth) {

            Ripple(bounded = true) {

                Clickable(onClick = onClick) {
                    children()
                }
            }
        }
    }
}

private fun ArticleActionsMenu(
    id: ScreenId,
    article: Article,
    onMessage: (Message) -> Unit
) {

    Row(
        modifier = ExpandedWidth,
        arrangement = Arrangement.End
    ) {
        ImageButton(
            R.drawable.ic_share_black_24dp,
            Color.Black
        ) {
            onMessage(
                ShareArticle(
                    article
                )
            )
        }
        ImageButton(
            R.drawable.ic_favorite_border_black_24dp,
            if (article.isFavorite) Color.Red else Color.Black
        ) {
            onMessage(
                ToggleArticleIsFavorite(
                    id,
                    article
                )
            )
        }
    }
}

@Composable
private fun ArticleImage(
    article: Article
) {
    Clip(shape = RoundedCornerShape(8.dp)) {

        Container(modifier = MinHeight(180.dp) wraps ExpandedWidth) {
            Clip(shape = RoundedCornerShape(8.dp, 8.dp)) {
                // fixme Composition requires active composition context
                // fixme Not in frame

                /*val state = state<Bitmap?> { null }
                val result = +state

                if (article.urlToImage != null) {
                    val observer = +memo {
                        val cb: (Bitmap?) -> Unit = {
                            Handler(Looper.getMainLooper()).post {
                                println("Update")

                                result.value = it

                                println("post Update")
                            }

                        }

                        cb
                    }

                    +onCommit {

                        article.urlToImage.loadImage(callback = observer)
                        //onDispose { j.cancel() }
                    }


                    println("VALUE ${result.value}")
                }

                if (result.value == null) {*/
                DrawImagePlaceholder()
                /* } else {
                     DrawImage(MyImage(result.value!!))
                 }*/
            }
        }
    }
}

private val dateFormatter: SimpleDateFormat by lazy {
    SimpleDateFormat(
        "dd MMM' at 'hh:mm",
        Locale.getDefault()
    )
}

@Composable
private fun ArticleTextContent(article: Article) {
    Column {

        val typography = +MaterialTheme.typography()

        Text(
            text = article.title.value,
            style = typography.h6.withOpacity(0.87f)
        )

        if (article.author != null) {
            Text(
                text = article.author.value,
                style = typography.subtitle2.withOpacity(0.87f)
            )
        }

        /*
        Text(
            text = "Published on ${dateFormatter.format(article.published)}",
            style = typography.caption.withOpacity(0.87f)
        )*/

        HeightSpacer(8.dp)

        Text(
            text = article.description?.value ?: "No description",
            style = typography.body2.withOpacity(0.6f)
        )
    }
}

@Composable
private fun DrawImagePlaceholder() {
    val paint = +memo { Paint().apply { color = Color.Gray } }

    Draw { canvas, parentSize -> canvas.drawRRect(parentSize.toRRect(), paint) }
}

private fun PxSize.toRRect() = RRect(0f, 0f, width.value, height.value)

// fixme Composition requires active composition context

private val scheduler = Executors.newFixedThreadPool(3)

private fun URL.loadImage(
    width: Int = 100.dp.value.toInt(),
    height: Int = 180.dp.value.toInt(),
    callback: (Bitmap?) -> Unit
) {

    scheduler.submit {
        /* val bm = Glide.with(+ambient(ContextAmbient))
             .asBitmap()
             .load(toExternalForm())
             .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
             .centerCrop()
             .submit(width, height)
             .get()*/

        loadImage2(
            this,
            width,
            height,
            callback
        )
    }
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