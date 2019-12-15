@file:Suppress("unused")

package com.max.weatherviewer.misc

/** forces compiler to check `when` clause is exhaustive */
val Unit?.safe get() = this