package com.oliynick.max.reader.app.feature

import com.oliynick.max.reader.app.AppException
import com.oliynick.max.reader.app.feature.article.list.Page

data class Loadable<out T>(
    val data: List<T>,
    val hasMore: Boolean,
    val loadableState: LoadableState,
) {
    companion object {
        fun <T> newLoading(
            data: List<T>
        ) = Loadable(data = data, hasMore = false, loadableState = Loading)
    }
}

data class Loadable1<out T>(
    val data: List<T>,
    val loadableState: LoadableState,
)

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
    how: (List<T>) -> List<T>
) = copy(data = how(data))

fun <T> Loadable<T>.toLoadingNext() =
    copy(loadableState = LoadingNext)

fun <T> Loadable<T>.toLoading() =
    copy(loadableState = Loading)

fun <T> Loadable<T>.toRefreshing() =
    copy(loadableState = Refreshing)

fun <T> Loadable<T>.toPreview(
    page: Page<T>,
): Loadable<T> =
    when (loadableState) {
        LoadingNext, is Exception -> {
            copy(
                data = data + page.data,
                loadableState = Preview,
                hasMore = page.hasMore
            )
        }
        Loading, Refreshing -> copy(
            data = page.data,
            loadableState = Preview,
            hasMore = page.hasMore
        )
        Preview -> this
    }

fun <T> Loadable<T>.toException(
    cause: AppException,
) = copy(loadableState = Exception(cause))