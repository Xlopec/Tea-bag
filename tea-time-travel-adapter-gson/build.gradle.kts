
import Libraries.gson
import Libraries.immutableCollections
import Libraries.kotlinStdLib

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    api(project(":tea-time-travel-protocol"))
    api("com.google.code.gson:gson:$gson")

    implementation(kotlinStdLib)

    testImplementation(project(":tea-test"))
    testImplementation(immutableCollections)

}