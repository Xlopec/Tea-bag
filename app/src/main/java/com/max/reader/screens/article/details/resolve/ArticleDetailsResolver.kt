package com.max.reader.screens.article.details.resolve

import com.max.reader.app.ArticleDetailsCommand
import com.max.reader.app.Message

interface ArticleDetailsResolver<Env> {

    suspend fun Env.resolve(command: ArticleDetailsCommand): Set<Message>

}
