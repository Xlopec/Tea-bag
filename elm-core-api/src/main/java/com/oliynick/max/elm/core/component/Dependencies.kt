@file:Suppress("unused")

package com.oliynick.max.elm.core.component

@DslMarker
private annotation class DslBuilder

/**
 * Dependencies holder
 */
data class Dependencies<M, C, S>(
    inline val initializer: Initializer<S, C>,
    inline val resolver: Resolver<C, M>,
    inline val update: Update<M, S, C>,
    inline val interceptor: Interceptor<M, S, C>
) {

    constructor(initialState: S,
                resolver: Resolver<C, M>,
                update: Update<M, S, C>,
                interceptor: Interceptor<M, S, C> = { _, _, _, _ -> },
                vararg initialCommands: C)
        : this(
        initializer(
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
                interceptor: Interceptor<M, S, C> = { _, _, _, _ -> },
                initialCommands: Set<C> = emptySet())

            : this(
        initializer(
            initialState,
            initialCommands
        ),
        resolver,
        update,
        interceptor
    )
}

@DslBuilder
class DependenciesBuilder<M, C, S>(
    var initializer: Initializer<S, C>,
    var resolver: Resolver<C, M>,
    var update: Update<M, S, C>,
    var interceptor: Interceptor<M, S, C> = { _, _, _, _ -> }
) {
    constructor(dependencies: Dependencies<M, C, S>) : this(
        dependencies.initializer,
        dependencies.resolver,
        dependencies.update,
        dependencies.interceptor
    )
}

fun <M, C, S> dependencies(
    initializer: Initializer<S, C>,
    resolver: Resolver<C, M>,
    update: Update<M, S, C>,
    config: DependenciesBuilder<M, C, S>.() -> Unit = {}
) = DependenciesBuilder(initializer, resolver, update)
    .apply(config)
    .toDependencies()

fun <M, C, S> dependencies(
    initialState: S,
    resolver: Resolver<C, M>,
    update: Update<M, S, C>,
    vararg initialCommands: C,
    config: DependenciesBuilder<M, C, S>.() -> Unit = {}
) = dependencies(initializer(initialState, setOf(*initialCommands)), resolver, update, config)

fun <M, C, S> dependencies(
    initialState: S,
    resolver: Resolver<C, M>,
    update: Update<M, S, C>,
    initialCommands: Set<C>,
    config: DependenciesBuilder<M, C, S>.() -> Unit = {}
) = dependencies(initializer(initialState, initialCommands), resolver, update, config)

fun <S, C> initializer(s: S, commands: Set<C>): Initializer<S, C> = { s to commands }

fun <S, C> initializer(s: S, vararg commands: C): Initializer<S, C> =
    initializer(s, setOf(*commands))

fun <M, C, S> DependenciesBuilder<M, C, S>.toDependencies() =
        Dependencies(initializer, resolver, update, interceptor)
