@file:Suppress("FunctionName")

package com.oliynick.max.reader.app

import com.oliynick.max.reader.app.command.ArticleDetailsCommand
import com.oliynick.max.reader.app.command.ArticlesCommand
import com.oliynick.max.reader.app.command.CloseApp
import com.oliynick.max.reader.app.command.StoreDarkMode
import com.oliynick.max.reader.app.storage.LocalStorage
import com.oliynick.max.reader.article.details.ArticleDetailsResolver
import com.oliynick.max.reader.article.list.ArticlesResolver
import com.oliynick.max.tea.core.component.sideEffect

typealias CloseCommandsSink = suspend (CloseApp) -> Unit

fun <Env> AppResolverImpl(
    closeCommands: CloseCommandsSink,
): AppResolver<Env> where Env : ArticlesResolver<Env>,
                          Env : LocalStorage,
                          Env : ArticleDetailsResolver =
    AppResolver { command ->
        when (command) {
            is CloseApp -> command.sideEffect { closeCommands(command) }
            is ArticlesCommand -> resolve(command)
            is ArticleDetailsCommand -> resolve(command)
            is StoreDarkMode -> command.sideEffect { storeIsDarkModeEnabled(command.isEnabled) }
        }
    }