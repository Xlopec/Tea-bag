import org.gradle.api.Project
import org.jetbrains.dokka.gradle.GradleDokkaSourceSetBuilder
import java.net.URL

fun GradleDokkaSourceSetBuilder.linkSourcesForSourceSet(
    project: Project,
    sourceSetName: String
) = sourceLink {
    localDirectory.set(project.file("src/$sourceSetName/kotlin"))
    remoteUrl.set(URL("https://github.com/Xlopec/Tea-bag/tree/$branchOrDefault/${project.name}/src/$sourceSetName/kotlin"))
    remoteLineSuffix.set("#L")
}