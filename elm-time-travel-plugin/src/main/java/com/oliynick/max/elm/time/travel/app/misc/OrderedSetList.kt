/*
 * Copyright (C) 2019 Maksym Oliinyk.
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

package com.oliynick.max.elm.time.travel.app.misc

import org.apache.commons.collections.set.ListOrderedSet

class OrderedSetList<E> {

    private val elements = ListOrderedSet.decorate(mutableListOf<E>())

    val size: Int
        get() = elements.size

    @Suppress("UNCHECKED_CAST")
    operator fun get(index: Int) = elements.get(index) as E

    operator fun plusAssign(element: E) {
        elements.add(element)
    }

    operator fun plusAssign(elements: Iterable<E>) = elements.forEach { e -> this += e }

    operator fun minusAssign(element: E) {
        elements.remove(element)
    }

    operator fun minusAssign(elements: Iterable<E>) = elements.forEach { e -> this -= e }

}

val OrderedSetList<*>.isEmpty get() = size == 0

val OrderedSetList<*>.isNotEmpty get() = !isEmpty