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
import com.max.weatherviewer.app.Message
import com.max.weatherviewer.domain.Article
import com.max.weatherviewer.home.*
import com.max.weatherviewer.safe
import kotlinx.coroutines.runBlocking
import java.net.URL

@Composable
fun HomeScreen(screen: Home, onMessage: (Message) -> Unit) {
    VerticalScroller {

        Column(
            crossAxisSize = LayoutSize.Expand,
            mainAxisSize = LayoutSize.Expand,
            mainAxisAlignment = MainAxisAlignment.Center,
            crossAxisAlignment = CrossAxisAlignment.Center,
            modifier = Spacing(16.dp)
        ) {

            when (screen) {
                Loading -> ArticlesProgress()
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
private fun ArticlesError(cause: Throwable, onMessage: (HomeMessage) -> Unit) {
    Column(
        mainAxisSize = LayoutSize.Expand,
        mainAxisAlignment = MainAxisAlignment.Center
    ) {
        Text(
            text = "Failed to load articles, message: '${cause.message?.decapitalize() ?: "unknown exception"}'",
            style = (+themeTextStyle { subtitle2 }).withOpacity(0.87f)
        )

        Row(
            mainAxisSize = LayoutSize.Expand,
            mainAxisAlignment = MainAxisAlignment.Center
        ) {
            Button(
                text = "Retry",
                onClick = { onMessage(LoadArticles("bitcoin")) }
            )
        }

    }
}

private val cache = LruCache<URL, Bitmap>(20)

@Composable
private fun ArticleCard(article: Article) {
    Column {

        if (article.urlToImage != null) {

            Container(expanded = true, height = 180.dp) {
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


        Text(
            text = article.title.value,
            style = (+themeTextStyle { h6 }).withOpacity(0.87f)
        )

        Text(
            text = article.author.value,
            style = (+themeTextStyle { subtitle2 }).withOpacity(0.87f)
        )

        HeightSpacer(8.dp)

        Text(
            text = article.description.value,
            style = (+themeTextStyle { body2 }).withOpacity(0.6f)
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