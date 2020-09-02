package core.misc

import io.kotlintest.shouldBe

inline fun <T, E> Collection<T>.shouldForEach(
    another: Collection<E>,
    block: (t: T, e: E) -> Unit
) {
    size shouldBe another.size

    val thisIter = iterator()
    val anotherIter = another.iterator()

    while(thisIter.hasNext()) {
        block(thisIter.next(), anotherIter.next())
    }
}