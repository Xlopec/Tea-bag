package com.oliynick.max.tea.core.debug.app.presentation.ui.misc

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.awt.event.ItemEvent
import java.awt.event.ItemListener
import javax.swing.JCheckBox

fun JCheckBox.selections(): Flow<Boolean> =
    callbackFlow {

        val l = ItemListener { e -> offer(e.stateChange == ItemEvent.SELECTED) }

        addItemListener(l)
        awaitClose { removeItemListener(l) }
    }
