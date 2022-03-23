package com.max.reader.app.ui.misc

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons.Default
import androidx.compose.material.icons.filled.Replay
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowCrossAxisAlignment
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow

@Composable
fun RowMessage(
    modifier: Modifier = Modifier,
    message: String,
    onClick: () -> Unit,
) {
    FlowRow(
        modifier = modifier,
        mainAxisAlignment = FlowMainAxisAlignment.Center,
        crossAxisAlignment = FlowCrossAxisAlignment.Center,
        mainAxisSpacing = 16.dp,
        crossAxisSpacing = 8.dp
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

@Composable
fun ColumnMessage(
    modifier: Modifier = Modifier,
    title: String,
    message: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Text(
            text = title,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.subtitle1
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = message,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.body1
        )

        IconButton(onClick = onClick) {
            Icon(imageVector = Default.Replay, contentDescription = "Retry")
        }
    }
}
