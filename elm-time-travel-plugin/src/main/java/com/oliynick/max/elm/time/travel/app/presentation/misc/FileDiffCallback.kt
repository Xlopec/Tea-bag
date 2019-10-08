package com.oliynick.max.elm.time.travel.app.presentation.misc

import com.oliynick.max.elm.time.travel.app.misc.DiffCallback
import java.io.File

internal object FileDiffCallback : DiffCallback<File, File> {
    override fun areContentsTheSame(oldItem: File, newItem: File): Boolean = oldItem == newItem
    override fun areItemsTheSame(oldItem: File, newItem: File): Boolean = oldItem.path == newItem.path
}