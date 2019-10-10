package com.oliynick.max.elm.time.travel.app.presentation.misc

import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

interface DefaultDocumentListener : DocumentListener {

    override fun changedUpdate(e: DocumentEvent) {
        onValueUpdated(e.document.getText(0, e.document.length))
    }

    override fun insertUpdate(e: DocumentEvent) {
        onValueUpdated(e.document.getText(0, e.document.length))
    }

    override fun removeUpdate(e: DocumentEvent) {
        onValueUpdated(e.document.getText(0, e.document.length))
    }

    fun onValueUpdated(value: String) = Unit

}