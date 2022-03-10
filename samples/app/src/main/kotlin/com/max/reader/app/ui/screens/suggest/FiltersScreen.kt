package com.max.reader.app.ui.screens.suggest

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.ExperimentalTransitionApi
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import com.max.reader.app.MessageHandler
import com.max.reader.app.ui.misc.SearchHeader
import com.max.reader.app.ui.screens.article.toSearchHint
import com.max.reader.app.ui.screens.suggest.ScreenAnimationState.Begin
import com.max.reader.app.ui.screens.suggest.ScreenAnimationState.Finish
import com.oliynick.max.reader.app.domain.Query
import com.oliynick.max.reader.app.feature.article.list.FilterUpdated
import com.oliynick.max.reader.app.feature.article.list.LoadArticles
import com.oliynick.max.reader.app.feature.filter.InputChanged
import com.oliynick.max.reader.app.feature.filter.SuggestState
import com.oliynick.max.reader.app.feature.navigation.Pop

@OptIn(
    ExperimentalMaterialApi::class, ExperimentalFoundationApi::class,
    ExperimentalTransitionApi::class
)
@Composable
fun FiltersScreen(
    state: SuggestState,
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
