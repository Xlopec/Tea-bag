package com.oliynick.max.tea.core.component.internal

import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.numerics.shouldBeExactly
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

@RunWith(JUnit4::class)
class MulticastFlowTest {

    @Test
    fun `only launch flow once`() = runBlocking {
        val numLaunches = AtomicInteger(0)

        val flow = flow {
            numLaunches.incrementAndGet()
            emit("A")

            delay(5)
        }.share()

        val tasks = ArrayList<Job>()

        tasks += launch {
            flow.collect {}
        }

        tasks += launch {
            flow.collect {}
        }

        tasks.joinAll()

        numLaunches.get().shouldBeExactly(1)
    }

    @Test
    fun `receive items on all collectors`() = runBlocking {
        val itemsA = ArrayList<String>()
        val itemsB = ArrayList<String>()

        val flow = flow {
            delay(5)

            emit("A")
            emit("B")
            emit("C")
        }.share()

        val tasks = ArrayList<Job>()

        tasks += launch {
            flow.collect {
                itemsA += it
            }
        }

        tasks += launch {
            flow.collect {
                itemsB += it
            }
        }

        tasks.joinAll()

        itemsA.shouldContainExactly("A", "B", "C")
        itemsB.shouldContainExactly("A", "B", "C")
    }

    @Test
    fun `close flow after all collectors close`() = runBlocking {
        val closedCompletable = CompletableDeferred<Unit>()

        val flow = flow {
            try {
                delay(10)
                emit("A")
                suspendCancellableCoroutine<Unit> { }
            } finally {
                closedCompletable.complete(Unit)
            }
        }.share()

        val tasks = ArrayList<Job>()

        tasks += launch {
            flow.collect {
                coroutineContext.cancel()
            }
        }

        tasks += launch {
            flow.collect {
                coroutineContext.cancel()
            }
        }

        tasks.joinAll()

        withTimeoutOrNull(5_000) { closedCompletable.join() } ?: error("Flow not closed")
    }

    @Test
    fun `do not crash the whole flow if one collector throws exception`() = runBlocking {
        val receivedItems = ArrayList<String>()

        val flow = flow {
            delay(5)

            emit("A")
            emit("B")
            emit("C")
        }.shareConflated()

        val tasks = ArrayList<Job>()

        tasks += launch {
            flow.collect {
                delay(10)
                receivedItems += it
            }
        }

        tasks += launch {
            try {
                flow.collect {
                    throw IllegalStateException("Test")
                }
            } catch (e: Exception) {
            }
        }

        tasks.joinAll()

        receivedItems.shouldContainExactly("A", "B", "C")
    }

    @Test
    fun `receive exceptions on all producers`() = runBlocking {
        val receivedA = AtomicBoolean(false)
        val receivedB = AtomicBoolean(false)

        val flow = flow<String> {
            delay(5)

            throw CloneNotSupportedException()
        }.share()

        val tasks = ArrayList<Job>()

        tasks += launch {
            try {
                flow.collect {}
            } catch (e: CloneNotSupportedException) {
                receivedA.set(true)
            }
        }

        tasks += launch {
            try {
                flow.collect {}
            } catch (e: CloneNotSupportedException) {
                receivedB.set(true)
            }
        }

        tasks.joinAll()

        (receivedA.get()).shouldBeTrue()
        (receivedB.get()).shouldBeTrue()
    }

    @Test
    fun `receive last item when new collector starts collecting existing flow`() = runBlocking {
        val itemsA = ArrayList<String>()
        val itemsB = ArrayList<String>()

        val flow = flow {
            delay(5)

            emit("A")
            emit("B")
            emit("C")

            delay(15)
        }.shareConflated()

        val tasks = ArrayList<Job>()

        tasks += launch {
            flow.collect {
                itemsA += it
            }
        }

        tasks += launch {
            delay(10)
            flow.collect {
                itemsB += it
            }
        }

        tasks.joinAll()

        itemsA.shouldContainExactly("A", "B", "C")
        itemsB.shouldContainExactly("C")
    }

    @Test
    fun `do not receive conflated last item when there were no active collectors`() = runBlocking {
        val itemsA = ArrayList<String>()
        val itemsB = ArrayList<String>()

        val flow = flow {
            delay(5)

            emit("A")
            emit("B")
            emit("C")

            delay(15)
        }.shareConflated()

        flow.collect {
            itemsA += it
        }

        flow.collect {
            itemsB += it
        }

        itemsA.shouldContainExactly("A", "B", "C")
        itemsB.shouldContainExactly("A", "B", "C")
    }

}