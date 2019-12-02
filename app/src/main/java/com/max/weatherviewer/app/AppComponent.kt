@file:Suppress("FunctionName")

package com.max.weatherviewer.app

import com.max.weatherviewer.Command
import com.max.weatherviewer.DoLoadArticles
import com.max.weatherviewer.home.Loading
import com.oliynick.max.elm.core.actor.Component
import com.oliynick.max.elm.core.component.Component
import com.oliynick.max.elm.core.component.Env
import com.oliynick.max.elm.core.component.androidLogger
import com.oliynick.max.elm.time.travel.Component
import com.oliynick.max.elm.time.travel.URL
import kotlinx.coroutines.CoroutineScope
import protocol.*
import java.net.URL

object URLConverter : Converter<URL, StringWrapper> {

    override fun from(v: StringWrapper, converters: Converters): URL? = URL(v.value)

    override fun to(t: URL, converters: Converters): StringWrapper =
        wrap(t.toExternalForm())

}

fun CoroutineScope.AppComponent(dependencies: AppDependencies): Component<Message, State> {

    suspend fun resolver(command: Command) = AppResolver.resolve(dependencies, command)
    // todo state persistence
    val componentDependencies = Env(
        State(Loading),
        ::resolver,
        AppUpdater::update,
        DoLoadArticles("bitcoin")
    ) {
        interceptor = androidLogger("News Reader App")
    }

    if (dependencies.isDebugBuild) {

        return Component(ComponentId("News Reader App"), componentDependencies, URL(host = "10.0.2.2")) {
            serverSettings {

                converters {
                    +URLConverter
                }
            }
        }
    }

    return Component(componentDependencies)

}
