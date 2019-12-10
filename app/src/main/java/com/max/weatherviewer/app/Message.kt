package com.max.weatherviewer.app

import com.max.weatherviewer.home.ScreenMessage

sealed class Message

sealed class Navigation : Message()

object NavigateToFeed : Navigation()

object NavigateToFavorite : Navigation()

object NavigateToTrending : Navigation()

object Pop : Navigation()

data class ScreenMsg(
    val message: ScreenMessage
) : Message()
