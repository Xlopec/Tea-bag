@file:Suppress("FunctionName")

package com.oliynick.max.tea.core.debug.app.domain.resolver

import com.intellij.openapi.project.Project

interface HasProject {
    val project: Project
}

fun HasProject(
    project: Project
) = object : HasProject {
    override val project: Project = project
}
