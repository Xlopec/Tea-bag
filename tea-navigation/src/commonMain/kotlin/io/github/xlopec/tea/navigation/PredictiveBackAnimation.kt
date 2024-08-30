package io.github.xlopec.tea.navigation

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.arkivanov.essenty.backhandler.BackCallback
import com.arkivanov.essenty.backhandler.BackDispatcher
import com.arkivanov.essenty.backhandler.BackEvent
import com.arkivanov.essenty.backhandler.BackEvent.SwipeEdge
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

public interface PredictiveBackAnimation {
    public val previousModifier: Modifier
    public val currentModifier: Modifier

    public suspend fun animate(event: BackEvent)
    public suspend fun reset()
    public suspend fun finishAnimation()
    public suspend fun cancelAnimation()
}

@Composable
public fun <T : NavStackEntry<*>> rememberPredictiveBackCoordinator(
    dispatcher: BackDispatcher,
    stack: NavigationStack<T>,
    animation: PredictiveBackAnimation,
    onBackComplete: (T) -> Unit,
): BackCoordinator<T> {
    val scope = rememberCoroutineScope()
    val currentOnBackComplete by rememberUpdatedState(onBackComplete)
    val coordinator = remember(animation) {
        BackCoordinator(
            stack = stack,
            scope = scope,
            animation = animation,
            onBackComplete = currentOnBackComplete,
        )
    }

    LaunchedEffect(stack.screen, stack.getOrNull(stack.lastIndex - 1)) {
        // TODO: decide how many screens we should take into account
        coordinator.stack = stack
    }

    DisposableEffect(Unit) {
        dispatcher.register(coordinator)

        onDispose {
            dispatcher.unregister(coordinator)
        }
    }

    return coordinator
}

@Composable
public fun rememberDefaultPredictiveBackAnimation(
    maxWidth: Dp,
): PredictiveBackAnimation {
    val density = LocalDensity.current
    return remember {
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

public class BackCoordinator<T : NavStackEntry<*>> internal constructor(
    stack: NavigationStack<T>,
    private val onBackComplete: (T) -> Unit,
    private val scope: CoroutineScope,
    private val animation: PredictiveBackAnimation,
) : BackCallback() {

    internal var stack: NavigationStack<T> = stack
        set(value) {
            current = value.screen
            field = value
        }

    internal var current: T by mutableStateOf(stack.screen)
        private set
    internal var previous: T? by mutableStateOf(null)
        private set

    internal val previousModifier: Modifier by animation::previousModifier
    internal val currentModifier: Modifier by animation::currentModifier

    override fun onBack() {
        println("onBack")
        val previous = previous ?: return

        scope.launch {
            animation.finishAnimation()
            onBackComplete(current)
            current = previous
            this@BackCoordinator.previous = null
            animation.reset()
        }
    }

    override fun onBackStarted(backEvent: BackEvent) {
        println("onBackStarted $backEvent")
        val previous = stack.getOrNull(stack.lastIndex - 1) ?: return

        this.previous = previous
    }

    override fun onBackProgressed(backEvent: BackEvent) {
        println("onBackProgressed $backEvent")

        previous ?: return

        scope.launch { animation.animate(backEvent) }
    }

    override fun onBackCancelled() {
        println("onBackCancelled")
        scope.launch {
            animation.cancelAnimation()
            previous = null
            animation.reset()
        }
    }
}

@Composable
public fun <T : NavStackEntry<*>> PredictiveBackContainer(
    backDispatcher: BackDispatcher,
    coordinator: BackCoordinator<T>,
    modifier: Modifier = Modifier,
    backIcon: (@Composable (progress: Float, edge: SwipeEdge) -> Unit)? = null,
    startEdgeEnabled: Boolean = true,
    endEdgeEnabled: Boolean = true,
    edgeWidth: Dp = 16.dp,
    activationOffsetThreshold: Dp = 16.dp,
    confirmationProgressThreshold: Float = 0.2F,
    onClose: (() -> Unit)? = null,
    content: @Composable (Modifier, T) -> Unit,
) {
    PredictiveBackGestureOverlay(
        modifier = modifier,
        backDispatcher = backDispatcher,
        backIcon = backIcon,
        startEdgeEnabled = startEdgeEnabled,
        endEdgeEnabled = endEdgeEnabled,
        edgeWidth = edgeWidth,
        activationOffsetThreshold = activationOffsetThreshold,
        confirmationProgressThreshold = confirmationProgressThreshold,
        onClose = onClose,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            val movableContent = remember(content) { movableContentOf(content) }
            val previous = coordinator.previous

            if (previous != null) {
                key(previous.id) {
                    movableContent(coordinator.previousModifier, previous)
                }
            }

            val current = coordinator.current

            key(current.id) {
                movableContent(coordinator.currentModifier, current)
            }
        }
    }
}
