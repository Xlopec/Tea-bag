package com.max.reader.screens.article.list.resolve

import com.max.reader.app.ArticlesCommand
import com.max.reader.app.Message

interface ArticlesResolver<Env> {

    suspend fun Env.resolve(command: ArticlesCommand): Set<Message>

}