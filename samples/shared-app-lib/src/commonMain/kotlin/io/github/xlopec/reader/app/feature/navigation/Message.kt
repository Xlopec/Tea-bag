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

package io.github.xlopec.reader.app.feature.navigation

import io.github.xlopec.reader.app.Message
import io.github.xlopec.reader.app.ScreenId
import io.github.xlopec.reader.app.feature.settings.SettingsScreen
import io.github.xlopec.reader.app.model.Article
import io.github.xlopec.reader.app.model.Filter
import io.github.xlopec.tea.data.RandomUUID

public sealed interface Navigation : Message

public enum class Tab(public val id: ScreenId) {
    Feed(RandomUUID()),
    Favorite(RandomUUID()),
    Trending(RandomUUID()),
    Settings(RandomUUID()),
}

public data class NavigateToArticleDetails(
    val article: Article,
    val id: ScreenId = RandomUUID(),
) : Navigation

public data class NavigateToFilters(
    val id: ScreenId,
    val filter: Filter,
) : Navigation

public sealed interface TabNavigation : Navigation {
    public val id: ScreenId
    public val tab: Tab
}

public data object NavigateToFavorite : TabNavigation {
    override val id: ScreenId = RandomUUID()
    override val tab: Tab = Tab.Favorite
}

public data object NavigateToFeed : TabNavigation {
    override val id: ScreenId = RandomUUID()
    override val tab: Tab = Tab.Feed
}

public data object NavigateToSettings : TabNavigation {
    override val id: ScreenId = SettingsScreen.id
    override val tab: Tab = Tab.Settings
}

public data object NavigateToTrending : TabNavigation {
    override val id: ScreenId = RandomUUID()
    override val tab: Tab = Tab.Trending
}

public data object Pop : Navigation
