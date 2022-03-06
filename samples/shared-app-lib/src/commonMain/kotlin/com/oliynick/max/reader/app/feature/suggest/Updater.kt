package com.oliynick.max.reader.app.feature.suggest

import com.oliynick.max.reader.app.misc.toPreview
import com.oliynick.max.tea.core.component.UpdateWith
import com.oliynick.max.tea.core.component.command
import com.oliynick.max.tea.core.component.noCommand
import kotlinx.collections.immutable.persistentHashSetOf

fun updateSuggestions(
    message: SuggestMessage,
    state: SuggestState
): UpdateWith<SuggestState, SuggestCommand> =
    when(message) {
        is SuggestionsLoaded -> state.copy(suggestions = message.suggestions).noCommand()
        is SourcesLoaded -> state.copy(sources = state.sources.toPreview(message.sources)).noCommand()
        is LoadSources -> state command DoLoadSources(message.id)
        is ToggleSourceSelection -> {

            val filter = state.filter

            val upd = if (message.sourceId in filter.sources) {
                filter.copy(sources = filter.sources.remove(message.sourceId))
            } else {
                filter.copy(sources = filter.sources.add(message.sourceId))
            }

            state.copy(filter = upd).noCommand()
        }
        is ClearSelection -> state.copy(filter = state.filter.copy(sources = persistentHashSetOf())).noCommand()
        is InputChanged -> state.copy(filter = state.filter.copy(input = message.input)).noCommand()
    }
