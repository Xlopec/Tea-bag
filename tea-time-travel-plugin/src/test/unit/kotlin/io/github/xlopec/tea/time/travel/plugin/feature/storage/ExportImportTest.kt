package io.github.xlopec.tea.time.travel.plugin.feature.storage

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import io.github.xlopec.tea.time.travel.plugin.data.TestAppStateValue
import io.github.xlopec.tea.time.travel.plugin.data.TestSnapshotId1
import io.github.xlopec.tea.time.travel.plugin.data.TestSnapshotId2
import io.github.xlopec.tea.time.travel.plugin.data.TestTimestamp1
import io.github.xlopec.tea.time.travel.plugin.data.TestTimestamp2
import io.github.xlopec.tea.time.travel.plugin.data.TestUserValue
import io.github.xlopec.tea.time.travel.plugin.model.Filter
import io.github.xlopec.tea.time.travel.plugin.model.FilterOption
import io.github.xlopec.tea.time.travel.plugin.model.CollectionWrapper
import io.github.xlopec.tea.time.travel.plugin.model.DebuggableComponent
import io.github.xlopec.tea.time.travel.plugin.model.Null
import io.github.xlopec.tea.time.travel.plugin.model.OriginalSnapshot
import io.github.xlopec.tea.time.travel.plugin.model.SnapshotMeta
import io.github.xlopec.tea.time.travel.plugin.model.toFiltered
import io.github.xlopec.tea.time.travel.plugin.model.updateFilter
import io.github.xlopec.tea.time.travel.protocol.ComponentId
import kotlin.test.assertEquals
import kotlinx.collections.immutable.persistentListOf
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ExportImportSerializationTest {

    @Test
    fun `when serialize and deserialize a debug session, it's restored properly`() {
        val gson = GsonBuilder().serializeNulls().create()

        val snapshotWithoutMessage = OriginalSnapshot(
            meta = SnapshotMeta(
                id = TestSnapshotId1,
                timestamp = TestTimestamp1
            ),
            message = null,
            state = TestAppStateValue,
            commands = CollectionWrapper(listOf(TestUserValue, TestUserValue, TestUserValue))
        )

        val snapshotWithNullMessage = OriginalSnapshot(
            meta = SnapshotMeta(
                id = TestSnapshotId2,
                timestamp = TestTimestamp2
            ),
            message = Null,
            state = TestAppStateValue,
            commands = CollectionWrapper(listOf(TestUserValue, TestUserValue, TestUserValue))
        )
        // null and Null are different values and should be serialized-deserialized accordingly
        val state = DebuggableComponent(
            id = TestComponentId,
            state = TestAppStateValue,
            filter = Filter.new("filter", FilterOption.WORDS, ignoreCase = true),
            snapshots = persistentListOf(snapshotWithoutMessage, snapshotWithNullMessage),
            filteredSnapshots = persistentListOf(snapshotWithoutMessage.toFiltered())
        )

        val expectedJsonObject = state.toJsonObject()
        val actualJsonObject = gson.fromJson(gson.toJson(expectedJsonObject), JsonObject::class.java)
        // the following assert should always hold: `deserialize(serialize(t)) == t`
        assertEquals(expectedJsonObject, actualJsonObject)

        val actualDebugState = actualJsonObject.toComponentDebugState()
        // For restored session filter should be reset
        val expectedDebugState = state.updateFilter(Filter.empty())

        assertEquals(actualDebugState, expectedDebugState)
    }
}

private val TestComponentId = ComponentId("some id")
