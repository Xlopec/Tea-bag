/*
 * Copyright (C) 2021. Maksym Oliinyk.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.oliynick.max.tea.core.debug.app.domain

sealed class Either<out L, out R>

data class Left<L>(val l: L) : Either<L, Nothing>()

data class Right<R>(val r: R) : Either<Nothing, R>()

inline fun <L, R, T> Either<L, R>.fold(
    left: (L) -> T,
    right: (R) -> T
): T =
    when (this) {
        is Left -> left(l)
        is Right -> right(r)
    }
