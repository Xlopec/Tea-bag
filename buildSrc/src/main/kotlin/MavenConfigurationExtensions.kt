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
