package com.max.weatherviewer.app

import com.max.weatherviewer.screens.feed.ScreenMessage

sealed class Message

sealed class Navigation : Message()

object NavigateToFeed : Navigation()

object NavigateToFavorite : Navigation()

object NavigateToTrending : Navigation()

object Pop : Navigation()

// fixme don't want to put all message definitions in the one file
data class ScreenMessageWrapper(
    val message: ScreenMessage
) : Message()
