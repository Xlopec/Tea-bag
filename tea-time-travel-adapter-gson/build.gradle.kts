import Libraries.immutableCollections
import Libraries.kotlinReflect
import Libraries.kotlinStdLib

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    api(project(path = ":tea-time-travel-protocol", configuration = "default"))

    implementation(kotlinStdLib)
    implementation(kotlinReflect)

    api("com.google.code.gson:gson:2.8.6")

    testImplementation(project(path = ":tea-test", configuration = "default"))
    testImplementation(immutableCollections)

}