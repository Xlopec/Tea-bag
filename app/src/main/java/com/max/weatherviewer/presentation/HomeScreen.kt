package com.max.weatherviewer.presentation

import androidx.compose.Composable
import androidx.ui.core.Text
import androidx.ui.foundation.VerticalScroller
import androidx.ui.layout.Column
import androidx.ui.layout.FlexColumn
import androidx.ui.material.TopAppBar
import com.max.weatherviewer.Home
import com.max.weatherviewer.Message
import com.max.weatherviewer.Pop
import com.max.weatherviewer.R

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
                    Text("Text")
                }
            }
        }
    }
}