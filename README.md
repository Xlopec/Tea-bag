[![Maven Central](https://img.shields.io/maven-central/v/io.github.xlopec/tea-core?style=plastic&versionPrefix=1.0.0-alpha3)](https://mvnrepository.com/artifact/io.github.xlopec/tea-core)
[![Jetbrains Market Place](https://img.shields.io/jetbrains/plugin/d/14254)](https://plugins.jetbrains.com/plugin/14254-time-travel-debugger)

# TEA Bag

<img align="right" alt="Tea Bag Logo" height="200px" src="res/tea-bag-logo.png">

Tea Bag is a simple implementation of [TEA](https://guide.elm-lang.org/architecture/)
architecture written in Kotlin. This library is based on Kotlin's coroutines, extensively uses
extension-based approach and supports both ```jvm``` and ```ios``` targets.

This library isn't production ready yet and was originally intended as pet project to give TEA a
try. Later I found that it'd be nice to make it simpler and more lightweight than analogs, add
debugging capabilities...

## Quick Sample

Nothing special, we just need to code our initializer, resolver (`tracker` function),
updater (`computeNewState`function), and UI (`renderSnapshot,` function). After that we should pass
them to an appropriate `Component` builder overload.

```kotlin
import io.github.xlopec.tea.core.Component
import io.github.xlopec.tea.core.Initial
import io.github.xlopec.tea.core.Regular
import io.github.xlopec.tea.core.ResolveCtx
import io.github.xlopec.tea.core.Snapshot
import io.github.xlopec.tea.core.command
import io.github.xlopec.tea.core.invoke
import io.github.xlopec.tea.core.sideEffect
import kotlinx.coroutines.runBlocking

/**Async initializer*/
suspend fun initializer() =
  Initial<String, String>("Hello", emptySet())

/**Some tracker*/
fun tracker(
  event: String,
  ctx: ResolveCtx<String>,
) {
  ctx sideEffect { println("Tracked: \"$event\"") }
}

/**App logic, just appends user message and passes it further to tracker*/
fun computeNewState(
  msg: String,
  state: String
) = (state + msg) command msg

/**Some UI, e.g. console*/
suspend fun renderSnapshot(
  snapshot: Snapshot<*, *, *>
) {
  val description = when (snapshot) {
    is Initial -> "Initial snapshot, $snapshot"
    is Regular -> "Regular snapshot, $snapshot"
  }

  println(description)
}

fun main() = runBlocking {
  // Somewhere at the application level
  val component = Component(
    initializer = ::initializer,
    resolver = ::tracker,
    updater = ::computeNewState,
    scope = this
  )
  // UI = component([message1, message2, ..., message N])
  component(" ", "world").collect(::renderSnapshot)
}
```

The sample above will print the following:

```text
Initial snapshot, Initial(currentState=Hello, commands=[])
Tracked: " "
Tracked: "world"
Regular snapshot, Regular(currentState=Hello , commands=[ ], previousState=Hello, message= )
Regular snapshot, Regular(currentState=Hello world, commands=[world], previousState=Hello , message=world)
```

Real world examples include [Android](https://github.com/Xlopec/Tea-bag/tree/master/samples/app) and
[IOS](https://github.com/Xlopec/Tea-bag/tree/master/samples/iosApp) app samples that use the same application component 
and share common entities, application and navigation logic.
[Intellij plugin](https://github.com/Xlopec/Tea-bag/tree/master/tea-time-travel-plugin) is built on the top of the library
as well.

## Main Features

- **Multiplatform** this library supports ```jvm```, ```iosX64``` and ```iosArm64``` targets
- **Scalability** it is build on the top of a simple idea of having pure functions that operate on
  plain data separated from impure one. Those functions are building blocks and form testable
  components that can be combined to build complex applications
- **Simplicity** component implementation resides in a single file
- **Extensibility** additional functionality and API is implemented as component extensions which
  means you can easily add your own
- **Debugger** [Intellij debugger plugin](https://plugins.jetbrains.com/plugin/14254-time-travel-debugger)
  is available for this library, though it's not production ready yet

## Gradle

Add the dependency:

```kotlin
implementation("io.github.xlopec:tea-core:[version]")
implementation("io.github.xlopec:tea-time-travel:[version]")
implementation("io.github.xlopec:tea-time-travel-adapter-gson:[version]")
```

Make sure that you have `mavenCentral()` in the list of repositories.

## Plugin

<p align="center">
  <img alt="Demo" src="res/demo.gif">
</p>

Plugin is available on [JetBrains marketplace](https://plugins.jetbrains.com/plugin/14254-time-travel-debugger)

## Main Modules

- **tea-core** - contains core types along with basic component implementation
- **tea-time-travel** - contains debuggable version of the component
- **tea-time-travel-adapter-gson** - implements debug protocol and serialization by means
  of [Gson](https://github.com/google/gson) library. Should be added as dependency together with **tea-time-travel** module
- **tea-time-travel-protocol** - contains debug protocol types definitions
- **tea-time-travel-plugin** - contains Intellij plugin implementation

## How to build and run

To build plugin from sources use ```./gradlew tea-time-travel-plugin:buildPlugin``` command.
Installable plugin will be located in ```tea-time-travel-plugin/build/distributions``` directory.

To run Intellij Idea with installed plugin use ```./gradlew tea-time-travel-plugin:runIde```
command.

To build android app sample run ```./gradlew :samples:app:assembleDefaultDebug``` or ```./gradlew :samples:app:assembleRemoteDebug```.
The last command assembles debuggable version on of the application that connects to the currently running instance of 
debugger (will try connecting to http://localhost:8080).

## Planned features and TODOs

- Re-implement client-server communication protocol from scratch
- Migrate from gson to `kotlinx.serialization.json`
- Release v1.0.0
- Add GitHub Wiki
- Rework component builders and possibly replace it with some kind of DSL
- Add keyboard shortcuts for plugin, consider improving plugin UX

## Contribution

Contributions are more than welcome. If something cannot be done, not convenient, or does not work -
create an issue or PR  
