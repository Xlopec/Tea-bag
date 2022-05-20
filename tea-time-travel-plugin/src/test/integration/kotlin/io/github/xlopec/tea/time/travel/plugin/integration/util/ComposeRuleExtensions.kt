package io.github.xlopec.tea.time.travel.plugin.integration.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import io.github.xlopec.tea.time.travel.plugin.integration.environment.TestEnvironment

operator fun ComposeContentTestRule.invoke(
    body: ComposeContentTestRule.() -> Unit
) {
    apply(body)
}

fun ComposeContentTestRule.setContentWithEnv(
    content: @Composable TestEnvironment.() -> Unit
) {
    setContent {
        val environment = remember { TestEnvironment() }

        DisposableEffect(Unit) {
            registerIdlingResource(environment)
            onDispose {
                unregisterIdlingResource(environment)
            }
        }

        content(environment)
    }
}
