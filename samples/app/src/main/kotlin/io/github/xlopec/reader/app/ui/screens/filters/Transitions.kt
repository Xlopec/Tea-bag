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

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.ExperimentalTransitionApi
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.createChildTransition
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextFieldColors
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.xlopec.reader.app.ui.screens.filters.ChildAnimationState.End
import io.github.xlopec.reader.app.ui.screens.filters.ChildAnimationState.Start
import io.github.xlopec.reader.app.ui.screens.filters.ScreenAnimationState.Begin
import io.github.xlopec.reader.app.ui.screens.filters.ScreenAnimationState.Finish
import io.github.xlopec.reader.app.ui.screens.filters.ScreenAnimationState.Half

enum class ScreenAnimationState {
    Begin, Half, Finish
}

enum class ChildAnimationState {
    Start, End,
}

data class HeaderTransitionState(
    val _textBackground: State<Color>,
    val _indicatorColor: State<Color>,
    val _horizontalPadding: State<Dp>,
    val _cornerRadius: State<Dp>,
    val _elevation: State<Dp>,
    val transition: Transition<ChildAnimationState>,
) {
    private val indicatorColor by _indicatorColor
    private val textBackground by _textBackground
    val horizontalPadding by _horizontalPadding
    val cornerRadius by _cornerRadius
    val elevation by _elevation

    @Composable
    fun textFieldTransitionColors(): TextFieldColors =
        TextFieldDefaults.textFieldColors(
            backgroundColor = textBackground,
            focusedIndicatorColor = indicatorColor,
            unfocusedIndicatorColor = indicatorColor,
            disabledIndicatorColor = indicatorColor,
            errorIndicatorColor = indicatorColor
        )
}

data class ChildTransitionState(
    val _contentAlpha: State<Float>,
    val _listItemOffsetY: State<Dp>,
    val transition: Transition<ChildAnimationState>,
) {
    val contentAlpha by _contentAlpha
    val listItemOffsetY by _listItemOffsetY
}

@OptIn(ExperimentalTransitionApi::class)
@Composable
fun Transition<ScreenAnimationState>.headerTransitionState(): HeaderTransitionState {

    val transition = createChildTransition(label = "Header transition") {
        when (it) {
            Begin -> Start
            Half, Finish -> End
        }
    }

    val textFieldColors = TextFieldDefaults.textFieldColors()

    val textBackground = transition.animateColor(label = "Text background color") {
        when (it) {
            Start -> textFieldColors.backgroundColor(enabled = true).value
            End -> MaterialTheme.colors.background
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

    val elevation = transition.animateDp(label = "Header elevation") {
        when (it) {
            Start -> 1.dp
            End -> 0.dp
        }
    }

    return remember(transition) {
        HeaderTransitionState(
            textBackground,
            indicatorColor,
            horizontalPadding,
            cornerRadius,
            elevation,
            transition
        )
    }
}

@OptIn(ExperimentalTransitionApi::class)
@Composable
fun Transition<ScreenAnimationState>.childTransitionState(): ChildTransitionState {
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
