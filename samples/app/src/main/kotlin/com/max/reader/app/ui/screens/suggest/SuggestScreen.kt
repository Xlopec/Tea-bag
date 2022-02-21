package com.max.reader.app.ui.screens.suggest

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.max.reader.app.ui.misc.SearchHeader
import com.max.reader.app.ui.screens.suggest.AnimationState.End
import com.max.reader.app.ui.screens.suggest.AnimationState.Start
import com.max.reader.app.ui.screens.suggest.ScreenAnimationState.*
import com.oliynick.max.reader.app.Message
import com.oliynick.max.reader.app.feature.article.list.LoadArticlesFromScratch
import com.oliynick.max.reader.app.feature.article.list.OnQueryUpdated
import com.oliynick.max.reader.app.feature.navigation.Pop
import com.oliynick.max.reader.app.feature.suggest.SuggestState
import com.oliynick.max.reader.app.feature.suggest.SuggestionQueryUpdated

private enum class ScreenAnimationState {
    Begin, Half, Finish
}

operator fun ((Message) -> Unit).invoke(
    vararg message: Message
) {
    message.forEach(::invoke)
}

@OptIn(
    ExperimentalMaterialApi::class, ExperimentalFoundationApi::class,
    ExperimentalTransitionApi::class
)
@Composable
fun SuggestScreen(
    state: SuggestState,
    onMessage: (Message) -> Unit,
) {
    var screenTransitionState by remember { mutableStateOf(Begin) }
    var closeScreen by remember { mutableStateOf(false) }
    var performSearch by remember { mutableStateOf(false) }
    val screenTransition = updateTransition(label = "Header transition", targetState = screenTransitionState)

    val headerTransitionState = screenTransition.headerTransitionState()
    val childTransitionState = screenTransition.childTransitionState()

    val focusRequester = remember { FocusRequester() }

    if (closeScreen) {
        LaunchedEffect(Unit) {
            screenTransitionState = Begin
        }

        if (screenTransition transitionedTo Begin) {
            focusRequester.freeFocus()
            onMessage(Pop)
        }
    } else {
        LaunchedEffect(Unit) {
            screenTransitionState = Finish
        }

        if (screenTransition transitionedTo Finish) {
            focusRequester.requestFocus()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (performSearch) {
                onMessage(LoadArticlesFromScratch(state.id))
            }
        }
    }

    BackHandler {
        closeScreen = true
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
                            horizontal = headerTransitionState.horizontalPadding,
                            vertical = 16.dp
                        )
                        .focusRequester(focusRequester),
                    inputText = state.query.input,
                    placeholderText = "Search in articles",
                    onQueryUpdate = { onMessage(SuggestionQueryUpdated(state.id, it), OnQueryUpdated(state.id, it)) },
                    onSearch = {
                        performSearch = true
                        closeScreen = true
                    },
                    shape = RoundedCornerShape(headerTransitionState.cornerRadius),
                    colors = headerTransitionState.textFieldTransitionColors()
                )
            }

            item {
                SuggestionsSubtitle(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = 16.dp)
                        .alpha(childTransitionState.contentAlpha)
                )
            }

            items(state.suggestions, { it }) { item ->
                SuggestionItem(
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .clickable { }
                        .animateItemPlacement()
                        .alpha(childTransitionState.contentAlpha)
                        .padding(all = 16.dp)
                        .offset(y = childTransitionState.listItemOffsetY),
                    suggestion = item
                )
            }
        }
    }
}

@Composable
private fun SuggestionsSubtitle(
    modifier: Modifier,
) {
    Text(
        text = "Recent searches",
        modifier = modifier,
        textAlign = TextAlign.Start,
        style = MaterialTheme.typography.subtitle1
    )
}

@Composable
private fun SuggestionItem(
    modifier: Modifier,
    suggestion: String,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = Icons.Default.Image, contentDescription = null)

        Spacer(modifier = Modifier.width(8.dp))

        Text(text = suggestion)
    }
}

private enum class AnimationState {
    Start, End,
}

private data class HeaderTransitionState(
    val _textBackground: State<Color>,
    val _indicatorColor: State<Color>,
    val _horizontalPadding: State<Dp>,
    val _cornerRadius: State<Dp>,
    val transition: Transition<AnimationState>,
) {
    val horizontalPadding by _horizontalPadding
    val cornerRadius by _cornerRadius
    val textBackground by _textBackground
    val indicatorColor by _indicatorColor

    @Composable
    fun textFieldTransitionColors(): TextFieldColors =
        TextFieldDefaults.textFieldColors(
            textColor = Color.White.copy(LocalContentAlpha.current),
            backgroundColor = textBackground,
            focusedIndicatorColor = indicatorColor,
            unfocusedIndicatorColor = indicatorColor,
            disabledIndicatorColor = indicatorColor,
            errorIndicatorColor = indicatorColor,
            cursorColor = Color.White.copy(LocalContentAlpha.current)
        )
}

private data class ChildTransitionState(
    val _contentAlpha: State<Float>,
    val _listItemOffsetY: State<Dp>,
    val transition: Transition<AnimationState>,
) {
    val contentAlpha by _contentAlpha
    val listItemOffsetY by _listItemOffsetY
}

@OptIn(ExperimentalTransitionApi::class)
@Composable
private fun Transition<ScreenAnimationState>.headerTransitionState(): HeaderTransitionState {

    val transition = createChildTransition(label = "Header transition") {
        when (it) {
            Begin -> Start
            Half, Finish -> End
        }
    }

    val colorsss = MaterialTheme.colors

    val endColor = remember {
        colorsss.surface
    }

    val textFieldColors = TextFieldDefaults.textFieldColors()

    val textBackground = transition.animateColor(label = "Text background color") {
        when (it) {
            Start -> textFieldColors.backgroundColor(enabled = true).value
            End -> endColor
        }
    }

    // grab color for unfocused state
    val indicatorColor = textFieldColors.indicatorColor(
        enabled = true,
        isError = false,
        interactionSource = MutableInteractionSource()
    )

    val horizontalPadding = transition.animateDp(label = "Header padding") {
        when (it) {
            Start -> 16.dp
            End -> 0.dp
        }
    }

    val cornerRadius = transition.animateDp(label = "Header corner radius") {
        when (it) {
            Start -> 8.dp
            End -> 0.dp
        }
    }

    return remember(transition) {
        HeaderTransitionState(
            textBackground,
            indicatorColor,
            horizontalPadding,
            cornerRadius,
            transition
        )
    }
}

@OptIn(ExperimentalTransitionApi::class)
@Composable
private fun Transition<ScreenAnimationState>.childTransitionState(): ChildTransitionState {
    val childTransition = createChildTransition(label = "Suggestions child transition") {
        when (it) {
            Begin, Half -> Start
            Finish -> End
        }
    }

    val contentAlpha = childTransition.animateFloat(label = "Child content alpha") {
        when (it) {
            Start -> 0f
            End -> 1f
        }
    }

    val listOffsetY = childTransition.animateDp(label = "Child offset y") {
        when (it) {
            Start -> 16.dp
            End -> 0.dp
        }
    }

    return remember(childTransition) {
        ChildTransitionState(contentAlpha, listOffsetY, childTransition)
    }
}

private infix fun <T> Transition<T>.transitionedTo(
    state: T
): Boolean = targetState == currentState && targetState == state && !isRunning
