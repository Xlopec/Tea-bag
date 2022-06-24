package io.github.xlopec.tea.time.travel.plugin.scenario

import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import io.github.xlopec.tea.core.Initializer
import io.github.xlopec.tea.core.toStatesComponent
import io.github.xlopec.tea.time.travel.plugin.data.*
import io.github.xlopec.tea.time.travel.plugin.environment.TestEnvironment
import io.github.xlopec.tea.time.travel.plugin.environment.TestPlatform
import io.github.xlopec.tea.time.travel.plugin.feature.component.ui.Tag
import io.github.xlopec.tea.time.travel.plugin.feature.info.InfoViewTag
import io.github.xlopec.tea.time.travel.plugin.feature.notification.AppendSnapshot
import io.github.xlopec.tea.time.travel.plugin.integration.Message
import io.github.xlopec.tea.time.travel.plugin.integration.PluginComponent
import io.github.xlopec.tea.time.travel.plugin.model.*
import io.github.xlopec.tea.time.travel.plugin.ui.Plugin
import io.github.xlopec.tea.time.travel.plugin.util.invoke
import io.github.xlopec.tea.time.travel.plugin.util.setContentWithEnv
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.flow.MutableSharedFlow
import org.junit.Rule
import org.junit.Test

class ComponentScenarios {

    @get:Rule
    val rule = createComposeRule()

    /**
     * Scenario:
     * * Running plugin state
     * * Component attaches with id [TestComponentId] and initial snapshot with id [TestSnapshotId1]
     * * Component emits snapshots with id [TestSnapshotId2]
     * * Check that snapshots with ids [TestSnapshotId1] and [TestSnapshotId2] exist in composition
     */
    @Test
    fun `test component snapshots are rendered when new snapshots are emitted`() = rule {
        val environment = TestEnvironment()
        val messages = MutableSharedFlow<Message>()

        val snapshotMeta1 = SnapshotMeta(TestSnapshotId1, TestTimestamp1)
        val snapshotMeta2 = SnapshotMeta(TestSnapshotId2, TestTimestamp2)

        val debugger = Debugger(
            persistentMapOf(
                ComponentDebugState(
                    TestComponentId,
                    persistentListOf(
                        OriginalSnapshot(
                            snapshotMeta1,
                            StringWrapper("message"),
                            TestUserValue,
                            CollectionWrapper()
                        )
                    ),
                    persistentListOf(
                        FilteredSnapshot(
                            snapshotMeta1,
                            StringWrapper("message"),
                            TestUserValue,
                            CollectionWrapper()
                        )
                    ),
                    TestUserValue
                )
            )
        )
        setContentWithEnv(environment) {
            Plugin(
                platform = TestPlatform(),
                component = PluginComponent(
                    environment,
                    Initializer(State(ValidTestSettings, debugger, StartedTestServerStub))
                ).toStatesComponent(),
                messages = messages
            )
        }
        // fixme assertExists should be replaced with assertIsDisplayed
        onNode(hasTestTag(InfoViewTag)).assertDoesNotExist()
        onNode(hasTestTag(Tag(snapshotMeta1))).assertExists()
        onNode(hasTestTag(Tag(snapshotMeta2))).assertDoesNotExist()

        messages.emit(
            AppendSnapshot(
                TestComponentId,
                snapshotMeta2,
                StringWrapper("nullify"),
                TestUserValue,
                TestAppStateValue,
                CollectionWrapper()
            )
        )
        awaitIdle()

        onNode(hasTestTag(Tag(snapshotMeta1))).assertExists()
        onNode(hasTestTag(Tag(snapshotMeta2))).assertExists()
    }
}
