@file:Suppress("FunctionName")

package com.oliynick.max.reader.app

import com.oliynick.max.reader.app.command.CloseApp
import com.oliynick.max.reader.app.command.Command
import com.oliynick.max.reader.app.command.DoStoreDarkMode
import com.oliynick.max.reader.app.feature.article.details.ArticleDetailsCommand
import com.oliynick.max.reader.app.feature.article.details.ArticleDetailsResolver
import com.oliynick.max.reader.app.feature.article.list.ArticlesCommand
import com.oliynick.max.reader.app.feature.article.list.ArticlesResolver
import com.oliynick.max.reader.app.feature.storage.LocalStorage
import com.oliynick.max.tea.core.component.sideEffect

fun <Env> AppResolverImpl(): AppResolver<Env> where
        Env : ArticlesResolver<Env>,
        Env : LocalStorage,
        Env : ArticleDetailsResolver =
    AppResolver { command: Command ->
        when (command) {
            is CloseApp -> error("IOS app can't get here")
            is ArticlesCommand -> resolve(command)
            is ArticleDetailsCommand -> resolve(command)
            is DoStoreDarkMode -> command sideEffect { storeIsDarkModeEnabled(enabled) }
            else -> error("can't get here")
        }
    }