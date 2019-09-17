@file:Suppress("FunctionName")

package com.max.weatherviewer.presentation.viewer

import androidx.compose.Composable
import androidx.compose.unaryPlus
import androidx.ui.core.Text
import androidx.ui.core.dp
import androidx.ui.layout.Column
import androidx.ui.layout.MainAxisAlignment
import androidx.ui.layout.Padding
import androidx.ui.layout.Row
import androidx.ui.material.Button
import androidx.ui.material.CircularProgressIndicator
import androidx.ui.material.MaterialTheme
import androidx.ui.material.themeTextStyle
import androidx.ui.text.ParagraphStyle
import androidx.ui.text.style.TextAlign
import androidx.ui.text.style.TextOverflow
import com.max.weatherviewer.api.weather.Weather
import com.oliynick.max.elm.core.misc.safe
import kotlinx.coroutines.channels.SendChannel

@Composable
fun WeatherView(relay: SendChannel<Message>, state: State) {
    MaterialTheme {

        when (state) {
            is State.Loading -> Progress()
            is State.Preview -> Preview(relay, state)
            is State.Initial -> Unit
            is State.LoadFailure -> Failure(relay, state)
        }.safe
    }
}

@Composable
fun Progress() {
    Padding(16.dp) {
        Row(mainAxisAlignment = MainAxisAlignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun Preview(messages: SendChannel<Message>, state: State.Preview) {
    Padding(16.dp) {

        Column(mainAxisAlignment = MainAxisAlignment.Center) {

            Text(
                text = formatWeather(state.data),
                style = +themeTextStyle { body2 },
                paragraphStyle = ParagraphStyle(TextAlign.Center)
            )

            Padding(top = 12.dp) {
                Row(mainAxisAlignment = MainAxisAlignment.Center) {
                    Button(
                        text = "Map",
                        onClick = { messages.offer(Message.SelectLocation) }
                    )
                }
            }
        }
    }
}

@Composable
fun Failure(relay: SendChannel<Message>, state: State.LoadFailure) {
    Padding(16.dp) {
        Column {
            Row {
                Text(
                    text = "Failed to perform action ${state.th.localizedMessage}",
                    style = +themeTextStyle { body2 },
                    maxLines = 10,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(mainAxisAlignment = MainAxisAlignment.Center) {
                Button("Retry",
                       onClick = { relay.offer(Message.Retry) })
            }
        }
    }
}

private fun formatWeather(w: Weather): String {
    return "Weather for lat=%.2f, lon=%.2f: wind speed is %.2f of %.2f degrees"
        .format(w.location.lat, w.location.lon, w.wind.speed, w.wind.degrees)
}