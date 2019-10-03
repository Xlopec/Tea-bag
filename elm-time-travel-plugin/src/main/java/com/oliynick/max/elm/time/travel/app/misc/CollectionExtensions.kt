package com.oliynick.max.elm.time.travel.app.misc

interface DiffCallback<in T1, in T2> {

    fun areItemsTheSame(oldItem: T1, newItem: T2): Boolean

    fun areContentsTheSame(oldItem: T1, newItem: T2): Boolean

}

/**
 * Replaces content in receiver list with content of replace list.
 * Changes in target list calculated using Eugene W. Myers diff algorithm.
 *
 * Might produce uncaught exceptions and poorly tested, so be careful.
 * Complexity is O(N + M)
 */
// todo add batch updates and decision strategy
inline fun <L : MutableList<T1>, T1, T2> L.replaceAll(replaceWith: List<T2>, diffCallback: DiffCallback<T1, T2>, crossinline supply: (T2) -> T1): L {

    var x = 0
    var y = 0

    var moodX = size
    val moodY = replaceWith.size

    while (x < size || y < replaceWith.size) {

        require(moodX == size) { "Receiver collection shouldn't be modified during modification" }
        require(moodY == replaceWith.size) { "Replace collection shouldn't be modified during modification" }

        while (x < size && y < replaceWith.size && diffCallback.areItemsTheSame(this[x], replaceWith[y])) {
            // move towards diagonal
            if (!diffCallback.areContentsTheSame(this[x], replaceWith[y])) {
                this[x] = supply(replaceWith[y])
            }

            x += 1
            y += 1
        }

        while (x < size && (y == replaceWith.size || (y < replaceWith.size && !diffCallback.areItemsTheSame(this[x], replaceWith[y])))) {
            // move down
            removeAt(x)
            moodX -= 1
        }

        while (y < replaceWith.size && (x >= size || !diffCallback.areItemsTheSame(this[x], replaceWith[y]))) {
            // move right
            add(x, supply(replaceWith[y]))

            x += 1
            y += 1
            moodX += 1
        }
    }

    return this
}

fun <L : MutableList<T>, T> L.replaceAll(replaceWith: List<T>, diffCallback: DiffCallback<T, T>): L {
    return replaceAll(replaceWith, diffCallback) { it }
}