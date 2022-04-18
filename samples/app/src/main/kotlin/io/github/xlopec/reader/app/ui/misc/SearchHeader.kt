/*
 * MIT License
 *
 * Copyright (c) 2022. Maksym Oliinyk.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.xlopec.reader.app.ui.misc

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldColors
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons.Default
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.input.ImeAction.Companion.Search
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.statusBarsPadding

@Composable
fun SearchHeader(
    modifier: Modifier = Modifier,
    inputText: String,
    placeholderText: String,
    onQueryUpdate: (String) -> Unit,
    onSearch: () -> Unit,
    onFocusChanged: (FocusState) -> Unit = {},
    shape: Shape = RoundedCornerShape(8.dp),
    elevation: Dp = 1.dp,
    colors: TextFieldColors = TextFieldDefaults.textFieldColors(),
    leadingIcon: @Composable (() -> Unit)? = null,
) {
    Card(
        modifier = modifier//rm
            .statusBarsPadding()
            .fillMaxWidth(),
        elevation = elevation,
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
            keyboardOptions = KeyboardOptions(imeAction = Search),
            singleLine = true,
            keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            textStyle = MaterialTheme.typography.subtitle2,
            colors = colors,
            leadingIcon = leadingIcon,
            trailingIcon = {
                IconButton(onClick = { onSearch() }) {
                    Icon(
                        imageVector = Default.Search,
                        contentDescription = "Search"
                    )
                }
            },
            onValueChange = onQueryUpdate
        )
    }
}
