/*
 * MIT License
 *
 * Copyright (c) 2022. Maksym Oliinyk.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.xlopec.reader.app.feature.filter

import io.github.xlopec.reader.app.AppException
import io.github.xlopec.reader.app.FilterUpdated
import io.github.xlopec.reader.app.ScreenId
import io.github.xlopec.reader.app.domain.Filter
import io.github.xlopec.reader.app.domain.Query
import io.github.xlopec.reader.app.domain.Source
import io.github.xlopec.reader.app.domain.SourceId
import io.github.xlopec.reader.app.misc.toException
import io.github.xlopec.reader.app.misc.toPreview
import io.github.xlopec.tea.core.Update
import io.github.xlopec.tea.core.command
import io.github.xlopec.tea.core.noCommand
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentHashSetOf

fun updateFilters(
    message: FilterMessage,
    state: FiltersState,
): Update<FiltersState, FilterCommand> =
    when (message) {
        is SuggestionsLoaded -> state.toLoadedUpdate(message.suggestions)
        is SourcesLoaded -> state.toPreviewUpdate(message.sources)
        is SourcesLoadException -> state.toExceptionUpdate(message.exception)
        is LoadSources -> state.toLoadUpdate(message.id)
        is ToggleSourceSelection -> state.toToggleSelectionUpdate(message.sourceId)
        is ClearSelection -> state.toClearSelectionUpdate()
    }

fun updateFilters(
    message: FilterUpdated,
    state: FiltersState,
) = if (message.filter.type == state.filter.type) state.toFilterChangedUpdate(message.filter) else state.noCommand()

private fun FiltersState.toToggleSelectionUpdate(
    sourceId: SourceId,
) = updatedFilter {
    copy(sources = selectToggleOperation(sourceId, sources)(sources, sourceId))
}.noCommand()

private fun selectToggleOperation(
    id: SourceId,
    sources: PersistentSet<SourceId>,
) = if (id in sources) PersistentSet<SourceId>::remove else PersistentSet<SourceId>::add

private fun FiltersState.toFilterChangedUpdate(
    filter: Filter,
) = updatedFilter { filter }.noCommand()

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
