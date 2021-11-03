@file:Suppress("FunctionName")

package com.oliynick.max.reader.app

import com.oliynick.max.reader.article.details.ArticleDetailsResolver
import com.oliynick.max.reader.article.list.ArticlesResolver
import com.oliynick.max.tea.core.component.sideEffect

// fixme looks like it's android specific navigation feature, remove it for IOS
typealias CloseCommandsSink = suspend (CloseApp) -> Unit

fun <Env> AppResolverImpl(
    closeCommands: CloseCommandsSink,
): AppResolver<Env> where Env : ArticlesResolver<Env>,
                          Env : LocalStorage,
                          Env : ArticleDetailsResolver =
    AppResolver { command: Command ->
        when (command) {
            is CloseApp -> command.sideEffect { closeCommands(command) }
            is ArticlesCommand -> resolve(command)
            is ArticleDetailsCommand -> resolve(command)
            is StoreDarkMode -> command.sideEffect { storeIsDarkModeEnabled(command.isEnabled) }
        }
    }