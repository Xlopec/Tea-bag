
dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    api(project(path= ":elm-time-travel-protocol", configuration= "default"))
    implementation(project(path= ":elm-core-component", configuration= "default"))

    implementation (Libraries.kotlinStdLib)
    implementation (Libraries.kotlinReflect)

    api ("com.google.code.gson:gson:2.8.6")

    testImplementation(project(path= ":elm-core-test", configuration= "default"))
    testImplementation ("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3")

}