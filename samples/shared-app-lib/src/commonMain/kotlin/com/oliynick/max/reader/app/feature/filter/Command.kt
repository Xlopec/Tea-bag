package com.oliynick.max.reader.app.feature.filter

import com.oliynick.max.reader.app.ScreenId
import com.oliynick.max.reader.app.command.Command
import com.oliynick.max.reader.app.domain.FilterType
import kotlin.jvm.JvmInline

sealed interface FilterCommand : Command

@JvmInline
value class DoLoadSources(
    val id: ScreenId,
) : FilterCommand

data class DoLoadSuggestions(
    val id: ScreenId,
    val type: FilterType
) : FilterCommand
