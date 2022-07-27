package io.github.xlopec.tea.time.travel.plugin.util

import arrow.core.Either

fun <L, R, C> Either<L, R>.foldSuper(): C where L : C, R : C = fold({ it }, { it })
