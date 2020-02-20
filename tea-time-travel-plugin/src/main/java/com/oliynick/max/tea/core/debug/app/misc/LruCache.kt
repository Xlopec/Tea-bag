package com.oliynick.max.tea.core.debug.app.misc

/**
 * The most simple, non-synchronized version of LRU cache
 */
class LruCache<K, V>(val capacity: UInt) {

    init {
        require(capacity > 0U) { "Capacity should be greater than 0" }
    }

    // [most used, ..., least used, null, ..., null]
    private val cache by lazy(LazyThreadSafetyMode.NONE) { arrayOfNulls<Pair<K, V>?>(capacity.toInt()) }
    private var realSize = 0U

    val size: UInt
        get() = realSize

    val isEmpty: Boolean
        inline get() = size == 0U

    // todo add checks
    fun getOrPut(
        k: K,
        v: () -> V
    ): V {

        for (i in cache.indices) {

            val cached = cache[i] ?: break

            if (cached.first == k) {
                moveToFront(cached, i)
                return cached.second
            }
        }

        val newCached = k to v()

        moveToFront(newCached, cache.lastIndex)

        if (realSize < capacity) {
            realSize++
        }

        return newCached.second
    }

    private fun moveToFront(
        elem: Pair<K, V>,
        i: Int
    ) {

        for (j in i downTo 1) {
            cache[j] = cache[j - 1]
        }

        cache[0] = elem
    }

}