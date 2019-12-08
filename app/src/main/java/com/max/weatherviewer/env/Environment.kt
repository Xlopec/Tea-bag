package com.max.weatherviewer.env

import com.max.weatherviewer.app.AppResolver
import com.max.weatherviewer.app.AppUpdater
import com.max.weatherviewer.app.CommandTransport
import com.max.weatherviewer.app.HasCommandTransport
import com.max.weatherviewer.home.*
import kotlinx.coroutines.CoroutineScope
import retrofit2.Retrofit

interface Environment :
    AppUpdater<Environment>,
    AppResolver<Environment>,
    HomeUpdater,
    HasCommandTransport,
    HomeResolver<Environment>,
    HasNewsApi,
    CoroutineScope {
    val isDebug: Boolean
}

@Suppress("FunctionName")
fun Environment(
    retrofit: Retrofit,
    scope: CoroutineScope,
    isDebug: Boolean
): Environment = object : Environment,
    AppUpdater<Environment> by AppUpdater(),
    HomeUpdater by LiveHomeUpdater,
    AppResolver<Environment> by AppResolver(),
    HasCommandTransport by CommandTransport(),
    HomeResolver<Environment> by HomeResolver(),
    HasNewsApi by NewsApi(retrofit),
    CoroutineScope by scope
{
    override val isDebug: Boolean = isDebug
}
