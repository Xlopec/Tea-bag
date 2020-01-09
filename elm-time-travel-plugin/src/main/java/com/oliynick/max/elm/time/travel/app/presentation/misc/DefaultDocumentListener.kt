/*
 * Copyright (C) 2019 Maksym Oliinyk.
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