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
