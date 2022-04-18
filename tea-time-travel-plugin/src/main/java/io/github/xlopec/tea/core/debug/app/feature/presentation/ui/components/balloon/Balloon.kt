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

package io.github.xlopec.tea.core.debug.app.feature.presentation.ui.components.balloon

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.wm.WindowManager
import com.intellij.ui.JBColor
import com.intellij.ui.awt.RelativePoint
import io.github.xlopec.tea.core.debug.app.feature.presentation.ui.components.ExceptionBalloonFillColor
import io.github.xlopec.tea.core.debug.app.feature.presentation.ui.components.ExceptionBalloonIcon
import io.github.xlopec.tea.core.debug.app.feature.presentation.ui.components.ExceptionBalloonTextColor
import io.github.xlopec.tea.core.debug.app.feature.presentation.ui.components.NotificationBalloonFillColor
import io.github.xlopec.tea.core.debug.app.feature.presentation.ui.components.NotificationBalloonIcon
import io.github.xlopec.tea.core.debug.app.feature.presentation.ui.components.NotificationBalloonTextColor
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

