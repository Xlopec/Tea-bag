@file:Suppress("FunctionName")

package com.oliynick.max.reader.app.feature.navigation

import com.oliynick.max.reader.app.TabScreen
import com.oliynick.max.reader.app.command.Command
import com.oliynick.max.reader.app.feature.article.list.ArticlesInitialUpdate
import com.oliynick.max.reader.app.feature.article.list.FilterType
import com.oliynick.max.reader.app.feature.settings.SettingsScreen
import com.oliynick.max.tea.core.component.UpdateWith
import com.oliynick.max.tea.core.component.noCommand

internal fun TabNavigation.toTabScreen(): UpdateWith<TabScreen, Command> =
    when (this) {
        NavigateToSettings -> SettingsScreen.noCommand()
        NavigateToFeed -> ArticlesInitialUpdate(id, FilterType.Regular)
        NavigateToFavorite -> ArticlesInitialUpdate(id, FilterType.Favorite)
        NavigateToTrending -> ArticlesInitialUpdate(id, FilterType.Trending)
    }
