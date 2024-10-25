package io.github.xlopec.tea.time.travel.plugin.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import io.github.xlopec.tea.time.travel.plugin.environment.TestEnvironment
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.seconds

 operator fun ComposeContentTestRule.invoke(
     body: suspend ComposeContentTestRule.() -> Unit
) {
    runBlocking {
        withTimeout(30.seconds) {
            body()
        }
    }
}

fun ComposeContentTestRule.setTestContent(
    content: @Composable () -> Unit
) {
    setContent {
        TestTheme {
            content()
        }
    }
}

fun ComposeContentTestRule.setContentWithEnv(
    environment: TestEnvironment,
    content: @Composable () -> Unit
) {
    setTestContent {
        DisposableEffect(Unit) {
            registerIdlingResource(environment)
            onDispose {
                unregisterIdlingResource(environment)
            }
        }

        content()
    }
}
