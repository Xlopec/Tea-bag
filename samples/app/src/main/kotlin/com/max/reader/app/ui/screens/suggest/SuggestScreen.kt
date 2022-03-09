package com.max.reader.app.ui.screens.suggest

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.icons.Icons.Default
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.QueryBuilder
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.max.reader.app.ui.misc.RowMessage
import com.max.reader.app.ui.misc.SearchHeader
import com.max.reader.app.ui.screens.article.toSearchHint
import com.max.reader.app.ui.screens.suggest.AnimationState.End
import com.max.reader.app.ui.screens.suggest.AnimationState.Start
import com.max.reader.app.ui.screens.suggest.ScreenAnimationState.*
import com.oliynick.max.reader.app.Message
import com.oliynick.max.reader.app.ScreenId
import com.oliynick.max.reader.app.domain.Source
import com.oliynick.max.reader.app.feature.article.list.FilterUpdated
import com.oliynick.max.reader.app.feature.article.list.LoadArticles
import com.oliynick.max.reader.app.feature.article.list.Query
import com.oliynick.max.reader.app.feature.navigation.Pop
import com.oliynick.max.reader.app.feature.suggest.*
import com.oliynick.max.reader.app.misc.*
import kotlinx.collections.immutable.PersistentList

private enum class ScreenAnimationState {
    Begin, Half, Finish
}

typealias MessageHandler = (Message) -> Unit

