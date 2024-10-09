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

@file:Suppress("FunctionName")

package io.github.xlopec.reader.app

import io.github.xlopec.reader.app.command.DoLog
import io.github.xlopec.reader.app.command.DoStoreDarkMode
import io.github.xlopec.reader.app.feature.article.details.ArticleDetailsResolver
import io.github.xlopec.reader.app.feature.article.details.DoOpenInBrowser
import io.github.xlopec.reader.app.feature.article.list.ArticlesCommand
import io.github.xlopec.reader.app.feature.article.list.ArticlesResolver
import io.github.xlopec.reader.app.feature.filter.FilterCommand
import io.github.xlopec.reader.app.feature.filter.FiltersResolver
import io.github.xlopec.reader.app.feature.storage.LocalStorage
import io.github.xlopec.tea.core.effects
import io.github.xlopec.tea.core.sideEffect

public fun <Env> AppResolver(): AppResolver<Env> where
        Env : ArticlesResolver<Env>,
        Env : LocalStorage,
        Env : FiltersResolver<Env>,
        Env : ArticleDetailsResolver =
    AppResolver { snapshot, ctx ->
        snapshot.commands.forEach { cmd ->
            when (cmd) {
                is ArticlesCommand -> ctx effects { resolve(cmd) }
                is DoOpenInBrowser -> ctx sideEffect { resolve(cmd) }
                is DoStoreDarkMode -> ctx sideEffect {
                    storeDarkModePreferences(cmd.userDarkModeEnabled, cmd.syncWithSystemDarkModeEnabled)
                }
                is FilterCommand -> ctx effects { resolve(cmd) }
                is DoLog -> ctx sideEffect { log(cmd) }
                else -> error("Shouldn't get here $cmd")
            }
        }
    }

private fun log(
    cmd: DoLog,
) {
    val screen = cmd.state.screens.find { it.id == cmd.id }

    val message = """App exception occurred, 
        |screen: ${screen?.let { it::class } ?: "unknown screen"}
        |caused by command ${cmd.causedBy}
    """.trimMargin()

    println(message)
    cmd.throwable.printStackTrace()
}
