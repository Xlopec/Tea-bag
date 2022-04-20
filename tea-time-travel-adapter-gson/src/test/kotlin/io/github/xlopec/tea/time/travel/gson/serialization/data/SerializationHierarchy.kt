/*
 * MIT License
 *
 * Copyright (c) 2021. Maksym Oliinyk.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.xlopec.tea.time.travel.gson.serialization.data

internal sealed class A

internal sealed class B : A()

internal data class C(
    val value: String = "C"
) : B()

internal data class D(
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

internal object Singleton
