@file:Suppress("FunctionName")

package com.oliynick.max.reader.app.feature.article.list

import android.app.Application
import android.content.Intent
import com.oliynick.max.reader.app.domain.Article

class AndroidShareArticle(
    private val application: Application
) : ShareArticle {
    override fun share(article: Article) = application.startActivity(ShareIntent(article))
}

private fun ShareIntent(
    article: Article,
): Intent =
    Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, article.url.toExternalForm())
        type = "text/plain"
        putExtra(Intent.EXTRA_TITLE, article.title.value)
    }.let { intent ->
        Intent.createChooser(intent, null)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
