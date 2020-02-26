package com.oliynick.max.tea.core.debug.app.presentation.sidebar

import com.intellij.icons.AllIcons
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.wm.WindowManager
import com.intellij.ui.JBColor
import com.intellij.ui.awt.RelativePoint
import java.awt.Color
import java.awt.Insets
import javax.swing.Icon

private val exceptionBalloonTextColor = JBColor(Color.BLACK, Color(182, 182, 182))
private val exceptionBalloonFillColor = JBColor(0xffcccc, 0x704745)
private val exceptionBalloonIcon by lazy { AllIcons.General.NotificationError!! }

private val notificationBalloonTextColor = JBColor(Color.BLACK, Color(182, 182, 182))
private val notificationBalloonFillColor = JBColor(0xbaeeba, 0x33412E)
private val notificationBalloonIcon by lazy { AllIcons.General.NotificationInfo!! }

fun Project.showBalloon(balloon: Balloon) {
    val point = RelativePoint.getNorthEastOf(WindowManager.getInstance().getStatusBar(this).component)

    balloon.show(point, Balloon.Position.atRight)
}

fun createNotificationBalloon(
    html: String
) = createBalloon(html, notificationBalloonIcon, notificationBalloonTextColor, notificationBalloonFillColor)

fun createErrorBalloon(
    html: String
) = createBalloon(html, exceptionBalloonIcon, exceptionBalloonTextColor, exceptionBalloonFillColor)

fun createBalloon(
    html: String,
    icon: Icon,
    textColor: JBColor,
    fillColor: JBColor
): Balloon =
    JBPopupFactory.getInstance()
        .createHtmlTextBalloonBuilder(html, icon, textColor, fillColor, null)
        .setBlockClicksThroughBalloon(true)
        .setFadeoutTime(15_000L)
        .setContentInsets(Insets(15, 15, 15, 15))
        .setCornerToPointerDistance(30)
        .setCloseButtonEnabled(true)
        .createBalloon()

