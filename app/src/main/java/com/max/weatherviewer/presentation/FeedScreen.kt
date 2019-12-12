@file:Suppress("FunctionName")

package com.max.weatherviewer.presentation

import android.graphics.Bitmap
import androidx.collection.LruCache
import androidx.compose.Composable
import androidx.compose.memo
import androidx.compose.unaryPlus
import androidx.ui.core.Clip
import androidx.ui.core.Opacity
import androidx.ui.core.Text
import androidx.ui.core.dp
import androidx.ui.foundation.Clickable
import androidx.ui.foundation.DrawImage
import androidx.ui.foundation.VerticalScroller
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.graphics.Color
import androidx.ui.layout.*
import androidx.ui.material.*
import androidx.ui.material.ripple.Ripple
import com.max.weatherviewer.R
import com.max.weatherviewer.app.ScreenId
import com.max.weatherviewer.domain.Article
import com.max.weatherviewer.home.*
import com.max.weatherviewer.presentation.main.ImageButton
import com.max.weatherviewer.safe
import kotlinx.coroutines.runBlocking
import java.net.URL

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

private val cache = LruCache<URL, Bitmap>(20)

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
                                        // fixme seems there is currently no way to load images off the main thread in Compose
                                        val image = +memo {
                                            cache.get(article.urlToImage) ?: runBlocking {
                                                loadImage(
                                                    article.urlToImage,
                                                    100,
                                                    180
                                                )
                                            }
                                        }

                                        cache.put(article.urlToImage, image)

                                        DrawImage(MyImage(image))
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
private fun ArticleDivider() {
    Padding(top = 4.dp, bottom = 6.dp) {
        Opacity(0.08f) {
            Divider()
        }
    }
}