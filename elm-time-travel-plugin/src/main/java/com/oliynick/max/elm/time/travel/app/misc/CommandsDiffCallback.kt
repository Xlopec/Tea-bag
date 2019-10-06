package com.oliynick.max.elm.time.travel.app.misc

object CommandsDiffCallback : DiffCallback<Any, Any> {
    override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean = oldItem === newItem
    override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean = oldItem == newItem
}