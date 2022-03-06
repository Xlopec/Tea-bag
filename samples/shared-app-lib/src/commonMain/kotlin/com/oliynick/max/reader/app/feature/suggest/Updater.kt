package com.oliynick.max.reader.app.feature.suggest

import com.oliynick.max.reader.app.feature.toPreview
import com.oliynick.max.tea.core.component.UpdateWith
import com.oliynick.max.tea.core.component.command
import com.oliynick.max.tea.core.component.noCommand

fun updateSuggestions(
    message: SuggestMessage,
    state: SuggestState
): UpdateWith<SuggestState, SuggestCommand> =
    when(message) {
        is SuggestionQueryUpdated -> updateQuery(state, message)
        is SuggestionsLoaded -> state.copy(suggestions = message.suggestions).noCommand()
        is SourcesLoaded -> state.copy(sources = state.sources.toPreview(message.sources)).noCommand()
        is LoadSources -> state command DoLoadSources(message.id)
        is ToggleSourceSelection -> {
            val news = if (state.isSelected(message.source.id)) {
                state.copy(selectedSources = state.selectedSources.remove(message.source.id))
            } else {
                state.copy(selectedSources = state.selectedSources.add(message.source.id))
            }

            news.noCommand()
        }
    }

private fun updateQuery(
    state: SuggestState,
    message: SuggestionQueryUpdated
) = state.noCommand()//copy(textFieldState = state.textFieldState.copy(), query = state.query.update(message.query)).noCommand()
