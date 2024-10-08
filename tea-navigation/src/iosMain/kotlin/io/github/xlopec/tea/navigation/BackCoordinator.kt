package io.github.xlopec.tea.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.arkivanov.essenty.backhandler.BackCallback
import com.arkivanov.essenty.backhandler.BackDispatcher
import com.arkivanov.essenty.backhandler.BackEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Creates and configures [BackCoordinator] to be used for a given [NavigationStack]
 *
 * @param dispatcher current back dispatcher
 * @param stack current navigation stack
 * @param previousScreenFor lambda to find previous screen for currently visible screen. Return null if there is no previous screen
 * @param animation animation to use
 * @param onBackComplete lambda to invoke when back animation completes
 */
@Composable
public fun <T : NavStackEntry<*>> rememberPredictiveBackCoordinator(
    dispatcher: BackDispatcher,
    stack: NavigationStack<T>,
    previousScreenFor: (stack: NavigationStack<T>, current: T) -> T?,
    animation: PredictiveBackAnimation,
    onBackComplete: (T) -> Unit,
): BackCoordinator<T> {
    val scope = rememberCoroutineScope()
    val currentOnBackComplete by rememberUpdatedState(onBackComplete)
    val currentPreviousScreen by rememberUpdatedState(previousScreenFor)
    val coordinator = remember(animation) {
        BackCoordinator(
            stack = stack,
            previousScreen = { stack, current -> currentPreviousScreen(stack, current) },
            scope = scope,
            animation = animation,
            onBackComplete = { currentOnBackComplete(it) },
        )
    }

    LaunchedEffect(stack) {
        // TODO: decide how many screens we should take into account, for a large stack it'll fallback to equals&hashCode
        coordinator.stack = stack
    }

    DisposableEffect(dispatcher, coordinator) {
        dispatcher.register(coordinator)

        onDispose {
            dispatcher.unregister(coordinator)
        }
    }

    return coordinator
}

/**
 * [BackCallback] implementation for a [NavigationStack]
 */
public class BackCoordinator<T : NavStackEntry<*>> internal constructor(
    stack: NavigationStack<T>,
    private val previousScreen: (NavigationStack<T>, T) -> T?,
    private val onBackComplete: (T) -> Unit,
    private val scope: CoroutineScope,
    private val animation: PredictiveBackAnimation,
) : BackCallback() {

    internal var stack: NavigationStack<T> = stack
        set(value) {
            field = value
            current = value.screen
            val currentPrevious = previous

            if (currentPrevious != null) {
                previous = previousScreen(value, value.screen) ?: previous
            }
        }

    internal var current: T by mutableStateOf(stack.screen)
        private set
    internal var previous: T? by mutableStateOf(null)
        private set

    internal val previousModifier: Modifier by animation::previousModifier
    internal val currentModifier: Modifier by animation::currentModifier

    override fun onBack() {
        val previous = previous ?: return
        val current = current

        scope.launch {
            animation.finishAnimation()
            onBackComplete(current)
            this@BackCoordinator.current = previous
            this@BackCoordinator.previous = null
            animation.reset()
        }
    }

    override fun onBackStarted(backEvent: BackEvent) {
        val previous = previousScreen(stack, current) ?: return
        this.previous = previous
    }

    override fun onBackProgressed(backEvent: BackEvent) {
        previous ?: return

        scope.launch { animation.animate(backEvent) }
    }

    override fun onBackCancelled() {
        scope.launch {
            animation.cancelAnimation()
            previous = null
            animation.reset()
        }
    }
}
