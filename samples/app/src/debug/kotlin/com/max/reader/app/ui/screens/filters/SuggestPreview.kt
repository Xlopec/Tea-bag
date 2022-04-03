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

package com.max.reader.app.ui.screens.filters

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.max.reader.app.ui.theme.ThemedPreview
import com.oliynick.max.reader.app.domain.Filter
import com.oliynick.max.reader.app.domain.FilterType
import com.oliynick.max.reader.app.domain.Query
import com.oliynick.max.reader.app.feature.filter.FiltersState
import com.oliynick.max.reader.app.misc.Loadable
import java.util.*

private val PreviewState = FiltersState(
    UUID.randomUUID(),
    Filter(FilterType.Regular, Query.of("Android")),
    Loadable.newLoading()
)

@Preview("Filters preview dark mode")
@Composable
fun FiltersPreviewDark() {
    ThemedPreview {
        FiltersScreen(
            state = PreviewState,
            handler = {}
        )
    }
}

@Preview("Filters preview light mode")
@Composable
fun FiltersPreviewLight() {
    ThemedPreview(isDarkModeEnabled = false) {
        FiltersScreen(
            state = PreviewState,
            handler = {}
        )
    }
}
