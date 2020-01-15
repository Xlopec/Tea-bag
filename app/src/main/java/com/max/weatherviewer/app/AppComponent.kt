@file:Suppress("FunctionName")

package com.max.weatherviewer.app

import com.max.weatherviewer.app.env.Environment
import com.max.weatherviewer.app.serialization.PersistentListSerializer
import com.max.weatherviewer.screens.feed.FeedLoading
import com.max.weatherviewer.screens.feed.LoadCriteria
import com.oliynick.max.elm.core.actor.ComponentLegacy
import com.oliynick.max.elm.core.component.ComponentLegacy
import com.oliynick.max.elm.core.component.Env
import com.oliynick.max.elm.core.component.androidLogger
import com.oliynick.max.elm.time.travel.component.ComponentLegacy
import com.oliynick.max.elm.time.travel.converter.GsonSerializer
import com.oliynick.max.elm.time.travel.component.URL
import com.oliynick.max.elm.time.travel.gson.TypeAppenderAdapterFactory
import kotlinx.collections.immutable.PersistentList
import protocol.ComponentId
import java.util.*

fun Environment.appComponent(): ComponentLegacy<Message, State> {

    suspend fun resolve(command: Command) = this.resolve(command)

    fun update(
        message: Message,
        state: State
    ) = this.update(message, state)

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

    if (isDebug) {

        return ComponentLegacy(ComponentId("News Reader App"), componentDependencies,
                               URL(host = "10.0.2.2")
        ) {
            serverSettings {
                installSerializer(AppGsonSerializer())
            }
        }
    }

    return ComponentLegacy(componentDependencies)

}

fun AppGsonSerializer() = GsonSerializer {

    registerTypeHierarchyAdapter(PersistentList::class.java, PersistentListSerializer)
    registerTypeAdapterFactory(TypeAppenderAdapterFactory)

}
