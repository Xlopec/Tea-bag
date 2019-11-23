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
import androidx.ui.material.CircularProgressIndicator
import androidx.ui.material.Divider
import androidx.ui.material.themeTextStyle
import androidx.ui.material.withOpacity
import com.max.weatherviewer.*
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

            when (screen.state) {
                Loading -> Progress()
                is Preview -> Articles(screen.state.articles)
            }.safe
        }
    }
}

@Composable
private fun Progress() {
    CircularProgressIndicator()
}

@Composable
private fun Articles(articles: Iterable<Article>) {
    articles.forEach { article ->
        ArticleCard(article)
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