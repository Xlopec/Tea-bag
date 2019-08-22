@file:Suppress("unused")

package com.max.weatherviewer

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

fun defaultNavOptionsBuilder(): NavOptions.Builder = NavOptions.Builder()
    .setEnterAnim(android.R.anim.slide_in_left)
    .setExitAnim(android.R.anim.slide_out_right)
    .setPopEnterAnim(R.anim.slide_in_right)
    .setPopExitAnim(R.anim.slide_out_left)

fun fadeNavOptionsBuilder(): NavOptions.Builder = NavOptions.Builder()
    .setEnterAnim(android.R.anim.fade_in)
    .setExitAnim(android.R.anim.fade_out)
    .setPopEnterAnim(R.anim.slide_in_right)
    .setPopExitAnim(R.anim.slide_out_left)

fun defaultNavOptions(): NavOptions = defaultNavOptionsBuilder().build()

fun NavController.navigateDefault(@IdRes resId: Int, args: Bundle? = null, navOptions: NavOptions? = null,
                                  extras: Navigator.Extras? = null) {
    navigate(resId, args, navOptions, extras)
}


fun NavController.navigateDefaultAnimated(@IdRes resId: Int, args: Bundle? = null, navOptions: NavOptions? = defaultNavOptions(),
                                          extras: Navigator.Extras? = null) {
    navigate(resId, args, navOptions, extras)
}

fun <T> Flow<T>.mergeWith(other: Flow<T>): Flow<T> =
    channelFlow {
        coroutineScope {
            launch {
                other.collect {
                    offer(it)
                }
            }

            launch {
                collect {
                    offer(it)
                }
            }
        }
    }

fun <T> Flow<T>.startWith(t: T): Flow<T> = onStart { emit(t) }

fun <T> Flow<T>.startWith(first: T, vararg other: T): Flow<T> = onStart { emit(first); other.forEach { emit(it) } }