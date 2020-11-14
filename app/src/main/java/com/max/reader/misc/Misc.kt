@file:Suppress("unused")

package com.max.reader.misc

/** forces compiler to check `when` clause is exhaustive */
val Unit?.safe get() = this

fun <T> unsafeLazy(block: () -> T) = lazy(LazyThreadSafetyMode.NONE, block)