@OptIn(
    ExperimentalMaterialApi::class, ExperimentalFoundationApi::class,
    ExperimentalTransitionApi::class
)
@Composable
fun SuggestScreen(
    state: SuggestState,
    handler: MessageHandler,
) {
    var screenTransitionState by remember { mutableStateOf(Begin) }
    var closeScreen by remember { mutableStateOf(false) }
    var performSearch by remember { mutableStateOf(false) }
    val screenTransition =
        updateTransition(label = "Header transition", targetState = screenTransitionState)

    val headerTransitionState = screenTransition.headerTransitionState()
    val childTransitionState = screenTransition.childTransitionState()

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
                            horizontal = headerTransitionState.horizontalPadding,
                            vertical = 16.dp
                        )
                        .focusRequester(focusRequester),
                    inputText = state.filter.query?.value ?: "",
                    placeholderText = state.filter.type.toSearchHint(),
                    onQueryUpdate = {
                        handler(InputChanged(state.id, Query.of(it)))
                    },
                    onSearch = {
                        performSearch = true
                        closeScreen = true
                    },
                    shape = RoundedCornerShape(headerTransitionState.cornerRadius),
                    colors = headerTransitionState.textFieldTransitionColors()
                )
            }

            item {
                SourcesSection(
                    id = state.id,
                    modifier = Modifier.fillParentMaxWidth(),
                    sources = state.sourcesState,
                    childTransitionState = childTransitionState,
                    handler = handler,
                    state = state
                )
            }

            if (state.suggestions.isNotEmpty()) {
                suggestionsSection(
                    suggestions = state.suggestions,
                    childTransitionState = childTransitionState
                ) { suggestion ->
                    handler(InputChanged(state.id, suggestion))
                    performSearch = true
                    closeScreen = true
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.suggestionsSection(
    suggestions: List<Query>,
    childTransitionState: ChildTransitionState,
    onSuggestionSelected: (Query) -> Unit,
) {
    item {
        Subtitle(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp)
                .alpha(childTransitionState.contentAlpha),
            text = "Recent searches"
        )
    }

    items(suggestions, Query::value) { item ->
        SuggestionItem(
            modifier = Modifier
                .fillParentMaxWidth()
                .clickable { onSuggestionSelected(item) }
                .animateItemPlacement()
                .alpha(childTransitionState.contentAlpha)
                .padding(all = 16.dp)
                .offset(y = childTransitionState.listItemOffsetY),
            suggestion = item
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun SourcesSection(
    state: SuggestState,
    id: ScreenId,
    modifier: Modifier,
    sources: Loadable<PersistentList<Source>>,
    childTransitionState: ChildTransitionState,
    handler: MessageHandler,
) {

    Column {

        Row(
            modifier = modifier
                .padding(all = 16.dp)
                .alpha(childTransitionState.contentAlpha),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Subtitle(
                modifier = Modifier,
                text = "Sources"
            )

            Spacer(Modifier.weight(1f))

            AnimatedVisibility(visible = state.filter.sources.isNotEmpty()) {
                ClearSelectionButton(onClick = { handler(ClearSelection(id)) })
            }
        }

        when (val loadable = sources.loadableState) {
            LoadingNext -> Unit
            is Exception -> {
                RowMessage(
                    modifier = modifier.padding(horizontal = 16.dp),
                    message = loadable.th.message,
                    onClick = { handler(LoadSources(id)) }
                )
            }
            Loading, Refreshing, Preview -> {
                val infiniteTransition = rememberInfiniteTransition()
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = keyframes {
                            durationMillis = 1000
                            0.7f at 500
                        },
                        repeatMode = RepeatMode.Reverse
                    )
                )

                LazyRow(
                    modifier = modifier
                        .alpha(childTransitionState.contentAlpha)
                        .offset(y = childTransitionState.listItemOffsetY),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    userScrollEnabled = sources.data.isNotEmpty(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                ) {

                    when {
                        loadable == Preview && sources.data.isEmpty() -> emptySourceItems(
                            onClick = { handler(LoadSources(id)) }
                        )
                        loadable == Preview -> sourceItems(
                            sources = sources.data,
                            onClick = { handler(ToggleSourceSelection(id, it.id)) },
                            state = state
                        )
                        else -> shimmerSourceItems(alpha = alpha)
                    }
                }
            }
        }
    }
}

private fun LazyListScope.emptySourceItems(
    onClick: () -> Unit,
) {
    item {
        RowMessage(
            modifier = Modifier.fillParentMaxWidth(),
            message = "No sources found",
            onClick = onClick
        )
    }
}

private fun LazyListScope.shimmerSourceItems(
    alpha: Float,
) {
    repeat(10) {
        item(it) {
            SourceItem(
                modifier = Modifier.alpha(alpha),
                painter = ColorPainter(Color.Gray),
                contentDescription = null,
                onClick = {}
            )
        }
    }
}

private fun LazyListScope.sourceItems(
    state: SuggestState,
    sources: List<Source>,
    onClick: (Source) -> Unit,
) {
    items(sources, { it.id.value }) { source ->
        SourceItem(
            painter = rememberImagePainter(
                data = source.logo.toExternalForm(),
            ) {
                crossfade(true)
            },
            checked = source.id in state.filter.sources,
            contentDescription = source.name.value,
            onClick = { onClick(source) }
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun SourceItem(
    painter: Painter,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    checked: Boolean = false,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Surface(
            elevation = 8.dp,
            shape = CircleShape,
            onClick = onClick
        ) {
            Image(
                modifier = Modifier
                    .size(SourceImageSize)
                    .background(Color.White),
                contentScale = ContentScale.Crop,
                painter = painter,
                contentDescription = contentDescription
            )

            if (checked) {
                Image(
                    modifier = Modifier
                        .size(SourceImageSize)
                        .background(Color.Black.copy(alpha = 0.65f))
                        .padding(SourceImageSize * 0.15f),
                    colorFilter = ColorFilter.tint(Color.White),
                    imageVector = Default.Check,
                    contentDescription = contentDescription
                )
            }
        }
    }
}

@Composable
private fun Subtitle(
    modifier: Modifier,
    text: String,
) {
    Text(
        text = text,
        modifier = modifier,
        textAlign = TextAlign.Start,
        style = MaterialTheme.typography.subtitle1
    )
}

@Composable
private fun SuggestionItem(
    modifier: Modifier,
    suggestion: Query,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(imageVector = Default.QueryBuilder, contentDescription = null)

        Spacer(modifier = Modifier.width(8.dp))

        Text(text = suggestion.value)
    }
}

@Composable
private fun ClearSelectionButton(
    onClick: () -> Unit,
) {
    Text(
        modifier = Modifier
            .clickable(onClick = onClick),
        text = "Clear",
        style = MaterialTheme.typography.subtitle2
    )
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

    val colorsss = colors

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
    state: T,
): Boolean = targetState == currentState && targetState == state && !isRunning

private val SourceImageSize = 60.dp
