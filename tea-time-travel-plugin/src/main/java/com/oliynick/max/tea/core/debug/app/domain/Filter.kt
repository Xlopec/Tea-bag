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

package com.oliynick.max.tea.core.debug.app.domain

import com.oliynick.max.tea.core.debug.app.domain.FilterOption.*
import java.util.*
import kotlin.collections.HashSet

typealias Predicate = (String) -> Boolean

enum class FilterOption {
    SUBSTRING,
    REGEX,
    WORDS
}

class Filter private constructor(
    val option: FilterOption,
    val ignoreCase: Boolean,
    val predicate: Validated<Predicate>? = null
) {

    companion object {

        private val EMPTY = Filter(option = SUBSTRING, ignoreCase = true)

        fun empty() = EMPTY

        fun new(
            filter: String,
            option: FilterOption,
            ignoreCase: Boolean
        ): Filter {

            if (filter.isEmpty()) {
                return Filter(option, ignoreCase)
            }

            return when (option) {
                SUBSTRING -> Filter(
                    SUBSTRING,
                    ignoreCase,
                    Valid(filter, SubstringPredicate(filter, ignoreCase))
                )
                REGEX -> Filter(REGEX, ignoreCase, RegexPredicate(filter, ignoreCase))
                WORDS -> Filter(
                    WORDS,
                    ignoreCase,
                    Valid(filter, WordsPredicate(filter, ignoreCase))
                )
            }
        }
    }

    val isEmpty: Boolean = predicate == null

}

fun SubstringPredicate(
    filter: String,
    ignoreCase: Boolean
): Predicate =
    { input -> filter.isEmpty() || input.contains(filter, ignoreCase) }

fun WordsPredicate(
    filter: String,
    ignoreCase: Boolean
): Predicate =
    { input -> input.equals(filter, ignoreCase) }

private val IGNORE_CASE_SET = EnumSet.of(RegexOption.IGNORE_CASE)
private val ALL_MATCH_REGEX = Regex(".*")
private val ALL_MATCH_IGNORING_CASE_REGEX = Regex(".*", RegexOption.IGNORE_CASE)

fun RegexPredicate(
    rawRegex: String,
    ignoreCase: Boolean
): Validated<Predicate> {

    val validated = if (rawRegex.isEmpty()) {
        Valid(rawRegex, if (ignoreCase) ALL_MATCH_IGNORING_CASE_REGEX else ALL_MATCH_REGEX)
    } else {
        runCatching { Regex(rawRegex, if (ignoreCase) IGNORE_CASE_SET else emptySet()) }.fold(
            onSuccess = { rgx -> Valid(rawRegex, rgx) },
            onFailure = { th ->
                Invalid(
                    rawRegex,
                    th.message
                        ?: "Invalid regular expression"
                )
            }
        )
    }

    return validated.map { regex -> regex::matches }
}

/**
 * Filters given [value] recursively. The value will be taken if it
 * or any its child value matches regex.
 * Any non-matching siblings of the current value will be filtered out
 */
fun applyTo(
    value: Value,
    predicate: Predicate
): Value? =
    when {
        value.primitiveTypeName?.let(predicate) == true || (value is Null && predicate(null.toString())) -> value
        value is CollectionWrapper -> applyToWrapper(value, predicate)
        value is Ref -> applyToRef(value, predicate)
        else -> null
    }

private fun applyToRef(
    ref: Ref,
    predicate: Predicate
): Ref? {

    fun applyToProp(
        property: Property
    ): Property? =
        if (predicate(property.name)) property
        else applyTo(property.v, predicate)
            ?.let { filteredValue ->
                Property(
                    property.name,
                    filteredValue
                )
            }

    return if (predicate(ref.type.name)) ref
    else ref.properties.mapNotNullTo(HashSet(ref.properties.size), ::applyToProp)
        .takeIf { filteredProps -> filteredProps.isNotEmpty() }
        ?.let { ref.copy(properties = it) }
}

private fun applyToWrapper(
    wrapper: CollectionWrapper,
    predicate: Predicate
): CollectionWrapper? =
    wrapper.value
        .mapNotNull { v -> applyTo(v, predicate) }
        .takeIf { filtered -> filtered.isNotEmpty() }
        ?.let(::CollectionWrapper)
