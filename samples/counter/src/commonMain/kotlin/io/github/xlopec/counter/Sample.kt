package io.github.xlopec.counter

import androidx.compose.runtime.AbstractApplier
import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.Composition
import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.snapshots.SnapshotStateList
import io.github.xlopec.counter.SnapshotManager.ensureStarted
import io.github.xlopec.counter.SnapshotManager.started
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Clock
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.ExperimentalTime

// Provided we have a tree with a node base type like the following
abstract class Node {
    val children = mutableListOf<Node>()

    abstract fun render()
}

// We would implement an Applier class like the following, which would teach compose how to
// manage a tree of Nodes.
class NodeApplier(root: Node) : AbstractApplier<Node>(root) {
    override fun insertTopDown(index: Int, instance: Node) {
        current.children.add(index, instance)
    }

    override fun insertBottomUp(index: Int, instance: Node) {
        // Ignored as the tree is built top-down.
    }

    override fun remove(index: Int, count: Int) {
        current.children.remove(index, count)
    }

    override fun move(from: Int, to: Int, count: Int) {
        current.children.move(from, to, count)
    }

    override fun onClear() {
        root.children.clear()
    }
}

// A function like the following could be created to create a composition provided a root Node.
fun Node.setContent(parent: CompositionContext, content: @Composable () -> Unit): Composition {
    return Composition(NodeApplier(this), parent).apply { setContent(content) }
}

// assuming we have Node sub-classes like "TextNode" and "GroupNode"
class TextNode : Node() {
    var text: String = ""
    var onClick: () -> Unit = {}
    override fun render() {
        println("Text(value=${text})")
    }


}

class GroupNode : Node() {
    override fun render() {
        println("Group:")
        children.forEach {
            it.render()
        }
    }
}

// Composable equivalents could be created
@Composable
fun Text(text: String, onClick: () -> Unit = {}) {
    ComposeNode<TextNode, NodeApplier>(::TextNode) {
        set(text) { this.text = it }
        set(onClick) { this.onClick = it }
    }
}

@Composable
fun Group(content: @Composable () -> Unit) {
    ComposeNode<GroupNode, NodeApplier>(::GroupNode, {}, content)
}

// and then a sample tree could be composed:
fun runApp(root: GroupNode, parent: CompositionContext, content: @Composable () -> Unit): Composition {
    return root.setContent(parent, content)
}

@Composable
fun FirstScreenResolver(
    screen: String,
) {
    LaunchedEffect(Unit) {
        try {
            while (isActive) {
                delay(400L)
                println("$screen is running")
            }
        } finally {
            println("$screen is disposed")
        }
    }
}

@OptIn(ExperimentalTime::class)
fun main() = runBlocking {

    val stack = mutableStateListOf<Pair<String, @Composable ((String) -> Unit)>>(
        "First screen" to {
            FirstScreenResolver(it)
        },
        "Second screen" to {},
        "Third screen" to {},
        "Fourth screen" to {}
    )

    launch {
        delay(2000L)
        println("Killing")
        stack.removeAt(0)
    }

    runResolver(stack)
}

@OptIn(ExperimentalTime::class)
private suspend fun <T> runResolver(
    stack: Iterable<Pair<T, @Composable (T) -> Unit>>,
) = coroutineScope {
    val internalClock = BroadcastFrameClock()

    val recomposer = Recomposer(coroutineContext + internalClock)
    // val composition = Composition(applier, recomposer)

    launch(internalClock, CoroutineStart.UNDISPATCHED) {

        recomposer.runRecomposeAndApplyChanges()

    }

    launch {
        ensureStarted()
    }

    val root = GroupNode()
    runApp(root, recomposer) {


        for ((screen, runner) in stack) {
            println("for $screen")
            key(screen) {
                runner(screen)
            }
        }

        /*var count by remember { mutableStateOf(0) }
        Group {
            Text("Count: $count")
            Text("Increment") { count++ }
        }

        if (count % 2 == 0) {
            Text("count divisible by 2")
        }

        LaunchedEffect(count) {
            println("Recomposing $count")
        }

        LaunchedEffect(Unit) {
            while (isActive) {
                delay(3000L)
                count++
            }
        }*/
    }

    launch {
        while (true) {
            internalClock.sendFrame(Clock.System.now().nanosecondsOfSecond.nanoseconds.inWholeNanoseconds)

            // "1000 FPS should be enough for anybody"
            // We need to yield in order for other coroutines on this dispatcher to run, otherwise
            // this is effectively a spin loop. We do a delay instead of a yield since dispatchers
            // are not required to support yield, but reasonable delay support is almost a guarantee.
            delay(1)

            root.children.forEach { it.render() }
        }
    }

    recomposer.awaitIdle()
}

/**
 * Manages registering the callback for the global snapshot states and sending an apply
 * notification for each callback checking if has not started with [started]. You typically
 * call [ensureStarted] before creating a composition. This is a slightly different implementation
 * of androidx.compose.ui.platform.GlobalSnapshotManager.
 */
@OptIn(ExperimentalAtomicApi::class)
internal object SnapshotManager {

    private val started = AtomicBoolean(false)

    /**
     * Registers an observer to global snapshot states and sends an apply notification
     * for each callback if it has not already started.
     */
    suspend fun ensureStarted() = coroutineScope {
        if (started.compareAndSet(expectedValue = false, newValue = true)) {
            val channel = Channel<Unit>(Channel.CONFLATED)
            launch {
                channel.consumeEach {
                    Snapshot.sendApplyNotifications()
                }
            }
            Snapshot.registerGlobalWriteObserver {
                channel.trySend(Unit)
            }
        }
    }
}