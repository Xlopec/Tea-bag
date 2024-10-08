package io.github.xlopec.tea.navigation

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import com.arkivanov.essenty.backhandler.BackEvent

public interface PredictiveBackAnimation {
    public val previousModifier: Modifier
    public val currentModifier: Modifier

    public suspend fun animate(event: BackEvent)
    public suspend fun reset()
    public suspend fun finishAnimation()
    public suspend fun cancelAnimation()
}

@Composable
public fun rememberDefaultPredictiveBackAnimation(
    maxWidth: Dp,
): PredictiveBackAnimation {
    val density = LocalDensity.current
    return remember(maxWidth, density) {
        DefaultPredictiveBackAnimation(
            previousModifierProvider = { Modifier },
            currentModifierProvider = { progress ->
                Modifier.graphicsLayer {
                    translationX = with(density) { progress * maxWidth.toPx() }
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
