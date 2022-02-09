@file:Suppress("FunctionName")

package com.oliynick.max.reader.app.feature.article.list

import com.oliynick.max.reader.domain.Article
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication

object IosShareArticle : ShareArticle {
    override fun share(article: Article) {
        val viewController = UIActivityViewController(activityItems = listOf(article.url), applicationActivities = null)

        UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(
            viewControllerToPresent = viewController,
            animated = true,
            completion = null
        )
    }
}
