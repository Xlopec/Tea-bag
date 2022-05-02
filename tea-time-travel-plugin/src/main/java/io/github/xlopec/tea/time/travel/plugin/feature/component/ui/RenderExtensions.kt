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

@file:Suppress("FunctionName")

package io.github.xlopec.tea.time.travel.plugin.feature.component.ui

import io.github.xlopec.tea.time.travel.plugin.model.BooleanWrapper
import io.github.xlopec.tea.time.travel.plugin.model.CharWrapper
import io.github.xlopec.tea.time.travel.plugin.model.CollectionWrapper
import io.github.xlopec.tea.time.travel.plugin.model.FilteredSnapshot
import io.github.xlopec.tea.time.travel.plugin.model.Null
import io.github.xlopec.tea.time.travel.plugin.model.NumberWrapper
import io.github.xlopec.tea.time.travel.plugin.model.Property
import io.github.xlopec.tea.time.travel.plugin.model.Ref
import io.github.xlopec.tea.time.travel.plugin.model.StringWrapper
import io.github.xlopec.tea.time.travel.plugin.model.Value
import io.github.xlopec.tea.time.travel.plugin.model.primitiveTypeName
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.MutableTreeNode

val DATE_TIME_FORMATTER: DateTimeFormatter by lazy {
    DateTimeFormatter.ofLocalizedDateTime(
        FormatStyle.MEDIUM
    )
}

fun toReadableStringLong(
    value: Value
): String =
    when (value) {
        is StringWrapper -> value.toReadableString()
        is Null -> value.toReadableString()
        is Ref -> value.toReadableString()
        is CollectionWrapper -> value.toReadableString()
        is NumberWrapper -> value.toReadableString()
        is CharWrapper -> value.toReadableString()
        is BooleanWrapper -> value.toReadableString()
    }

fun toReadableStringShort(
    value: Value
): String =
    when (value) {
        is StringWrapper -> value.toReadableString()
        is Null -> value.toReadableString()
        is Ref -> value.toReadableStringShort()
        is CollectionWrapper -> value.toReadableStringShort()
        is NumberWrapper -> value.toReadableString()
        is CharWrapper -> value.toReadableString()
        is BooleanWrapper -> value.toReadableString()
    }

private operator fun DefaultMutableTreeNode.plusAssign(children: Iterable<MutableTreeNode>) =
    children.forEach(::add)

private operator fun DefaultMutableTreeNode.plusAssign(child: MutableTreeNode) = add(child)

@Suppress("unused")
internal fun Null.toReadableString(): String = "null"

internal fun StringWrapper.toReadableString(): String = "\"$value\":$primitiveTypeName"

internal fun Ref.toReadableString(): String =
    "${type.name}(${properties.joinToString(transform = ::toReadableString, limit = 120)})"

internal fun Ref.toReadableStringShort(): String = type.name

internal fun toReadableString(field: Property): String =
    "${field.name}=${toReadableStringLong(field.v)}"

internal fun CollectionWrapper.toReadableString(): String =
    items.joinToString(
        prefix = "[",
        postfix = "]",
        transform = ::toReadableStringLong
    )

internal fun CollectionWrapper.toReadableStringShort(): String =
    "[${items.size} element${if (items.size == 1) "" else "s"}]"

internal fun NumberWrapper.toReadableString(): String =
    "$value:$primitiveTypeName"

internal fun CharWrapper.toReadableString(): String = "$value:$primitiveTypeName"

internal fun BooleanWrapper.toReadableString(): String = "$value:$primitiveTypeName"

fun toReadableString(
    snapshot: FilteredSnapshot
) = "${snapshot.meta.timestamp.format(DATE_TIME_FORMATTER)}: ${snapshot.meta.id.value}"
