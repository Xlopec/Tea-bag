@file:Suppress("FunctionName")

package com.oliynick.max.reader.app

import com.oliynick.max.reader.app.command.CloseApp
import com.oliynick.max.reader.app.command.DoStoreDarkMode
import com.oliynick.max.reader.app.feature.article.details.ArticleDetailsCommand
import com.oliynick.max.reader.app.feature.article.details.ArticleDetailsResolver
import com.oliynick.max.reader.app.feature.article.list.ArticlesCommand
import com.oliynick.max.reader.app.feature.article.list.ArticlesResolver
import com.oliynick.max.reader.app.feature.storage.LocalStorage
import com.oliynick.max.reader.app.feature.suggest.SuggestCommand
import com.oliynick.max.reader.app.feature.suggest.SuggestionsResolver
import com.oliynick.max.tea.core.component.sideEffect

fun <Env> AppResolver(): AppResolver<Env> where
        Env : ArticlesResolver<Env>,
        Env : LocalStorage,
        Env : SuggestionsResolver<Env>,
        Env : ArticleDetailsResolver =
    AppResolver { command ->
        when (command) {
            is CloseApp -> setOf()
            is ArticlesCommand -> resolve(command)
            is ArticleDetailsCommand -> resolve(command)
            is DoStoreDarkMode -> command sideEffect { storeDarkModePreferences(userDarkModeEnabled, syncWithSystemDarkModeEnabled) }
            is SuggestCommand -> resolve(command)
            else -> error("Shouldn't get here $command")
        }
    }