@file:Suppress("FunctionName")

package com.max.weatherviewer.screens.feed.ui

import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.Px
import androidx.compose.foundation.Image
import androidx.compose.foundation.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.runtime.*
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageAsset
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.max.weatherviewer.R
import com.max.weatherviewer.app.Message
import com.max.weatherviewer.app.ScreenId
import com.max.weatherviewer.domain.Article
import com.max.weatherviewer.misc.safe
import com.max.weatherviewer.screens.feed.*
import com.max.weatherviewer.ui.theme.lightThemeColors
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume

private val dateFormatter: SimpleDateFormat by lazy {
    SimpleDateFormat("dd MMM' at 'hh:mm", Locale.getDefault())
}


@Composable
fun FeedScreen(
    screen: Feed,
    onMessage: (Message) -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        alignment = Alignment.Center
    ) {
        when (screen) {
            is FeedLoading -> FeedArticlesProgress()
            is Preview -> FeedArticles(screen, onMessage)
            is Error -> FeedError(screen.id, screen.toReadableMessage(), onMessage)
        }.safe
    }
}

private fun Error.toReadableMessage() =
    cause.message?.decapitalize(Locale.getDefault()) ?: "unknown exception"

@Composable
fun FeedArticlesProgress() {
    CircularProgressIndicator()
}

@Composable
fun FeedArticles(
    screen: Preview,
    onMessage: (Message) -> Unit,
) {

    if (screen.articles.isEmpty()) {
        Message(
            message = "Feed is empty",
            actionText = "Reload"
        ) {
            onMessage(LoadArticles(screen.id))
        }
    } else {
        LazyColumnFor(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 8.dp),
            items = screen.articles
        ) { article ->
            ArticleItem(screen.id, article, onMessage)
        }
    }
}

@Composable
fun ArticleItem(
    screenId: ScreenId,
    article: Article,
    onMessage: (Message) -> Unit,
) {

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = { onMessage(OpenArticle(article)) })
            .padding(8.dp)
    ) {

        Surface(
            modifier = Modifier
                .preferredHeight(180.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.2f)
        ) {

            if (article.urlToImage != null) {

                val imageState = article.urlToImage.loadImage(ContextAmbient.current)
                val image = imageState.value

                if (image != null) {
                    Image(image.asImageAsset())
                }
            }

        }

        Text(text = article.title.value)

        if (article.author != null) {
            Text(
                text = article.author.value,
                style = typography.subtitle2
            )
        }

        Text(
            text = "Published on ${dateFormatter.format(article.published)}",
            style = typography.caption
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = article.description?.value ?: "No description",
            style = typography.body2
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {

            IconButton(
                onClick = { onMessage(ShareArticle(article)) }
            ) {
                Icon(asset = vectorResource(id = R.drawable.ic_share_black_24dp))
            }

            IconButton(
                onClick = { onMessage(ToggleArticleIsFavorite(screenId, article)) }
            ) {
                Icon(
                    tint = if (article.isFavorite) lightThemeColors.primary else lightThemeColors.onSecondary,
                    asset = vectorResource(id = R.drawable.ic_favorite_border_black_24dp),
                )
            }
        }
    }
}

@Composable
fun FeedError(
    id: ScreenId,
    message: String,
    onMessage: (Message) -> Unit,
) {
    Message(
        "Failed to load articles, message: '${message.decapitalize(Locale.getDefault())}'",
        "Retry"
    ) {
        onMessage(LoadArticles(id))
    }
}

@Composable
private fun Message(
    message: String,
    actionText: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier.padding(16.dp).fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            textAlign = TextAlign.Center
        )

        TextButton(
            onClick = onClick
        ) {
            Text(text = actionText)
        }
    }

}

@Composable
fun URL.loadImage(
    context: Context,
    @Px width: Int = 100.dp.value.toInt(),
    @Px height: Int = 180.dp.value.toInt(),
): State<Bitmap?> {
    val state = remember { mutableStateOf<Bitmap?>(null) }

    onCommit(this) {
        val j = GlobalScope.launch {
            state.value = doLoadImage(context, width, height)
        }

        onDispose { j.cancel() }
    }

    return state
}

private suspend fun URL.doLoadImage(
    context: Context,
    @Px width: Int,
    @Px height: Int,
) =
    suspendCancellableCoroutine<Bitmap?> { c ->

        val bitmapFuture = Glide.with(context)
            .asBitmap()
            .load(toExternalForm())
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .centerCrop()
            .submit(width, height)

        c.invokeOnCancellation { bitmapFuture.cancel(true) }
        c.resume(bitmapFuture.get())
    }