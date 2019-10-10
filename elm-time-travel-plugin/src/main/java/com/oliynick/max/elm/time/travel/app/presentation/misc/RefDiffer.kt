package com.oliynick.max.elm.time.travel.app.presentation.misc

import com.oliynick.max.elm.time.travel.app.misc.DiffCallback

object RefDiffer : DiffCallback<Any, Any> {
    override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean = oldItem === newItem
    override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean = oldItem === newItem
}