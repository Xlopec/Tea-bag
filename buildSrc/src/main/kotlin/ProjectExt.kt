import org.gradle.api.Project
import java.io.File
import java.nio.file.Paths

private const val COMMIT_HASH_LEN = 6

fun isLocalEnv(): Boolean = !isCIEnv()

fun isCIEnv(): Boolean =
    System.getenv("CI")?.toBoolean() == true

fun bintrayApiKey(): String? =
    if (isCIEnv()) System.getenv("BINTRAY_API_KEY") else null

fun pluginReleaseChannels(): Array<String> {

    val tag: String? = System.getenv("TRAVIS_TAG")

    return when {
        tag.isNullOrEmpty() -> arrayOf("dev")
        tag.contains("alpha") -> arrayOf("eap")
        tag.contains("beta") -> arrayOf("rc")
        else -> emptyArray()
    }
}

fun branchName(): String =
    System.getenv("TRAVIS_BRANCH").takeUnless { s -> s.isNullOrEmpty() } ?: "master"

fun versionName(): String =
    System.getenv("TRAVIS_TAG").takeUnless { s -> s.isNullOrEmpty() } ?:
    System.getenv("TRAVIS_COMMIT").takeUnless { s -> s.isNullOrEmpty() }?.let { commit -> "DEV-${commit.take(COMMIT_HASH_LEN)}" } ?:
    "DEV"

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
