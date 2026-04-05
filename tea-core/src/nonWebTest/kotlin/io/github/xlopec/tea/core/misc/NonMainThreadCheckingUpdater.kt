package io.github.xlopec.tea.core.misc

import io.github.xlopec.tea.core.Updater
import io.github.xlopec.tea.core.noCommand
import kotlin.test.assertNotEquals

@Suppress("TestFunctionName")
fun <M, S> NonMainThreadCheckingUpdater(
    mainThreadName: String,
): Updater<M, S, Nothing> = { _, s ->

    val actualThreadNamePrefix = currentThreadName().replaceAfterLast('@', "")
    val mainThreadName = mainThreadName.replaceAfterLast('@', "")

    assertNotEquals(mainThreadName, actualThreadNamePrefix)

    s.noCommand()
}
