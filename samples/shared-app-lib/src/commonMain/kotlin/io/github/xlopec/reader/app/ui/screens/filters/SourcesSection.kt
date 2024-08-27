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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import io.github.xlopec.reader.app.MessageHandler
import io.github.xlopec.reader.app.ScreenId
import io.github.xlopec.reader.app.feature.filter.ClearSelection
import io.github.xlopec.reader.app.feature.filter.FiltersState
import io.github.xlopec.reader.app.feature.filter.LoadSources
import io.github.xlopec.reader.app.feature.filter.ToggleSourceSelection
import io.github.xlopec.reader.app.misc.Exception
import io.github.xlopec.reader.app.misc.Idle
import io.github.xlopec.reader.app.misc.Loadable
import io.github.xlopec.reader.app.misc.Loading
import io.github.xlopec.reader.app.misc.LoadingNext
import io.github.xlopec.reader.app.misc.Refreshing
import io.github.xlopec.reader.app.model.Source
import io.github.xlopec.reader.app.ui.misc.RowMessage
import io.github.xlopec.tea.data.Url
import io.github.xlopec.tea.data.toExternalValue

@Composable
internal fun SourcesSection(
    state: FiltersState,
    id: ScreenId,
    modifier: Modifier,
    sources: Loadable<Source>,
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
            FiltersSubtitle(
                modifier = Modifier,
                text = "Sources"
            )

            Spacer(Modifier.weight(1f))

            val filtersCount = state.filter.sources.size.toUInt()

            AnimatedVisibility(visible = filtersCount > 0U) {
                ClearSelectionButton(
                    filtersCount = filtersCount
                ) { handler(ClearSelection(id)) }
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

            Loading, Refreshing, Idle -> {
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
                        .alpha(alpha = childTransitionState.contentAlpha)
                        .offset(y = childTransitionState.listItemOffsetY),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    userScrollEnabled = sources.data.isNotEmpty(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                ) {

                    when {
                        loadable == Idle && sources.data.isEmpty() -> emptySourceItems(
                            onClick = { handler(LoadSources(id)) }
                        )

                        loadable == Idle -> sourceItems(
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
    state: FiltersState,
    sources: List<Source>,
    onClick: (Source) -> Unit,
) {
    items(sources, { it.id.value }) { source ->
        SourceItem(
            painter = source.logo.toAsyncImagePainter(),
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
                    imageVector = Icons.Default.Check,
                    contentDescription = contentDescription
                )
            }
        }
    }
}

@Composable
private fun ClearSelectionButton(
    filtersCount: UInt,
    onClick: () -> Unit,
) {
    Text(
        modifier = Modifier
            .clickable(onClick = onClick),
        text = if (filtersCount > 0U) "Clear ($filtersCount)" else "Clear",
        style = MaterialTheme.typography.subtitle2
    )
}

private val SourceImageSize = 60.dp

@Composable
private fun Url.toAsyncImagePainter() = rememberAsyncImagePainter(
    ImageRequest.Builder(LocalPlatformContext.current)
        .data(toExternalValue())
        .crossfade(true)
        .build()
)
