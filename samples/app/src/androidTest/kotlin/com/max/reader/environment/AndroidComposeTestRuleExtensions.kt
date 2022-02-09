package com.max.reader.test

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.max.reader.environment.TestEnvironment
import com.oliynick.max.reader.app.command.CloseApp
import kotlinx.coroutines.flow.MutableSharedFlow

operator fun <A : ComponentActivity> AndroidComposeTestRule<ActivityScenarioRule<A>, A>.invoke(
    block: AndroidComposeTestRule<ActivityScenarioRule<A>, A>.() -> Unit
) {
    apply(block)
}

fun AndroidComposeTestRule<*, *>.setTestContent(
    composable: @Composable TestEnvironment.() -> Unit
) = setContent {

    val closeCommands = remember { MutableSharedFlow<CloseApp>() }
    val environment = remember { TestEnvironment(activity.application) }

    DisposableEffect(Unit) {
        registerIdlingResource(environment)

        onDispose {
            unregisterIdlingResource(environment)
        }
    }

    environment.apply { composable() }
}