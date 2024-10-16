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

package io.github.xlopec.reader.app

import io.github.xlopec.reader.app.feature.article.details.ArticleDetailsModule
import io.github.xlopec.reader.app.feature.article.list.ArticlesModule
import io.github.xlopec.reader.app.feature.article.list.IosShareArticle
import io.github.xlopec.reader.app.feature.article.list.NewsApi
import io.github.xlopec.reader.app.feature.filter.FiltersModule
import io.github.xlopec.reader.app.feature.storage.LocalStorage
import kotlinx.coroutines.CoroutineScope

public fun Environment(
    scope: CoroutineScope,
): Environment =
    object : Environment,
        AppModule<Environment> by AppModule(),
        ArticlesModule<Environment> by ArticlesModule(IosShareArticle),
        FiltersModule<Environment> by FiltersModule(),
        ArticleDetailsModule by ArticleDetailsModule(),
        LocalStorage by LocalStorage(),
        NewsApi by NewsApi(),
        CoroutineScope by scope {
    }

public actual interface Environment :
    AppModule<Environment>,
    ArticlesModule<Environment>,
    FiltersModule<Environment>,
    ArticleDetailsModule,
    LocalStorage,
    NewsApi,
    CoroutineScope
