
inline fun <T> Iterable<T>.forEachApplying(
    action: T.() -> Unit
) = forEach { t -> t.apply(action) }