
inline fun <T> Sequence<T>.forEachApplying(
    action: T.() -> Unit
) = forEach { t -> t.apply(action) }

inline fun <T> Iterable<T>.forEachApplying(
    action: T.() -> Unit
) = forEach { t -> t.apply(action) }