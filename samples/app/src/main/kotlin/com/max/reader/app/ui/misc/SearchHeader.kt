package com.max.reader.app.ui.misc

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.statusBarsPadding

@Composable
@OptIn(ExperimentalComposeUiApi::class)
fun SearchHeader(
    modifier: Modifier = Modifier,
    inputText: String,
    placeholderText: String,
    onSearchQueryUpdate: (String) -> Unit,
    onSearch: () -> Unit,
    onFocusChanged: (FocusState) -> Unit,
    shape: Shape = RoundedCornerShape(8.dp),
    colors: TextFieldColors = TextFieldDefaults.textFieldColors()
) {
    Card(
        modifier = modifier//rm
            .statusBarsPadding()
            .fillMaxWidth(),
        shape = shape
    ) {

        TextField(
            value = inputText,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged(onFocusChanged),
            placeholder = {
                Text(
                    text = placeholderText,
                    style = MaterialTheme.typography.subtitle2
                )
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            maxLines = 1,
            keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            textStyle = MaterialTheme.typography.subtitle2,
            colors = colors,
            trailingIcon = {
                IconButton(onClick = { onSearch() }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                }
            },
            onValueChange = onSearchQueryUpdate
        )
    }
}