
dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    api(project(path= ":tea-time-travel-protocol", configuration= "default"))
    implementation(project(path= ":tea-core", configuration= "default"))

    implementation (Libraries.kotlinStdLib)
    implementation (Libraries.kotlinReflect)

    api ("com.google.code.gson:gson:2.8.6")

    testImplementation(project(path= ":tea-test", configuration= "default"))
    testImplementation ("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3")

}