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

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.TransitionState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Stable
public class PredictiveBackContainerState<T : NavStackEntry<*>> internal constructor(initial: T) {
    internal val seekableState = SeekableTransitionState(initial)
    internal var current by mutableStateOf(initial)
    internal var previous by mutableStateOf<T?>(null)
    internal var top by mutableStateOf(initial)

    // Read-only view: the container is the single owner of the seek.
    public val transitionState: TransitionState<T> get() = seekableState
    public val fraction: Float get() = seekableState.fraction
    public val gestureTarget: T? get() = previous
    public val isSettled: Boolean
        get() {
            val settled = seekableState.currentState.id
            return previous == null && settled == seekableState.targetState.id && settled == top.id
        }
}

@Composable
public fun <T : NavStackEntry<*>> rememberPredictiveBackContainerState(
    initial: T,
): PredictiveBackContainerState<T> = remember { PredictiveBackContainerState(initial) }

@Stable
public interface NavEntryScope {
    public val isSettled: Boolean
    public val animatedVisibilityScope: AnimatedVisibilityScope
}

internal class NavEntryScopeImpl(
    override val animatedVisibilityScope: AnimatedVisibilityScope,
    private val state: PredictiveBackContainerState<*>,
    private val id: Any?,
) : NavEntryScope {
    override val isSettled: Boolean
        get() = state.isSettled && state.seekableState.currentState.id == id
}
