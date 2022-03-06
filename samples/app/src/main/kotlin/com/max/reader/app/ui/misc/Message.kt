package com.max.reader.app.ui.misc

import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons.Default
import androidx.compose.material.icons.filled.Replay
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun RowMessage(
    modifier: Modifier = Modifier,
    message: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = message,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.width(16.dp))

        IconButton(onClick = onClick) {
            Icon(imageVector = Default.Replay, contentDescription = "Retry")
        }
    }
}

@Composable
fun ColumnMessage(
    modifier: Modifier = Modifier,
    message: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            textAlign = TextAlign.Center
        )

        IconButton(onClick = onClick) {
            Icon(imageVector = Default.Replay, contentDescription = "Retry")
        }
    }
}