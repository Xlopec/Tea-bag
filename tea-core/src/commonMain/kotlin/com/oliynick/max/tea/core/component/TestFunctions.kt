package com.oliynick.max.tea.core.component

import com.oliynick.max.tea.core.ShareOptions
import com.oliynick.max.tea.core.ShareStateWhileSubscribed
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

public fun foo1() {
    println("foo1")
}

public fun foo2(
    f: (Int) -> String,
) {
    println("foo2 ${f(1)}")
}

public fun <T> foo3(
    f: (T) -> String,
) {
    println("foo3 $f")
}

public suspend fun suspendingFoo() {
    coroutineScope {
        launch {
            println("hello from suspendingFoo")
        }
    }
}

public fun fooTakingSuspendingFun(
    f: suspend (String) -> Unit
) {
    println("suspendingFooTakingFun $f")
}

public suspend fun suspendingFooTakingSuspendingFun(
    f: suspend (String) -> Unit
) {
    coroutineScope {
        println("suspendingFooTakingSuspendingFun ${f("ohh suuuka")}")
    }
}

public fun io(): CoroutineDispatcher = Dispatchers.Default

public fun testScope(): CoroutineScope = CoroutineScope(Dispatchers.Default)

public fun shareStateWhileSubscribed(): ShareOptions = ShareStateWhileSubscribed

public fun someFlow(): Flow<String> = flowOf("a", "b", "c")