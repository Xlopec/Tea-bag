
inline fun <T> Sequence<T>.forEachApplying(
    action: T.() -> Unit
) = forEach { t -> t.apply(action) }