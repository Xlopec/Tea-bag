package io.github.xlopec.tea.time.travel.plugin.scenario

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.xlopec.tea.core.Initializer
import io.github.xlopec.tea.core.toStatesComponent
import io.github.xlopec.tea.time.travel.plugin.data.StartedTestServerStub
import io.github.xlopec.tea.time.travel.plugin.data.TestComponentId1
import io.github.xlopec.tea.time.travel.plugin.data.TestSnapshotMeta1
import io.github.xlopec.tea.time.travel.plugin.data.TestUserValue
import io.github.xlopec.tea.time.travel.plugin.data.ValidTestSettings
import io.github.xlopec.tea.time.travel.plugin.environment.TestEnvironment
import io.github.xlopec.tea.time.travel.plugin.environment.TestPlatform
import io.github.xlopec.tea.time.travel.plugin.feature.component.ui.ComponentTag
import io.github.xlopec.tea.time.travel.plugin.integration.Message
import io.github.xlopec.tea.time.travel.plugin.integration.PluginComponent
import io.github.xlopec.tea.time.travel.plugin.model.CollectionWrapper
import io.github.xlopec.tea.time.travel.plugin.model.DebuggableComponent
import io.github.xlopec.tea.time.travel.plugin.model.Debugger
import io.github.xlopec.tea.time.travel.plugin.model.OriginalSnapshot
import io.github.xlopec.tea.time.travel.plugin.model.State
import io.github.xlopec.tea.time.travel.plugin.ui.Plugin
import io.github.xlopec.tea.time.travel.plugin.ui.ServerActionButtonTag
import io.github.xlopec.tea.time.travel.plugin.util.invoke
import io.github.xlopec.tea.time.travel.plugin.util.setContentWithEnv
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import org.junit.Rule
import org.junit.Test

class StopPluginScenarios {

    @get:Rule
    val rule = createComposeRule()

    /**
     * Scenario:
     * * Given a running plugin with some components attached
     * * Then check components rendered
     * * When user clicks on `stop` button
     * * Then check components are still present in the composition
     */
    @Test
    fun `test info view displayed when user stops plugin server given non-empty started state`() = rule {
        val debugger = Debugger(
            ValidTestSettings,
            persistentMapOf(
                TestComponentId1 to DebuggableComponent(
                    TestComponentId1,
                    TestUserValue,
                    persistentListOf(OriginalSnapshot(TestSnapshotMeta1, null, TestUserValue, CollectionWrapper(TestUserValue)))
                )
            )
        )
        val messages = MutableSharedFlow<Message>()
        val started = State(debugger, StartedTestServerStub)
        val scheduler = TestCoroutineScheduler()
        val environment = TestEnvironment(scope = CoroutineScope(StandardTestDispatcher(scheduler)))

        val component = PluginComponent(environment, Initializer(started))

        setContentWithEnv(environment) {
            Plugin(
                platform = TestPlatform(),
                component = component.toStatesComponent(),
                messages = messages
            )
        }

        scheduler.advanceUntilIdle()
        awaitIdle()
        onNodeWithText(TestComponentId1.value).assertIsDisplayed()
        onNodeWithTag(ComponentTag(TestComponentId1)).assertIsDisplayed()

        onNodeWithTag(ServerActionButtonTag).performClick()
        scheduler.advanceUntilIdle()
        awaitIdle()
        onNodeWithText(TestComponentId1.value).assertIsDisplayed()
        onNodeWithTag(ComponentTag(TestComponentId1)).assertIsDisplayed()
    }
}
