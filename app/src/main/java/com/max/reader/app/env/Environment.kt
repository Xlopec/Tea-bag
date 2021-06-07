/*
 * MIT License
 *
 * Copyright (c) 2021. Maksym Oliinyk.
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

@file:Suppress("FunctionName")

package com.max.reader.app.env

import android.app.Application
import com.max.reader.BuildConfig.DEBUG
import com.max.reader.app.AppModule
import com.max.reader.app.env.storage.Gson
import com.max.reader.app.env.storage.HasGson
import com.max.reader.app.env.storage.local.LocalStorage
import com.max.reader.app.env.storage.network.ArticleAdapters
import com.max.reader.app.env.storage.network.NewsApi
import com.max.reader.app.resolve.CommandTransport
import com.max.reader.app.resolve.HasCommandTransport
import com.max.reader.app.update.AppNavigation
import com.max.reader.screens.article.details.ArticleDetailsModule
import com.max.reader.screens.article.list.ArticlesModule
import kotlinx.coroutines.CoroutineScope

interface Environment :
    AppModule<Environment>,
    ArticlesModule<Environment>,
    ArticleDetailsModule<Environment>,
    HasCommandTransport,
    HasAppContext,
    NewsApi<Environment>,
    LocalStorage,
    HasGson,
    AppNavigation,
    CoroutineScope

@Suppress("FunctionName")
fun Environment(
    application: Application,
    scope: CoroutineScope,
): Environment {

    val gson = BuildGson()

    return object : Environment,
        AppModule<Environment> by AppModule(),
        ArticlesModule<Environment> by ArticlesModule(),
        ArticleDetailsModule<Environment> by ArticleDetailsModule(),
        HasCommandTransport by CommandTransport(),
        NewsApi<Environment> by NewsApi(gson, DEBUG),
        LocalStorage by LocalStorage(application),
        HasGson by Gson(gson),
        HasAppContext by AppContext(application),
        CoroutineScope by scope {
    }
}

private fun BuildGson() =
    AppGson {
        setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

        ArticleAdapters.forEach { (cl, adapter) ->
            registerTypeAdapter(cl.java, adapter)
        }
    }
