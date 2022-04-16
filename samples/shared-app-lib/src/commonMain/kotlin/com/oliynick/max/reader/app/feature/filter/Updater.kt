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

package com.oliynick.max.reader.app.feature.filter

import com.oliynick.max.reader.app.AppException
import com.oliynick.max.reader.app.ScreenId
import com.oliynick.max.reader.app.domain.Filter
import com.oliynick.max.reader.app.domain.Query
import com.oliynick.max.reader.app.domain.Source
import com.oliynick.max.reader.app.domain.SourceId
import com.oliynick.max.reader.app.misc.toException
import com.oliynick.max.reader.app.misc.toPreview
import com.oliynick.max.tea.core.Update
import com.oliynick.max.tea.core.command
import com.oliynick.max.tea.core.noCommand
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
