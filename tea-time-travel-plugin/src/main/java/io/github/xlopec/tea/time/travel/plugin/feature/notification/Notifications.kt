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

package io.github.xlopec.tea.time.travel.plugin.feature.notification

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.project.Project

fun Project.showNotification(
    title: String,
    message: String,
    type: NotificationType,
    vararg actions: AnAction
) = showNotification(title, message, type, listOf(*actions))

fun Project.showNotification(
    title: String,
    message: String,
    type: NotificationType,
    actions: Iterable<AnAction>
) {
    NotificationGroupManager.getInstance()
        .getNotificationGroup("Tea Time Traveller")
        .createNotification(title, message, type)
        .addActions(actions as? MutableCollection<out AnAction> ?: actions.toMutableList() as MutableCollection<out AnAction>)
        .notify(this)
}
