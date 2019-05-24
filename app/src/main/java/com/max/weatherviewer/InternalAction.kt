package com.max.weatherviewer

import com.max.weatherviewer.model.Weather

sealed class InternalAction {
    object FeedLoading : InternalAction()

    data class FeedLoaded(val data: Weather) : InternalAction()

    data class FeedLoadFailure(val th: Throwable) : InternalAction()
}