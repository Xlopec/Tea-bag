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

package com.oliynick.max.tea.core.debug.gson.serialization.data

import kotlinx.collections.immutable.PersistentList

data class Container(val list: PersistentList<String>)

data class PolyContainer(val list: PersistentList<Poly>)

interface Poly {
    val property: String
}

class PolyB : Poly {
    override val property: String = "b"
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PolyB

        if (property != other.property) return false

        return true
    }

    override fun hashCode(): Int {
        return property.hashCode()
    }
}

class PolyA : Poly {
    override val property: String = "a"
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PolyA

        if (property != other.property) return false

        return true
    }

    override fun hashCode(): Int {
        return property.hashCode()
    }


}
