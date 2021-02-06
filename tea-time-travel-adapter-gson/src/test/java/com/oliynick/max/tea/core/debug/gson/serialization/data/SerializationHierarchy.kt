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

sealed class A

sealed class B : A()

data class C(
    val value: String = "C"
) : B()

data class D(
    val i: Int = 10,
    val c: C = C(),
    val l: List<C?> = listOf(
        C(), null),
    val arrC: Array<C?> = arrayOf(
        C(), null),
    val nilC: C? = null,
    val nilMap: Map<String?, A?> = mapOf(null to C(), null to null, "some" to C(), "some" to null)
) : A() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as D

        if (i != other.i) return false
        if (c != other.c) return false
        if (l != other.l) return false
        if (!arrC.contentEquals(other.arrC)) return false
        if (nilC != other.nilC) return false
        if (nilMap != other.nilMap) return false

        return true
    }

    override fun hashCode(): Int {
        var result = i
        result = 31 * result + c.hashCode()
        result = 31 * result + l.hashCode()
        result = 31 * result + arrC.contentHashCode()
        result = 31 * result + (nilC?.hashCode() ?: 0)
        result = 31 * result + nilMap.hashCode()
        return result
    }
}

object Singleton
