package com.oliynick.max.reader.app.feature.suggest

import com.oliynick.max.tea.core.component.UpdateWith
import com.oliynick.max.tea.core.component.noCommand

fun updateSuggestions(
    message: SuggestMessage,
    state: SuggestState
): UpdateWith<SuggestState, SuggestCommand> =
    when(message) {
        is SuggestionQueryUpdated -> updateQuery(state, message)
    }

private fun updateQuery(
    state: SuggestState,
    message: SuggestionQueryUpdated
) = state.noCommand()//copy(textFieldState = state.textFieldState.copy(), query = state.query.update(message.query)).noCommand()
