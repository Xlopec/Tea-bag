@file:Suppress("FunctionName")

package com.oliynick.max.reader.app

import com.oliynick.max.reader.article.details.ArticleDetailsResolver
import com.oliynick.max.reader.article.list.ArticlesResolver
import com.oliynick.max.tea.core.component.sideEffect
import kotlinx.coroutines.flow.MutableSharedFlow

fun <Env> AppResolver(
    closeCommands: MutableSharedFlow<CloseApp>,
): AppResolver<Env> where Env : ArticlesResolver<Env>,
                          Env : LocalStorage,
                          Env : ArticleDetailsResolver<Env> =
    AppResolver { command: Command ->
        when (command) {
            is CloseApp -> command.sideEffect { closeCommands.emit(command) }
            is ArticlesCommand -> resolve(command)
            is ArticleDetailsCommand -> resolve(command)
            is StoreDarkMode -> command.sideEffect { storeIsDarkModeEnabled(command.isEnabled) }
        }
    }