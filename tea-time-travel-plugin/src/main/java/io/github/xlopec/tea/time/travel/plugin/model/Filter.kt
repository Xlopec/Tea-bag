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

package io.github.xlopec.tea.time.travel.plugin.model

import androidx.compose.runtime.Immutable
import arrow.core.Valid
import arrow.core.Validated
import io.github.xlopec.tea.time.travel.plugin.model.FilterOption.*
import java.util.*

typealias Predicate = (String) -> Boolean

// todo make a sealed interface with predicate
enum class FilterOption {
    SUBSTRING,
    REGEX,
    WORDS
}

private val MatchAllValidatedPredicate: Input<String, Predicate> = Input("", Valid(value = { true }))

@Immutable
class Filter private constructor(
    val option: FilterOption,
    val ignoreCase: Boolean,
    val predicate: Input<String, Predicate>
) {

    companion object {

        private val EMPTY = Filter(option = SUBSTRING, ignoreCase = true, predicate = MatchAllValidatedPredicate)

        fun empty() = EMPTY

        fun new(
            filter: String,
            option: FilterOption,
            ignoreCase: Boolean
        ): Filter {

            if (filter.isEmpty()) {
                return Filter(option, ignoreCase, MatchAllValidatedPredicate)
            }

            return when (option) {
                SUBSTRING -> Filter(
                    SUBSTRING,
                    ignoreCase,
                    Input(filter, Valid(SubstringPredicate(filter, ignoreCase)))
                )
                REGEX -> Filter(REGEX, ignoreCase, RegexPredicate(filter, ignoreCase))
                WORDS -> Filter(
                    WORDS,
                    ignoreCase,
                    Input(filter, Valid(WordsPredicate(filter, ignoreCase)))
                )
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Filter

        if (option != other.option) return false
        if (ignoreCase != other.ignoreCase) return false
        if (predicate != other.predicate) return false

        return true
    }

    override fun hashCode(): Int {
        var result = option.hashCode()
        result = 31 * result + ignoreCase.hashCode()
        result = 31 * result + predicate.hashCode()
        return result
    }

    override fun toString(): String = "Filter(option=$option, ignoreCase=$ignoreCase, predicate=$predicate)"
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
): Input<String, Predicate> {

    @Suppress("SuspiciousCallableReferenceInLambda")
    val validatedRegex = Validated.catch {
        when {
            rawRegex.isEmpty() && ignoreCase -> ALL_MATCH_IGNORING_CASE_REGEX
            rawRegex.isEmpty() && !ignoreCase -> ALL_MATCH_REGEX
            else -> Regex(rawRegex, if (ignoreCase) IGNORE_CASE_SET else emptySet())
        }
    }.mapLeft { th -> th.message ?: "Invalid regular expression" }
        .map { regex -> regex::matches }

    return Input(rawRegex, validatedRegex)
}

/**
 * Filters given [value] recursively. The value will be taken if it
 * or any its child value matches regex.
 * Any non-matching siblings of the current value will be filtered out
 */
fun Predicate.applyTo(
    value: Value
): Value? =
    when {
        value.primitiveTypeName?.let(this) == true || (value is Null && this(null.toString())) -> value
        value.stringValue?.let(this) == true -> value
        value is CollectionWrapper -> applyToWrapper(value)
        value is Ref -> applyToRef(value)
        else -> null
    }

private fun Predicate.applyToRef(
    ref: Ref
): Ref? {

    fun applyToProp(
        property: Property
    ): Property? =
        if (this(property.name)) {
            property
        } else {
            applyTo(property.v)
            ?.let { filteredValue ->
                Property(
                    property.name,
                    filteredValue
                )
            }
        }

    return if (this(ref.type.name)) {
        ref
    } else {
        ref.properties.mapNotNullTo(HashSet(ref.properties.size), ::applyToProp)
        .takeIf(Collection<*>::isNotEmpty)
        ?.let { ref.copy(properties = it) }
    }
}

fun Predicate.applyToWrapper(
    wrapper: CollectionWrapper
): CollectionWrapper? =
    wrapper.items
        .mapNotNull(::applyTo)
        .takeIf(Collection<*>::isNotEmpty)
        ?.let(::CollectionWrapper)
