package com.oliynick.max.elm.core.component

/**
 * Marks declarations that are **obsolete** in Component API, which means that the design of the corresponding
 * declarations has serious known flaws and they will be redesigned in the future.
 * Roughly speaking, these declarations will be deprecated in the future but there is no replacement for them yet,
 * so they cannot be deprecated right away.
 */
@MustBeDocumented
@Retention(value = AnnotationRetention.BINARY)
@Experimental(level = Experimental.Level.WARNING)
annotation class ObsoleteComponentApi

@MustBeDocumented
@Retention(value = AnnotationRetention.BINARY)
@Experimental(level = Experimental.Level.WARNING)
annotation class UnstableApi

@Retention(value = AnnotationRetention.BINARY)
@Experimental(level = Experimental.Level.ERROR)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.TYPEALIAS, AnnotationTarget.PROPERTY)
annotation class InternalComponentApi