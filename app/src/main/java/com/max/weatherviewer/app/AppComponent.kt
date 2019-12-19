@file:Suppress("FunctionName")

package com.max.weatherviewer.app

import com.max.weatherviewer.app.env.Environment
import com.max.weatherviewer.screens.feed.FeedLoading
import com.max.weatherviewer.screens.feed.LoadCriteria
import com.oliynick.max.elm.core.actor.Component
import com.oliynick.max.elm.core.component.Component
import com.oliynick.max.elm.core.component.Env
import com.oliynick.max.elm.core.component.androidLogger
import com.oliynick.max.elm.time.travel.Component
import com.oliynick.max.elm.time.travel.URL
import protocol.ComponentId
import java.util.*

fun Environment.appComponent(): Component<Message, State> {

    suspend fun resolve(command: Command) = this.resolve(command)

    fun update(message: Message, state: State) = this.update(message, state)

    val initScreen = FeedLoading(
        UUID.randomUUID(),
        LoadCriteria.Query("android")
    )

    // todo state persistence
    val componentDependencies = Env(
        State(initScreen),
        ::resolve,
        ::update,
        LoadByCriteria(
            initScreen.id,
            initScreen.criteria
        )
    ) {
        interceptor = androidLogger("News Reader App")
    }

    if (false && isDebug) {

        return Component(ComponentId("News Reader App"), componentDependencies, URL(host = "10.0.2.2")) {
            serverSettings {

                /*converters {
                    +URLConverter
                    +PersistentListConverter
                }*/
            }
        }
    }

    return Component(componentDependencies)

}

/*private fun toRef(s: Screen, converters: Converters): Value<*> = s.toValue(converters)

private object PersistentListConverter : Converter<PersistentList<*>, CollectionWrapper> {

    override fun from(v: CollectionWrapper, converters: Converters): PersistentList<*> {
        return v.value.map { e -> e.fromValue(converters) }.toPersistentList()
    }

    override fun to(t: PersistentList<*>, converters: Converters): CollectionWrapper {
        return CollectionWrapper(t.map { e -> e.toValue(converters) })
    }

}

private object URLConverter : Converter<URL, StringWrapper> {

    override fun from(v: StringWrapper, converters: Converters): URL? = URL(v.value)

    override fun to(t: URL, converters: Converters): StringWrapper =
        wrap(t.toExternalForm())

}*/
