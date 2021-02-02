package com.oliynick.max.tea.core.debug.app.presentation.ui.balloon

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.wm.WindowManager
import com.intellij.ui.JBColor
import com.intellij.ui.awt.RelativePoint
import com.oliynick.max.tea.core.debug.app.presentation.ui.*
import java.awt.Insets
import javax.swing.Icon

fun Project.showBalloon(balloon: Balloon) {
    val point = RelativePoint.getNorthEastOf(WindowManager.getInstance().getStatusBar(this).component)

    balloon.show(point, Balloon.Position.atRight)
}

fun createNotificationBalloon(
    html: String
) = createBalloon(html,
        NotificationBalloonIcon,
        NotificationBalloonTextColor,
        NotificationBalloonFillColor
)

fun createErrorBalloon(
    html: String
) = createBalloon(html,
        ExceptionBalloonIcon,
        ExceptionBalloonTextColor,
        ExceptionBalloonFillColor
)

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

