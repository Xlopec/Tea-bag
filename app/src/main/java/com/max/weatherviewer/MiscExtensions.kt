@file:Suppress("unused")

package com.max.weatherviewer

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.navigation.NavArgs
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.fragment.navArgs
import kotlinx.coroutines.Job

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

inline fun <reified Args : NavArgs> Fragment.args() = navArgs<Args>().value

fun MutableCollection<Job>.dispose() {
    forEach { it.cancel() }
    clear()
}