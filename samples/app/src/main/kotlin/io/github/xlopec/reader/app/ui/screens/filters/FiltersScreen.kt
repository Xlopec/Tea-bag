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

import androidx.activity.compose.BackHandler
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
import io.github.xlopec.reader.app.MessageHandler
import io.github.xlopec.reader.app.domain.Query
import io.github.xlopec.reader.app.feature.article.list.FilterUpdated
import io.github.xlopec.reader.app.feature.article.list.LoadArticles
import io.github.xlopec.reader.app.feature.filter.FiltersState
import io.github.xlopec.reader.app.feature.filter.InputChanged
import io.github.xlopec.reader.app.feature.navigation.Pop
import io.github.xlopec.reader.app.ui.misc.SearchHeader
import io.github.xlopec.reader.app.ui.screens.article.toSearchHint
import io.github.xlopec.reader.app.ui.screens.filters.ScreenAnimationState.Begin
import io.github.xlopec.reader.app.ui.screens.filters.ScreenAnimationState.Finish

@Composable
fun FiltersScreen(
    state: FiltersState,
    handler: MessageHandler,
) {
    var screenTransitionState by remember { mutableStateOf(Begin) }
    var closeScreen by remember { mutableStateOf(false) }
    var performSearch by remember { mutableStateOf(false) }
    val screenTransition =
        updateTransition(label = "Header transition", targetState = screenTransitionState)

    val headerTransition = screenTransition.headerTransitionState()
    val childTransition = screenTransition.childTransitionState()

    val focusRequester = remember { FocusRequester() }

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

    LaunchedEffect(state.filter) {
        handler(FilterUpdated(state.id, state.filter))
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            item {
                SearchHeader(
                    modifier = Modifier
                        .padding(
                            horizontal = headerTransition.horizontalPadding,
                            vertical = 16.dp
                        )
                        .focusRequester(focusRequester),
                    elevation = headerTransition.elevation,
                    inputText = state.filter.query?.value ?: "",
                    placeholderText = state.filter.type.toSearchHint(),
                    onQueryUpdate = {
                        handler(InputChanged(state.id, Query.of(it)))
                    },
                    onSearch = {
                        performSearch = true
                        closeScreen = true
                    },
                    shape = RoundedCornerShape(headerTransition.cornerRadius),
                    colors = headerTransition.textFieldTransitionColors()
                )
            }

            item {
                SourcesSection(
                    id = state.id,
                    modifier = Modifier.fillParentMaxWidth(),
                    sources = state.sourcesState,
                    childTransitionState = childTransition,
                    handler = handler,
                    state = state
                )
            }

            if (state.suggestions.isNotEmpty()) {
                suggestionsSection(
                    suggestions = state.suggestions,
                    childTransitionState = childTransition
                ) { suggestion ->
                    handler(InputChanged(state.id, suggestion))
                    performSearch = true
                    closeScreen = true
                }
            }
        }
    }
}

private infix fun <T> Transition<T>.transitionedTo(
    state: T,
): Boolean = targetState == currentState && targetState == state && !isRunning
