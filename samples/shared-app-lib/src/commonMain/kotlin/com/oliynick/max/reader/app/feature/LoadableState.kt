package com.oliynick.max.reader.app.feature

import com.oliynick.max.reader.app.AppException
import com.oliynick.max.reader.app.feature.article.list.Page

data class LoadableState<T>(
    val data: List<T>,
    val hasMore: Boolean,
    val transientState: TransientState,
) {
    companion object {
        fun <T> newLoading(
            data: List<T>
        ) = LoadableState(data = data, hasMore = false, transientState = Loading)
    }
}

sealed interface TransientState

data class Exception(
    val th: AppException,
) : TransientState

object Loading : TransientState

object LoadingNext : TransientState

object Refreshing : TransientState

object Preview : TransientState

val LoadableState<*>.isLoading: Boolean
    get() = transientState === Loading

val LoadableState<*>.isLoadingNext: Boolean
    get() = transientState === LoadingNext

val LoadableState<*>.isRefreshing: Boolean
    get() = transientState === Refreshing

val LoadableState<*>.isPreview: Boolean
    get() = transientState === Preview

inline fun <T> LoadableState<T>.updated(
    how: (List<T>) -> List<T>
) = copy(data = how(data))

fun <T> LoadableState<T>.toLoadingNext() =
    copy(transientState = LoadingNext)

fun <T> LoadableState<T>.toLoading() =
    copy(transientState = Loading)

fun <T> LoadableState<T>.toRefreshing() =
    copy(transientState = Refreshing)

fun <T> LoadableState<T>.toPreview(
    page: Page<T>,
): LoadableState<T> =
    when (transientState) {
        LoadingNext, is Exception -> {
            copy(
                data = data + page.data,
                transientState = Preview,
                hasMore = page.hasMore
            )
        }
        Loading, Refreshing -> copy(
            data = page.data,
            transientState = Preview,
            hasMore = page.hasMore
        )
        Preview -> this
    }

fun <T> LoadableState<T>.toException(
    cause: AppException,
) = copy(transientState = Exception(cause))