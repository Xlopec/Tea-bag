package com.max.weatherviewer

import io.reactivex.Single

fun <T> T.just() = Single.just(this)