package com.oliynick.max.reader.app.feature

import com.oliynick.max.reader.app.AppException
import com.oliynick.max.reader.app.feature.article.list.Page
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

data class Loadable<out T>(
    val data: T,
    val hasMore: Boolean,
    val loadableState: LoadableState,
) {
    companion object {
        fun <T> newLoading(
            data: PersistentList<T> = persistentListOf()
        ) = Loadable(data = data, hasMore = false, loadableState = Loading)
    }
}

sealed interface LoadableState

data class Exception(
    val th: AppException,
) : LoadableState

object Loading : LoadableState

object LoadingNext : LoadableState

object Refreshing : LoadableState

object Preview : LoadableState

val Loadable<*>.isLoading: Boolean
    get() = loadableState === Loading

val Loadable<*>.isLoadingNext: Boolean
    get() = loadableState === LoadingNext

val Loadable<*>.isRefreshing: Boolean
    get() = loadableState === Refreshing

val Loadable<*>.isPreview: Boolean
    get() = loadableState === Preview

inline fun <T> Loadable<T>.updated(
    how: (T) -> T
) = copy(data = how(data))

fun <T> Loadable<T>.toLoadingNext() =
    copy(loadableState = LoadingNext)

fun <T> Loadable<T>.toLoading() =
    copy(loadableState = Loading)

fun <T> Loadable<T>.toRefreshing() =
    copy(loadableState = Refreshing)

fun <T> Loadable<PersistentList<T>>.toPreview(
    data: ImmutableList<T>,
) = toPreview(Page(data = data, hasMore = false))

fun <T> Loadable<PersistentList<T>>.toPreview(
    page: Page<T>,
): Loadable<PersistentList<T>> =
    when (loadableState) {
        LoadingNext, is Exception -> {
            copy(
                data = data.addAll(page.data),
                loadableState = Preview,
                hasMore = page.hasMore
            )
        }
        Loading, Refreshing -> copy(
            data = page.data.toPersistentList(),
            loadableState = Preview,
            hasMore = page.hasMore
        )
        Preview -> this
    }

fun <T> Loadable<T>.toException(
    cause: AppException,
) = copy(loadableState = Exception(cause))