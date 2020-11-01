package com.max.reader.screens.feed.update

import com.max.reader.app.Command
import com.max.reader.screens.feed.Feed
import com.max.reader.screens.feed.FeedMessage
import com.oliynick.max.tea.core.component.UpdateWith

interface FeedUpdater {
    fun update(
        message: FeedMessage,
        feed: Feed
    ): UpdateWith<Feed, Command>
}