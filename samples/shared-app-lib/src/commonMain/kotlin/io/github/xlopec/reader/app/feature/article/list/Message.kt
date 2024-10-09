/*
 * MIT License
 *
 * Copyright (c) 2022. Maksym Oliinyk.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.xlopec.reader.app.feature.article.list

import io.github.xlopec.reader.app.AppException
import io.github.xlopec.reader.app.ScreenId
import io.github.xlopec.reader.app.ScreenMessage
import io.github.xlopec.reader.app.model.Article
import io.github.xlopec.reader.app.model.Filter

import kotlin.jvm.JvmInline

public sealed interface ArticlesMessage : ScreenMessage {
    public val id: ScreenId?
}

public data class FilterLoaded(
    override val id: ScreenId,
    val filter: Filter,
) : ArticlesMessage

/*@JvmInline value*/
// see https://kotlinlang.org/docs/native-objc-interop.html#unsupported
public data class LoadNextArticles(
    override val id: ScreenId,
) : ArticlesMessage

/*@JvmInline value*/
// see https://kotlinlang.org/docs/native-objc-interop.html#unsupported
public data class LoadArticles(
    override val id: ScreenId,
) : ArticlesMessage

/*@JvmInline value*/
// see https://kotlinlang.org/docs/native-objc-interop.html#unsupported
public data class RefreshArticles(
    override val id: ScreenId,
) : ArticlesMessage

public data class ToggleArticleIsFavorite(
    override val id: ScreenId,
    val article: Article,
) : ArticlesMessage

public sealed interface ArticlesLoadResult : ArticlesMessage

public data class ArticlesLoaded(
    override val id: ScreenId,
    val page: Page<Article>,
) : ArticlesLoadResult

public data class ArticlesLoadException(
    override val id: ScreenId?,
    val cause: AppException,
) : ArticlesLoadResult

@JvmInline
public value class ArticleUpdated(
    public val article: Article,
) : ArticlesMessage {
    override val id: Nothing?
        get() = null
}

public data class SyncScrollPosition(
    override val id: ScreenId,
    val scrollState: ScrollState,
) : ArticlesMessage

/*@JvmInline
value*/
public data class OnShareArticle(
    val article: Article,
) : ArticlesMessage {
    override val id: Nothing?
        get() = null
}
