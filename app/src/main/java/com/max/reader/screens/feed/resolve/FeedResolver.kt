package com.max.reader.screens.feed.resolve

import com.max.reader.app.FeedCommand
import com.max.reader.app.Message

interface FeedResolver<Env> {

    suspend fun Env.resolve(command: FeedCommand): Set<Message>

}