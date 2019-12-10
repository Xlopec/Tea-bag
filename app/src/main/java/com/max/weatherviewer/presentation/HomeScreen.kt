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
import androidx.ui.foundation.DrawImage
import androidx.ui.foundation.VerticalScroller
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.layout.*
import androidx.ui.material.*
import com.max.weatherviewer.domain.Article
import com.max.weatherviewer.home.*
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
                is FeedLoading -> ArticlesProgress()
                is Preview -> Articles(screen.articles)
                is Error -> ArticlesError(screen.cause, onMessage)
            }.safe
        }
    }
}

@Composable
private fun ArticlesProgress() {
    CircularProgressIndicator()
}

@Composable
private fun Articles(articles: Iterable<Article>) {
    articles.forEach { article ->
        ArticleCard(article)
    }
}

@Composable
private fun ArticlesError(cause: Throwable, onMessage: (FeedMessage) -> Unit) {
    Column(
        modifier = Expanded
    ) {
        Text(
            text = "Failed to load articles, message: '${cause.message?.decapitalize()
                ?: "unknown exception"}'",
            style = (+MaterialTheme.typography()).subtitle2.withOpacity(0.87f)
        )

        Row(
            modifier = Expanded
        ) {
            Button(
                text = "Retry",
                onClick = {/* onMessage(LoadArticles("bitcoin")) */}
            )
        }

    }
}

private val cache = LruCache<URL, Bitmap>(20)

@Composable
private fun ArticleCard(article: Article) {
    Column(modifier = ExpandedWidth) {

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

        ArticleDivider()
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