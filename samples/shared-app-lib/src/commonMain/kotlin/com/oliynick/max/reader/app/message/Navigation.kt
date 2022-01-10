/*
 * MIT License
 *
 * Copyright (c) 2021. Maksym Oliinyk.
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

package com.oliynick.max.reader.app.message

import com.oliynick.max.entities.shared.randomUUID
import com.oliynick.max.reader.app.ScreenId
import com.oliynick.max.reader.domain.Article
import com.oliynick.max.reader.settings.SettingsState

sealed interface Navigation : Message

data class NavigateToArticleDetails(
    val article: Article,
    val id: ScreenId = randomUUID(),
) : Navigation

sealed interface TabNavigation : Navigation {
    val id: ScreenId
}

object NavigateToFavorite : TabNavigation {
    override val id: ScreenId = randomUUID()
}

object NavigateToFeed : TabNavigation {
    override val id: ScreenId = randomUUID()
}

object NavigateToSettings : TabNavigation {
    override val id: ScreenId = SettingsState.id
}

object NavigateToTrending : TabNavigation {
    override val id: ScreenId = randomUUID()
}

object Pop : Navigation
