package com.max.weatherviewer.screens.feed.update

import com.max.weatherviewer.app.Command
import com.max.weatherviewer.screens.feed.Feed
import com.max.weatherviewer.screens.feed.FeedMessage
import com.oliynick.max.tea.core.component.UpdateWith

interface FeedUpdater {
    fun update(
        message: FeedMessage,
        feed: Feed
    ): UpdateWith<Feed, Command>
}