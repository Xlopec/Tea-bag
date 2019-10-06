package com.oliynick.max.elm.time.travel.app.misc

import java.io.File

object FileDiffCallback : DiffCallback<File, File> {
    override fun areContentsTheSame(oldItem: File, newItem: File): Boolean = oldItem == newItem
    override fun areItemsTheSame(oldItem: File, newItem: File): Boolean = oldItem.path == newItem.path
}