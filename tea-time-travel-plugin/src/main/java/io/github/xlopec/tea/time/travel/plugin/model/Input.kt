package io.github.xlopec.tea.time.travel.plugin.model

import arrow.core.Validated

data class Input<out E, out V>(
    val input: String,
    val value: Validated<E, V>,
)
