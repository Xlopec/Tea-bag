package com.max.weatherviewer.screens.feed.resolve

import com.max.weatherviewer.app.FeedCommand
import com.max.weatherviewer.app.Message

interface FeedResolver<Env> {

    suspend fun Env.resolve(command: FeedCommand): Set<Message>

}