/*
 * MIT License
 *
 * Copyright (c) 2026. Maksym Oliinyk.
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

package io.github.xlopec.tea.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween

private const val TransitionDuration = 350

// Fraction of the screen width the receding screen travels (parallax).
// iOS uses ~30%; 1/4 reads close enough and matches the prior behaviour.
private const val ParallaxDivisor = 4

/**
 * iOS-style push. The new screen slides in from the trailing edge over the
 * previous one, while the previous parallax-shifts a quarter of the way
 * toward the leading edge.
 *
 * `targetContentZIndex` is left at its default — [PredictiveBackContainer]
 * overrides it with a per-entry monotonic value so the current top stays drawn
 * above the revealed one across chained pops.
 */
public fun <T : Any> PushTransitionSpec(
    durationMillis: Int = TransitionDuration,
): AnimatedContentTransitionScope<T>.() -> ContentTransform = {
    ContentTransform(
        slideIntoContainer(
            towards = SlideDirection.Start,
            animationSpec = tween(durationMillis),
        ),
        slideOutOfContainer(
            towards = SlideDirection.Start,
            targetOffset = { it / ParallaxDivisor },
            animationSpec = tween(durationMillis),
        ),
    )
}

/**
 * iOS-style pop (button or programmatic). The current screen slides off to the
 * trailing edge while the previous returns from its parallax position.
 *
 * `targetContentZIndex` is left at its default — [PredictiveBackContainer]
 * overrides it with a per-entry monotonic value so the current top stays drawn
 * above the revealed one across chained pops.
 */
public fun <T : Any> PopTransitionSpec(
    durationMillis: Int = TransitionDuration,
): AnimatedContentTransitionScope<T>.() -> ContentTransform = {
    ContentTransform(
        slideIntoContainer(
            towards = SlideDirection.End,
            initialOffset = { it / ParallaxDivisor },
            animationSpec = tween(durationMillis),
        ),
        slideOutOfContainer(
            towards = SlideDirection.End,
            animationSpec = tween(durationMillis),
        ),
    )
}

/**
 * iOS-style predictive back (gesture-driven). Identical visually to
 * [PopTransitionSpec] but with linear easing — the slide is driven by
 * `SeekableTransitionState.seekTo(progress)` where `progress` is the finger
 * position, so any non-linear easing makes the screen run ahead of the finger.
 *
 * `targetContentZIndex` is left at its default — [PredictiveBackContainer]
 * overrides it with a per-entry monotonic value so the current top stays drawn
 * above the revealed one across chained pops.
 */
public fun <T : Any> PredictivePopTransitionSpec(
    durationMillis: Int = TransitionDuration,
): AnimatedContentTransitionScope<T>.() -> ContentTransform = {
    ContentTransform(
        slideIntoContainer(
            towards = SlideDirection.End,
            initialOffset = { it / ParallaxDivisor },
            animationSpec = tween(durationMillis, easing = LinearEasing),
        ),
        slideOutOfContainer(
            towards = SlideDirection.End,
            animationSpec = tween(durationMillis, easing = LinearEasing),
        ),
    )
}
