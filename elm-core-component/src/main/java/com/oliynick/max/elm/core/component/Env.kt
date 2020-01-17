@file:Suppress("unused", "FunctionName")

package com.oliynick.max.elm.core.component

@DslMarker
private annotation class DslBuilder

/**
 * Dependencies holder
 */
data class Env<M, C, S>(
    @Deprecated("will be removed", ReplaceWith("newInitializer"))
    inline val initializer: InitializerLegacy<S, C>,
    inline val resolver: Resolver<C, M>,
    inline val update: Update<M, S, C>,
    inline val interceptor: LegacyInterceptor<M, S, C>
) {

    val newInitializer: Initializer<S, C> = { initializer().let { (s, c) -> Initial(s, c) } }

    constructor(initialState: S,
                resolver: Resolver<C, M>,
                update: Update<M, S, C>,
                interceptor: LegacyInterceptor<M, S, C> = { _, _, _, _ -> },
                vararg initialCommands: C)
        : this(
        initializerLegacy(
            initialState,
            setOf(*initialCommands)
        ),
        resolver,
        update,
        interceptor
    )

    constructor(initialState: S,
                resolver: Resolver<C, M>,
                update: Update<M, S, C>,
                interceptor: LegacyInterceptor<M, S, C> = { _, _, _, _ -> },
                initialCommands: Set<C> = emptySet())

            : this(
        initializerLegacy(
            initialState,
            initialCommands
        ),
        resolver,
        update,
        interceptor
    )
}

@DslBuilder
class EnvBuilder<M, C, S>(
    @Deprecated("will be removed")
    var initializer: InitializerLegacy<S, C>,
    var resolver: Resolver<C, M>,
    var update: Update<M, S, C>,
    @ObsoleteComponentApi
    var interceptor: LegacyInterceptor<M, S, C> = { _, _, _, _ -> }
) {
    constructor(env: Env<M, C, S>) : this(
        env.initializer,
        env.resolver,
        env.update,
        env.interceptor
    )
}

fun <M, C, S> Env(
    initializer: InitializerLegacy<S, C>,
    resolver: Resolver<C, M>,
    update: Update<M, S, C>,
    config: EnvBuilder<M, C, S>.() -> Unit = {}
) = EnvBuilder(initializer, resolver, update)
    .apply(config)
    .toEnv()

fun <M, C, S> Env(
    initialState: S,
    resolver: Resolver<C, M>,
    update: Update<M, S, C>,
    vararg initialCommands: C,
    config: EnvBuilder<M, C, S>.() -> Unit = {}
) = Env(initializerLegacy(initialState, setOf(*initialCommands)), resolver, update, config)

fun <M, C, S> Env(
    initialState: S,
    resolver: Resolver<C, M>,
    update: Update<M, S, C>,
    initialCommands: Set<C>,
    config: EnvBuilder<M, C, S>.() -> Unit = {}
) = Env(initializerLegacy(initialState, initialCommands), resolver, update, config)

fun <S, C> Initializer(s: S, commands: Set<C>): Initializer<S, C> = { Initial(s, commands) }

fun <S, C> Initializer(s: S, vararg commands: C): Initializer<S, C> = Initializer(s, setOf(*commands))

fun <S, C> Initializer(s: S): Initializer<S, C> = Initializer(s, emptySet())

fun <S, C> initializerLegacy(s: S, commands: Set<C>): InitializerLegacy<S, C> = { s to commands }

fun <S, C> initializerLegacy(s: S, vararg commands: C): InitializerLegacy<S, C> =
    initializerLegacy(s, setOf(*commands))

fun <M, C, S> EnvBuilder<M, C, S>.toEnv() =
        Env(initializer, resolver, update, interceptor)
