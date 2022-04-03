/*
 * MIT License
 *
 * Copyright (c) 2022. Maksym Oliinyk.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.publish.maven.MavenPom
import java.util.*

fun RepositoryHandler.configureRepositories(
    project: Project
) {
    mavenLocal()
    maven {
        name = "OSSRH"
        url = libraryVersion.toOssrhDeploymentUri()
        credentials {
            username = project.ossrhUser
            password = project.ossrhPassword
        }
    }
}

fun MavenPom.configurePom(
    projectName: String,
) {
    name.set(projectName)
    description.set(
        "TEA Bag is simple implementation of TEA written in Kotlin. " +
                "${projectName.capitalize(Locale.ROOT)} is part of this project"
    )
    url.set("https://github.com/Xlopec/Tea-bag.git")

    licenses {
        license {
            name.set("MIT License")
            url.set("https://opensource.org/licenses/MIT")
        }
    }

    developers {
        developer {
            id.set("Maxxx")
            name.set("Maksym Oliinyk")
            url.set("https://github.com/Xlopec")
            organizationUrl.set("https://github.com/Xlopec")
        }
    }

    scm {
        connection.set("scm:git:git://github.com/Xlopec/Tea-bag.git")
        developerConnection.set("scm:git:ssh://github.com:Xlopec/Tea-bag.git")
        url.set("https://github.com/Xlopec/Tea-bag/tree/master")
    }
}
