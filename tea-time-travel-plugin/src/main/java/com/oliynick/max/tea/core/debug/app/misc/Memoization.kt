package com.oliynick.max.tea.core.debug.app.misc

//todo implement empty static instance with a factory method
data class Key7(
    val a1: Any?,
    val a2: Any? = null,
    val a3: Any? = null,
    val a4: Any? = null,
    val a5: Any? = null,
    val a6: Any? = null,
    val a7: Any? = null
)

inline fun <A1, A2, R> memoize(
    capacity: UInt = 10U,
    crossinline f: (A1, A2) -> R
) =
    object : (A1, A2) -> R {

        private val cache = LruCache<Key7, R>(capacity)

        override fun invoke(
            p1: A1,
            p2: A2
        ): R =
            cache.getOrPut(Key7(p1, p2)) { f(p1, p2) }

    }
