import org.gradle.api.Project
import java.io.File
import java.nio.file.Paths

fun isLocalEnv(): Boolean = !isCIEnv()

fun isCIEnv(): Boolean =
    System.getenv("CI")?.toBoolean() == true

fun bintrayApiKey(): String? =
    if (isCIEnv()) System.getenv("BINTRAY_API_KEY") else null

fun versionName(): String =
    System.getenv("TRAVIS_TAG").takeUnless { s -> s.isNullOrEmpty() } ?: System.getenv("TRAVIS_COMMIT") ?: "SNAPSHOT"

fun Project.installGitHooks() = afterEvaluate {
    (projectHooksDir.listFiles { f -> f.extension == "sh" } ?: emptyArray())
        .forEach { f ->
            val target = File(gitHooksDir, f.nameWithoutExtension)

            f.copyTo(target, overwrite = true)
            target.setExecutable(true, false)
        }
}

val Project.projectHooksDir: File
    get() = Paths.get(rootDir.path, "hooks").toFile()

val Project.gitHooksDir: File
    get() = Paths.get(rootDir.path, ".git", "hooks").toFile()

val Project.detektConfig: File
    get() = Paths.get(rootDir.path, "detekt", "detekt-config.yml").toFile()

val Project.detektBaseline: File
    get() = Paths.get(rootDir.path, "detekt", "detekt-baseline.xml").toFile()
