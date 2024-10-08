package io.github.xlopec.tea.navigation

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import com.arkivanov.essenty.backhandler.BackEvent

private val InitialBackstackScreenOffset = 50.dp

public interface PredictiveBackAnimation {
    public val previousModifier: Modifier
    public val currentModifier: Modifier

    public suspend fun animate(event: BackEvent)
    public suspend fun reset()
    public suspend fun finishAnimation()
    public suspend fun cancelAnimation()
}

/**
 * Creates custom predictive back animation
 *
 * @param previousModifierProvider lambda to calculate Modifier for a screen in a backstack
 * @param currentModifierProvider lambda to calculate Modifier for a currently visible screen
 */
@Composable
public fun rememberPredictiveBackAnimation(
    previousModifierProvider: (progress: Float) -> Modifier,
    currentModifierProvider: (progress: Float) -> Modifier,
): PredictiveBackAnimation {
    val currentPreviousModifierProvider by rememberUpdatedState(previousModifierProvider)
    val currentCurrentModifierProvider by rememberUpdatedState(currentModifierProvider)
    return remember {
        DefaultPredictiveBackAnimation(
            previousModifierProvider = { currentPreviousModifierProvider(it) },
            currentModifierProvider = { currentCurrentModifierProvider(it) },
        )
    }
}

/**
 * Creates IOS-like predictive back animation
 *
 * @param screenWidth width occupied by a currently visible screen
 */
@Composable
public fun rememberDefaultPredictiveBackAnimation(
    screenWidth: Dp,
): PredictiveBackAnimation {
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    return remember(screenWidth, density, layoutDirection) {
        DefaultPredictiveBackAnimation(
            previousModifierProvider = { progress ->
                Modifier.graphicsLayer {
                    translationX = with(density) {
                        val startOffset = if (layoutDirection == LayoutDirection.Ltr) {
                            -InitialBackstackScreenOffset
                        } else {
                            InitialBackstackScreenOffset
                        }

                        lerp(startOffset, 0.dp, progress).toPx()
                    }
                }
            },
            currentModifierProvider = { progress ->
                Modifier.graphicsLayer {
                    translationX = with(density) { progress * screenWidth.toPx() }
                }
            },
        )
    }
}

internal class DefaultPredictiveBackAnimation(
    initialProgress: Float = 0f,
    private val previousModifierProvider: (Float) -> Modifier,
    private val currentModifierProvider: (Float) -> Modifier,
) : PredictiveBackAnimation {
    private val animatable = Animatable(initialValue = initialProgress)

    override val previousModifier: Modifier
        get() = previousModifierProvider(animatable.value)

    override val currentModifier: Modifier
        get() = currentModifierProvider(animatable.value)

    override suspend fun animate(event: BackEvent) {
        animatable.animateTo(event.progress)
    }

    override suspend fun reset() {
        animatable.snapTo(0f)
    }

    override suspend fun finishAnimation() {
        animatable.animateTo(1f)
    }

    override suspend fun cancelAnimation() {
        animatable.animateTo(0f)
    }
}
