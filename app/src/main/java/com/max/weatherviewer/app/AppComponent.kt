package com.max.weatherviewer.app

import com.max.weatherviewer.Command
import com.max.weatherviewer.DoLoadArticles
import com.max.weatherviewer.home.Loading
import com.oliynick.max.elm.core.actor.component
import com.oliynick.max.elm.core.component.Component
import com.oliynick.max.elm.core.component.androidLogger
import com.oliynick.max.elm.time.travel.GsonConverter
import com.oliynick.max.elm.time.travel.debugComponent
import kotlinx.coroutines.CoroutineScope
import protocol.*
import java.net.URL

object URLConverter : Converter<URL, StringWrapper> {

    override fun from(v: StringWrapper, converters: Converters): URL? = URL(v.value)

    override fun to(t: URL, converters: Converters): StringWrapper =
        wrap(t.toExternalForm())

}

fun CoroutineScope.appComponent(dependencies: Dependencies): Component<Message, State> {

    suspend fun resolver(command: Command) = AppResolver.resolve(dependencies, command)
    // todo state persistence

    return if (dependencies.isDebugBuild) {

        debugComponent(
            ComponentId("News Reader App"), GsonConverter,
            com.oliynick.max.elm.core.component.Dependencies(
                State(Loading),
                ::resolver,
                AppUpdater::update,
                androidLogger("News Reader App"),
                DoLoadArticles("bitcoin")
            )
        ) {
            serverSettings {
                url = URL("http://10.0.2.2:8080")

                converters {
                    +URLConverter
                }
            }
        }
    } else component(State(Loading), ::resolver, AppUpdater::update, DoLoadArticles("bitcoin")) {

    }
}
