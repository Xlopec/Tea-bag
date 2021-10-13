package com.oliynick.max.reader.article.list

import com.oliynick.max.reader.network.NewsApiCommon
import com.oliynick.max.reader.network.Page

fun NewsApi(): NewsApi =
    object : NewsApi {

        private val impl = NewsApiCommon(
            /*HttpClient(CIO) {

                install(JsonFeature) {
                    serializer = GsonSerializer(gson)
                }

                if (debug) {
                    Logging {
                        level = LogLevel.ALL
                    }
                }
            }*/
        )

        override suspend fun fetchFromEverything(
            input: String,
            currentSize: Int,
            resultsPerPage: Int,
        ): Page = impl.fetchFromEverything(input, currentSize, resultsPerPage)

        override suspend fun fetchTopHeadlines(
            input: String,
            currentSize: Int,
            resultsPerPage: Int,
        ): Page = impl.fetchTopHeadlines(input, currentSize, resultsPerPage, "EN")

    }