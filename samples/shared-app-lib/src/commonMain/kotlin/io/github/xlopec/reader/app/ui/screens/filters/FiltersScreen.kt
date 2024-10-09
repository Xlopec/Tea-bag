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

import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import io.github.xlopec.reader.app.FilterUpdated
import io.github.xlopec.reader.app.MessageHandler
import io.github.xlopec.reader.app.feature.article.list.LoadArticles
import io.github.xlopec.reader.app.feature.filter.FiltersState
import io.github.xlopec.reader.app.feature.filter.RecentSearchRemoved
import io.github.xlopec.reader.app.feature.navigation.Pop
import io.github.xlopec.reader.app.model.Query
import io.github.xlopec.reader.app.model.query
import io.github.xlopec.reader.app.ui.misc.SearchHeader
import io.github.xlopec.reader.app.ui.screens.BackHandler
import io.github.xlopec.reader.app.ui.screens.article.toSearchHint
import io.github.xlopec.reader.app.ui.screens.filters.ScreenAnimationState.Begin
import io.github.xlopec.reader.app.ui.screens.filters.ScreenAnimationState.Finish

internal const val HeaderSectionId = "header section"
internal const val SourcesSectionId = "sources section"
internal const val RecentSearchesSubtitle = "recent searches subtitle"

@Composable
internal fun FiltersScreen(
    state: FiltersState,
    handler: MessageHandler,
    modifier: Modifier = Modifier,
) {
    var screenTransitionState by remember { mutableStateOf(Begin) }
    var closeScreen by remember { mutableStateOf(false) }
    var performSearch by remember { mutableStateOf(false) }
    val screenTransition =
        updateTransition(label = "Header transition", targetState = screenTransitionState)

    val headerTransition = screenTransition.headerTransitionState()
    val childTransition = screenTransition.childTransitionState()

    val focusRequester = remember { FocusRequester() }
    val inputState = remember { mutableStateOf(state.filter.query) }

    if (closeScreen) {
        LaunchedEffect(Unit) {
            screenTransitionState = Begin
        }

        if (screenTransition transitionedTo Begin) {
            focusRequester.freeFocus()

            if (performSearch) {
                handler(LoadArticles(state.id))
            }
            handler(Pop)
        }
    } else {
        LaunchedEffect(Unit) {
            screenTransitionState = Finish
        }

        if (screenTransition transitionedTo Finish) {
            focusRequester.requestFocus()
        }
    }

    BackHandler {
        closeScreen = true
    }

    if (closeScreen) {
        LaunchedEffect(Unit) {
            handler(FilterUpdated(state.filter.query(inputState.value)))
        }
    }

    Scaffold(
        modifier = modifier,
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            item(key = HeaderSectionId) {
                SearchHeader(
                    modifier = Modifier
                        .padding(
                            horizontal = headerTransition.horizontalPadding,
                            vertical = 16.dp
                        )
                        .focusRequester(focusRequester),
                    elevation = headerTransition.elevation,
                    inputText = inputState.value?.value ?: "",
                    placeholderText = state.filter.type.toSearchHint(),
                    onQueryUpdate = { inputState.value = Query.of(it) },
                    onSearch = {
                        performSearch = true
                        closeScreen = true
                    },
                    shape = RoundedCornerShape(headerTransition.cornerRadius),
                    colors = headerTransition.textFieldTransitionColors()
                )
            }

            item(key = SourcesSectionId) {
                SourcesSection(
                    id = state.id,
                    modifier = Modifier.fillParentMaxWidth(),
                    sources = state.sourcesState,
                    childTransitionState = childTransition,
                    handler = handler,
                    state = state
                )
            }

            if (state.recentSearches.isNotEmpty()) {
                recentSearchesSection(
                    suggestions = state.recentSearches,
                    childTransitionState = childTransition,
                    onSelect = { suggestion ->
                        inputState.value = suggestion
                        performSearch = true
                        closeScreen = true
                    },
                    onDelete = { suggestion ->
                        handler(RecentSearchRemoved(state.id, suggestion))
                    },
                )
            }
        }
    }
}

private infix fun <T> Transition<T>.transitionedTo(
    state: T,
): Boolean = targetState == currentState && targetState == state && !isRunning
