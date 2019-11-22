package com.max.weatherviewer.presentation

import androidx.compose.Composable
import androidx.ui.core.Text
import androidx.ui.foundation.VerticalScroller
import androidx.ui.layout.Column
import androidx.ui.layout.FlexColumn
import androidx.ui.layout.Row
import androidx.ui.material.CircularProgressIndicator
import androidx.ui.material.TopAppBar
import com.max.weatherviewer.*

@Composable
fun HomeScreen(screen: Home, onMessage: (Message) -> Unit) {
    FlexColumn {
        inflexible {
            TopAppBar(
                title = { Text(text = "News Reader") },
                navigationIcon = {
                    VectorImageButton(R.drawable.ic_arrow_back_24) {
                        onMessage(Pop)
                    }
                }
            )
        }
        flexible(flex = 1f) {

            VerticalScroller {

                Column {

                    when (screen.state) {
                        Loading -> CircularProgressIndicator()
                        is Preview -> {
                            screen.state.articles.forEach { article ->
                                Row {
                                    Text(article.title.value)
                                }
                            }
                        }
                    }.safe
                }
            }
        }
    }
}