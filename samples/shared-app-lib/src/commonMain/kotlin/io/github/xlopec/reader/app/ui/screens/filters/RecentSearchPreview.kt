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

package io.github.xlopec.reader.app.ui.screens.filters

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.xlopec.reader.app.feature.filter.FiltersState
import io.github.xlopec.reader.app.misc.Loadable
import io.github.xlopec.reader.app.model.Filter
import io.github.xlopec.reader.app.model.FilterType
import io.github.xlopec.reader.app.model.Query
import io.github.xlopec.reader.app.ui.theme.ThemedPreview
import io.github.xlopec.tea.data.RandomUUID
import org.jetbrains.compose.ui.tooling.preview.Preview

private val PreviewState = FiltersState(
    id = RandomUUID(),
    filter = Filter(FilterType.Regular, Query.of("Android")),
    sourcesState = Loadable.newLoading()
)

@Preview
@Composable
internal fun FiltersPreviewDark() {
    ThemedPreview {
        FiltersScreen(
            state = PreviewState,
            handler = {}
        )
    }
}

@Preview
@Composable
internal fun FiltersPreviewLight() {
    ThemedPreview(isDarkModeEnabled = false) {
        FiltersScreen(
            state = PreviewState,
            handler = {},
        )
    }
}

@Preview
@Composable
internal fun SuggestionItems() {
    ThemedPreview {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RecentSearchItem(
                modifier = Modifier.fillMaxWidth(),
                suggestion = Query.of("Android search text")!!,
            )
            RecentSearchItem(
                modifier = Modifier.fillMaxWidth(),
                suggestion = Query.of("IOS search text")!!,
            )
            RecentSearchItem(
                modifier = Modifier.fillMaxWidth(),
                suggestion = Query.of("Desktop search text")!!,
            )
        }
    }
}
