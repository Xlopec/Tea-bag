package com.oliynick.max.tea.core.debug.app.presentation.ui.misc

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.swing.JTextField

var JTextField.textSafe: String
    get() = text ?: ""
    set(value) {
        if (text != value) {
            text = value
        }
    }

fun JTextField.textChanges(): Flow<String> =
    callbackFlow {

        val l = DefaultDocumentListener(::offer)

        document.addDocumentListener(l)
        awaitClose { document.removeDocumentListener(l) }
    }
