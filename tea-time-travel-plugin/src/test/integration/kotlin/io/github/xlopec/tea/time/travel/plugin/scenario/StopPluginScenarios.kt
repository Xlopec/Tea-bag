package io.github.xlopec.tea.time.travel.plugin.scenario

import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performClick
import io.github.xlopec.tea.core.Initializer
import io.github.xlopec.tea.core.toStatesComponent
import io.github.xlopec.tea.time.travel.plugin.data.*
import io.github.xlopec.tea.time.travel.plugin.environment.TestEnvironment
import io.github.xlopec.tea.time.travel.plugin.environment.TestPlatform
import io.github.xlopec.tea.time.travel.plugin.feature.component.ui.ComponentTabTag
import io.github.xlopec.tea.time.travel.plugin.feature.component.ui.ComponentTag
import io.github.xlopec.tea.time.travel.plugin.integration.Message
import io.github.xlopec.tea.time.travel.plugin.integration.PluginComponent
import io.github.xlopec.tea.time.travel.plugin.model.*
import io.github.xlopec.tea.time.travel.plugin.ui.Plugin
import io.github.xlopec.tea.time.travel.plugin.ui.ServerActionButtonTag
import io.github.xlopec.tea.time.travel.plugin.util.invoke
import io.github.xlopec.tea.time.travel.plugin.util.setContentWithEnv
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.flow.MutableSharedFlow
import org.junit.Rule
import org.junit.Test

class StopPluginScenarios {

    @get:Rule
    val rule = createComposeRule()

    /**
     * Scenario:
     * * Running plugin with some components attached
     * * Check components rendered
     * * User clicks on `stop` button
     * * Check components are still present in the composition
     */
    @Test
    fun `test info view displayed when user stops plugin server given non-empty started state`() = rule {
        val debugger = Debugger(
            persistentMapOf(
                TestComponentId to DebuggableComponent(
                    TestComponentId,
                    TestUserValue,
                    persistentListOf(OriginalSnapshot(TestSnapshotMeta1, null, TestUserValue, CollectionWrapper(TestUserValue)))
                )
            )
        )
        val messages = MutableSharedFlow<Message>()
        val started = State(ValidTestSettings, debugger, StartedTestServerStub)
        val environment = TestEnvironment()

        val component = PluginComponent(environment, Initializer(started))

        setContentWithEnv(environment) {
            Plugin(
                platform = TestPlatform(),
                component = component.toStatesComponent(),
                messages = messages
            )
        }

        awaitIdle()
        onNode(hasTestTag(ComponentTabTag(TestComponentId))).assertExists()
        onNode(hasTestTag(ComponentTag(TestComponentId))).assertExists()

        onNode(hasTestTag(ServerActionButtonTag)).performClick()
        // fixme assertExists should be replaced with assertIsDisplayed
        awaitIdle()
        onNode(hasTestTag(ComponentTabTag(TestComponentId))).assertExists()
        onNode(hasTestTag(ComponentTag(TestComponentId))).assertExists()
    }
}
