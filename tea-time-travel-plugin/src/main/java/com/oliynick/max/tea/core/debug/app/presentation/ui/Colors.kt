package com.oliynick.max.tea.core.debug.app.presentation.ui

import com.intellij.icons.AllIcons
import com.intellij.ui.JBColor
import java.awt.Color

val ExceptionBalloonTextColor = JBColor(Color.BLACK, Color(182, 182, 182))
val ExceptionBalloonFillColor = JBColor(0xffcccc, 0x704745)
val ExceptionBalloonIcon = AllIcons.General.NotificationError

val NotificationBalloonTextColor = JBColor(Color.BLACK, Color(182, 182, 182))
val NotificationBalloonFillColor = JBColor(0xbaeeba, 0x33412E)
val NotificationBalloonIcon = AllIcons.General.NotificationInfo

val ErrorColor = Color(ExceptionBalloonFillColor.rgb)
