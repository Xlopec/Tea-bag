@file:Suppress("FunctionName")

package com.oliynick.max.reader.app

import com.oliynick.max.reader.app.command.CloseApp
import com.oliynick.max.reader.app.command.DoLog
import com.oliynick.max.reader.app.command.DoStoreDarkMode
import com.oliynick.max.reader.app.feature.article.details.ArticleDetailsCommand
import com.oliynick.max.reader.app.feature.article.details.ArticleDetailsResolver
import com.oliynick.max.reader.app.feature.article.list.ArticlesCommand
import com.oliynick.max.reader.app.feature.article.list.ArticlesResolver
import com.oliynick.max.reader.app.feature.filter.FilterCommand
import com.oliynick.max.reader.app.feature.filter.FiltersResolver
import com.oliynick.max.reader.app.feature.storage.LocalStorage
import com.oliynick.max.tea.core.component.sideEffect

fun <Env> AppResolver(): AppResolver<Env> where
        Env : ArticlesResolver<Env>,
        Env : LocalStorage,
        Env : FiltersResolver<Env>,
        Env : ArticleDetailsResolver =
    AppResolver { command ->
        when (command) {
            is CloseApp -> setOf()
            is ArticlesCommand -> resolve(command)
            is ArticleDetailsCommand -> resolve(command)
            is DoStoreDarkMode -> command sideEffect { storeDarkModePreferences(userDarkModeEnabled, syncWithSystemDarkModeEnabled) }
            is FilterCommand -> resolve(command)
            is DoLog -> command sideEffect { log(this) }
            else -> error("Shouldn't get here $command")
        }
    }

private fun log(
    cmd: DoLog
) {
    val screen = cmd.state.screens.find { it.id == cmd.id }

    val message = """App exception occurred, 
        |screen: ${screen?.let { it::class } ?: "unknown screen"}
        |caused by command ${cmd.causedBy}
    """.trimMargin()

    println(message)
    cmd.throwable.printStackTrace()
}
