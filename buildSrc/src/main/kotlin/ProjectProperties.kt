import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.get
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.nio.file.Paths

const val CommitHashLength = 6

val isCiEnv: Boolean
    get() = getenvSafe("CI")?.toBoolean() == true

val pluginReleaseChannels: Collection<String>
    get() = when (libraryVersion) {
        is Snapshot -> listOf("dev")
        is Alpha -> listOf("eap")
        is ReleaseCandidate -> listOf("rc")
        is Stable -> listOf()
    }

val branch: String?
    get() = getenvSafe("GITHUB_REF_NAME")
        ?.removePrefix("refs/heads/")
        ?.removePrefix("refs/tags/")
        ?: localBranch

val branchOrDefault: String
    get() = branch ?: "master"

val tag: String?
    get() = getenvSafe("GITHUB_TAG")
        ?.takeUnless { tag -> tag.startsWith("refs/heads/") }
        ?.removePrefix("refs/tags/")

val commitSha: String?
    get() = getenvSafe("GITHUB_SHA")

val libraryVersion: Version
    get() = Version(tag, commitSha)

val Project.ossrhUser: String?
    get() = ciVariable("OSSRH_USER")

val Project.ossrhPassword: String?
    get() = ciVariable("OSSRH_PASSWORD")

val Project.signingKey: String?
    get() = ciVariable("SIGNING_KEY")

val Project.signingPassword: String?
    get() = ciVariable("SIGNING_PASSWORD")

val Project.projectSourceSets: SourceSetContainer
    get() = extensions["sourceSets"] as SourceSetContainer

val Project.documentationDir: File
    get() = buildDir.resolve("documentation")

val Project.libsDir: File
    get() = buildDir.resolve("libs")

val Project.distributionsDir: File
    get() = buildDir.resolve("distributions")

val Project.artifactsDir: File
    get() {
        var root = this

        while (root != root.rootProject) {
            root = root.rootProject
        }

        return root.buildDir.resolve("artifacts/${name}")
    }

val Project.projectHooksDir: File
    get() = Paths.get(rootDir.path, "hooks").toFile()

val Project.gitHooksDir: File
    get() = Paths.get(rootDir.path, ".git", "hooks").toFile()

val Project.detektConfig: File
    get() = Paths.get(rootDir.path, "detekt", "detekt-config.yml").toFile()

val Project.detektBaseline: File
    get() = Paths.get(rootDir.path, "detekt", "detekt-baseline.xml").toFile()

val Project.hasKotlinMultiplatformPlugin: Boolean
    get() = plugins.hasPlugin("org.jetbrains.kotlin.multiplatform")

val Project.hasKotlinJvmPlugin: Boolean
    get() = plugins.hasPlugin("org.jetbrains.kotlin.jvm")

val localBranch: String?
    get() = Runtime.getRuntime().exec("git rev-parse --abbrev-ref HEAD")
        .also { it.waitFor() }
        .takeIf { it.exitValue() == 0 }
        ?.let { BufferedReader(InputStreamReader(it.inputStream)) }
        ?.let { it.use { reader -> reader.readLine().trim() } }

fun Project.ciVariable(
    name: String,
): String? = getenvSafe(name) ?: getPropertySafe(name)

fun getenvSafe(
    name: String,
): String? =
    System.getenv(name).takeUnless(CharSequence?::isNullOrEmpty)

private fun Project.getPropertySafe(
    name: String,
): String? =
    properties[name]?.toString().takeUnless(CharSequence?::isNullOrEmpty)

