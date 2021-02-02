
import Libraries.immutableCollections
import Libraries.kotlinStdLib

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    api(project(":tea-time-travel-protocol"))
    api("com.google.code.gson:gson:2.8.6")

    implementation(kotlinStdLib)

    testImplementation(project(":tea-test"))
    testImplementation(immutableCollections)

}