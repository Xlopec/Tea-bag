/*
 * Copyright (C) 2019 Maksym Oliinyk.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("unused", "MemberVisibilityCanBePrivate", "FunctionName")

package com.oliynick.max.elm.core.actor

import com.oliynick.max.elm.core.component.*
import com.oliynick.max.elm.core.loop.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.flow.asFlow
import kotlin.coroutines.CoroutineContext

/**
 * Component is one of the main parts of the [ELM architecture](https://guide.elm-lang.org/architecture/). Component (Runtime)
 * is a stateful part of the application responsible for a specific feature.
 *
 * Conceptually component is a triple [message][M], [command][C], [state][S] operated by pure [update][Update] and impure [resolver][Resolver]
 * functions. Each component accepts flow of [messages][M] and produces flow of [states][S] triggered by that messages.
 * Components can be bound to each other to produce new, more complex components
 *
 * Note that the resulting function always returns the last state value to its subscribers
 *
 * Component's behaviour can be configured by passing corresponding implementations of [resolver] and [update] functions
 *
 * @receiver scope where the component should be placed
 * @param initializer initializer to supply initial args to the component
 * @param resolver function to resolve effects
 * @param update pure function to compute states and effects to be resolved
 * @param M incoming messages
 * @param S state of the component
 * @param C commands to be executed
 * @return configured instance of [Component]
 */
fun <M : Any, C : Any, S : Any> CoroutineScope.component(initializer: Initializer<S, C>,
                                                         resolver: Resolver<C, M>,
                                                         update: Update<M, S, C>,
                                                         interceptor: Interceptor<M, S, C> = ::emptyInterceptor): Component<M, S> {

    val (messages, states) = actorComponent(initializer, resolver, update, interceptor)

    return newComponent(states, messages)
}

/**
 * Component is one of the main parts of the [ELM architecture](https://guide.elm-lang.org/architecture/). Component (Runtime)
 * is a stateful part of the application responsible for a specific feature.
 *
 * Conceptually component is a triple [message][M], [command][C], [state][S] operated by pure [update][Update] and impure [resolver][Resolver]
 * functions. Each component accepts flow of [messages][M] and produces flow of [states][S] triggered by that messages.
 * Components can be bound to each other to produce new, more complex components
 *
 * Note that the resulting function always returns the last state value to its subscribers
 *
 * Component's behaviour can be configured by passing corresponding implementations of [resolver] and [update] functions
 *
 * @receiver scope where the component should be placed
 * @param initialState initial state of the component
 * @param resolver function to resolve effects
 * @param update pure function to compute states and effects to be resolved
 * @param initialCommands initial set of commands to execute
 * @param M incoming messages
 * @param S state of the component
 * @param C commands to be executed
 * @return configured instance of [Component]
 */
fun <M : Any, C : Any, S : Any> CoroutineScope.component(initialState: S,
                                                         resolver: Resolver<C, M>,
                                                         update: Update<M, S, C>,
                                                         interceptor: Interceptor<M, S, C> = ::emptyInterceptor,
                                                         vararg initialCommands: C): Component<M, S> {

    @Suppress("RedundantSuspendModifier")
    suspend fun loader() = initialState to setOf(*initialCommands)

    return component(::loader, resolver, update, interceptor)
}

private fun <M, C, S> CoroutineScope.actorComponent(initializer: Initializer<S, C>,
                                                     resolver: Resolver<C, M>,
                                                     update: Update<M, S, C>,
                                                     interceptor: Interceptor<M, S, C>): ComponentInternal<M, S> {

    val statesChannel = BroadcastChannel<S>(Channel.CONFLATED)

    @UseExperimental(ObsoleteCoroutinesApi::class)
    return this@actorComponent.actor<M>(coroutineContext.jobOrDefault(),
        onCompletion = statesChannel::close) {

        loop(initializer, Dependencies(initializer, resolver, update, interceptor), channel, statesChannel)

    } to statesChannel.asFlow()
}

private fun CoroutineContext.jobOrDefault(): Job = this[Job] ?: Job()