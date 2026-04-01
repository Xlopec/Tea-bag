package io.github.xlopec.tea.core.component

import io.github.xlopec.tea.core.Component
import io.github.xlopec.tea.core.Initializer
import io.github.xlopec.tea.core.invoke
import io.github.xlopec.tea.core.misc.NonMainThreadCheckingUpdater
import io.github.xlopec.tea.core.misc.TestEnv
import io.github.xlopec.tea.core.misc.currentThreadName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class ComponentUpdaterThreadingTest {

    @Test
    fun `when collecting component with specific dispatcher then updater runs on this dispatcher`() = runTest {
        // All test schedulers use 'Test worker' as a prefix, so to check if we actually switch threads, we use
        // Default dispatcher, which has a different thread name
        val mainThreadNamePrefix = async { currentThreadName() }
        val scope = CoroutineScope(Job() + Dispatchers.Default)
        val env = TestEnv<Char, String, Char>(
            initializer = Initializer(""),
            resolver = { snapshot -> contextOf<CoroutineScope>().launch { snapshot.collect { check(it.commands.isEmpty()) { "Non empty snapshot $it" } } } },
            updater = NonMainThreadCheckingUpdater(mainThreadNamePrefix.await()),
            scope = scope,
        )

        val job = scope.launch { Component(env)('a'..'d').take('d' - 'a').collect() }

        job.join()
    }
}