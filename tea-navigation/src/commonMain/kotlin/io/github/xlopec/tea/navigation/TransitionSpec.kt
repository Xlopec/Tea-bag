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

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.GraphicsLayerScope

/**
 * Which side of a running transition a screen is on. [Incoming] is the screen the
 * transition is settling onto (the target); [Outgoing] is the one being left behind.
 */
public enum class ScreenRole { Incoming, Outgoing }

/**
 * A caller-owned navigation transition. It bundles the two things the generic
 * [PredictiveBackContainer] must not decide for itself:
 *
 * - [placement]: where a screen sits, as a pure function of the transition `fraction`
 *   (`0f` at the start of a segment, `1f` at the end) and the screen's [ScreenRole].
 *   Evaluated inside a [androidx.compose.ui.graphics.graphicsLayer] block, so it sets
 *   draw-phase transforms (`translationX`, `alpha`, `scaleX`, …) directly on the
 *   [GraphicsLayerScope], reading [GraphicsLayerScope.size] for container dimensions.
 * - [animationSpec]: the timing the container animates `fraction` with when it drives
 *   the transition itself (programmatic push/pop, and the settle after a released
 *   gesture). During an in-flight gesture `fraction` follows the finger, so the spec is
 *   only consulted for the machine-driven portions.
 *
 * Because the container never routes this through `AnimatedContent`'s enter/exit, the
 * whole transition is one seekable, cancellable value with no frozen off-screen state.
 */
@Immutable
public class ScreenTransition(
    public val animationSpec: AnimationSpec<Float>,
    public val placement: GraphicsLayerScope.(role: ScreenRole, fraction: Float) -> Unit,
)

// Fraction of the screen width the receding screen travels (parallax). iOS uses ~30%;
// 1/4 reads close enough and matches the prior behaviour.
private const val DefaultParallaxFraction = 0.25f

/**
 * iOS-style push. The new ([ScreenRole.Incoming]) screen slides in from the trailing
 * edge over the previous one, while the previous ([ScreenRole.Outgoing]) parallax-shifts
 * toward the leading edge.
 */
public fun PushTransitionSpec(
    parallaxFraction: Float = DefaultParallaxFraction,
    animationSpec: AnimationSpec<Float> = tween(),
): ScreenTransition = ScreenTransition(animationSpec) { role, fraction ->
    translationX = when (role) {
        ScreenRole.Incoming -> (1f - fraction) * size.width
        ScreenRole.Outgoing -> -parallaxFraction * fraction * size.width
    }
}

/**
 * iOS-style pop (button or programmatic). The current ([ScreenRole.Outgoing]) screen
 * slides off to the trailing edge while the previous ([ScreenRole.Incoming]) returns
 * from its parallax position at the leading edge.
 */
public fun PopTransitionSpec(
    parallaxFraction: Float = DefaultParallaxFraction,
    animationSpec: AnimationSpec<Float> = tween(),
): ScreenTransition = ScreenTransition(animationSpec) { role, fraction ->
    translationX = when (role) {
        ScreenRole.Incoming -> -parallaxFraction * (1f - fraction) * size.width
        ScreenRole.Outgoing -> fraction * size.width
    }
}

/**
 * iOS-style predictive back (gesture-driven). Geometrically identical to
 * [PopTransitionSpec]; the container seeks `fraction` from the finger position and uses
 * [animationSpec] only for the settle after release.
 */
public fun PredictivePopTransitionSpec(
    parallaxFraction: Float = DefaultParallaxFraction,
    animationSpec: AnimationSpec<Float> = tween(),
): ScreenTransition = PopTransitionSpec(parallaxFraction, animationSpec)
