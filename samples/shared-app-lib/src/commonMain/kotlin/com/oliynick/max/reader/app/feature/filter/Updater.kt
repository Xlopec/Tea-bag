package com.oliynick.max.reader.app.feature.filter

import com.oliynick.max.reader.app.AppException
import com.oliynick.max.reader.app.ScreenId
import com.oliynick.max.reader.app.domain.Filter
import com.oliynick.max.reader.app.domain.Query
import com.oliynick.max.reader.app.domain.Source
import com.oliynick.max.reader.app.domain.SourceId
import com.oliynick.max.reader.app.misc.toException
import com.oliynick.max.reader.app.misc.toPreview
import com.oliynick.max.tea.core.component.UpdateWith
import com.oliynick.max.tea.core.component.command
import com.oliynick.max.tea.core.component.noCommand
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentHashSetOf

fun updateFilters(
    message: FilterMessage,
    state: FiltersState,
): UpdateWith<FiltersState, FilterCommand> =
    when (message) {
        is SuggestionsLoaded -> state.toLoadedUpdate(message.suggestions)
        is SourcesLoaded -> state.toPreviewUpdate(message.sources)
        is SourcesLoadException -> state.toExceptionUpdate(message.exception)
        is LoadSources -> state.toLoadUpdate(message.id)
        is ToggleSourceSelection -> state.toToggleSelectionUpdate(message.sourceId)
        is ClearSelection -> state.toClearSelectionUpdate()
        is InputChanged -> state.toInputChangedUpdate(message.query)
    }

private fun FiltersState.toToggleSelectionUpdate(
    sourceId: SourceId,
) = updatedFilter {
    copy(sources = selectToggleOperation(sourceId, sources)(sources, sourceId))
}.noCommand()

private fun selectToggleOperation(
    id: SourceId,
    sources: PersistentSet<SourceId>,
) = if (id in sources) PersistentSet<SourceId>::remove else PersistentSet<SourceId>::add

private fun FiltersState.toInputChangedUpdate(
    query: Query?,
) = updatedFilter { copy(query = query) }.noCommand()

private fun FiltersState.toClearSelectionUpdate() =
    updatedFilter { copy(sources = persistentHashSetOf()) }.noCommand()

private fun FiltersState.toLoadUpdate(
    id: ScreenId,
) = command(DoLoadSources(id))

private fun FiltersState.toExceptionUpdate(
    exception: AppException,
) = copy(sourcesState = sourcesState.toException(exception)).noCommand()

private fun FiltersState.toPreviewUpdate(
    sources: ImmutableList<Source>,
) = copy(sourcesState = sourcesState.toPreview(sources)).noCommand()

private fun FiltersState.toLoadedUpdate(
    suggestions: ImmutableList<Query>,
) = copy(suggestions = suggestions).noCommand()

private fun FiltersState.updatedFilter(
    how: Filter.() -> Filter,
) = copy(filter = filter.run(how))
