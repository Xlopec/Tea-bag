package com.max.weatherviewer.app

import com.max.weatherviewer.Command
import com.max.weatherviewer.DoLoadArticles
import com.oliynick.max.elm.core.actor.component
import com.oliynick.max.elm.core.component.Component
import kotlinx.coroutines.CoroutineScope

fun CoroutineScope.appComponent(dependencies: Dependencies): Component<Message, State> {

    suspend fun resolver(command: Command) = AppResolver.resolve(dependencies, command)
    // todo state persistence
    return component(State(Home.initial()), ::resolver, AppUpdater::update, DoLoadArticles("bitcoin")) {

    }
}
