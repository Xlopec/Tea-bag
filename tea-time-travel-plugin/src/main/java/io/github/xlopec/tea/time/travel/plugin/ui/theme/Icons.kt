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

package io.github.xlopec.tea.time.travel.plugin.ui.theme

/**
 * Plugin wide icon set
 */

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource

object ValueIcon {
    val Class @Composable get() = painterResource("/images/class.svg")
    val Property @Composable get() = painterResource("/images/property.svg")
    val Snapshot @Composable get() = painterResource("/images/snapshotGutter.svg")
}

object ActionIcons {

    val UpdateRunningApplication @Composable get() = painterResource("/images/updateRunningApplication.svg")
    val Remove @Composable get() = painterResource("/images/remove.svg")

    val Import @Composable get() = painterResource("/images/import_dark.svg")
    val Export @Composable get() = painterResource("/images/export_dark.svg")
    val Execute @Composable get() = painterResource("/images/execute.svg")
    val Suspend @Composable get() = painterResource("/images/suspend.svg")
    val Close @Composable get() = painterResource("/images/close.svg")

    val Expand @Composable get() = painterResource("/images/expand.svg")
    val Collapse @Composable get() = painterResource("/images/collapse.svg")
    val Copy @Composable get() = painterResource("/images/inlineCopy.svg")
}
