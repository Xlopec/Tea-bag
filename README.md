# TEA Bag

<p align="left">
  <img alt="Tea Bag Logo" width="350px" src="res/tea-bag-logo.png">
</p>

The Elm Architecture implementation in Kotlin.

## What's This?

TEA Core is the simplest implementation of [TEA](https://guide.elm-lang.org/architecture/)
architecture written in Kotlin. This library is based on Kotlin's coroutines and extensively uses
extension-based approach.

This library isn't production ready yet and was originally intended as pet project to give TEA a
try. Later I found that it'd be nice to make it more simple and lightweight than analogs, add
debugging capabilities...

## Quick Sample

Nothing special, we just need to code our initializer, resolver (`tracker` function),
updater (`computeNewState`function), and UI (`renderSnapshot,` function). After that we should pass
them to an appropriate `Component` builder overload.

```kotlin
import com.oliynick.max.tea.core.Initial
import com.oliynick.max.tea.core.Regular
import com.oliynick.max.tea.core.Snapshot
import com.oliynick.max.tea.core.component.Component
import com.oliynick.max.tea.core.component.command
import com.oliynick.max.tea.core.component.invoke
import com.oliynick.max.tea.core.component.sideEffect
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking

/**Async initializer*/
suspend fun initializer() =
    Initial<String, String>("Hello", emptySet())

/**Some tracker*/
suspend fun tracker(
    event: String
): Set<String> = event.sideEffect { println("Tracked: \"$event\"") }

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

Real world example includes [Android app sample](https://github.com/Xlopec/Tea-bag/tree/master/app)
built on the top of Jetpack Compose and
[Intellij plugin](https://github.com/Xlopec/Tea-bag/tree/master/tea-time-travel-plugin)

## Main Features

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
// Broken due to coroutines bug, see https://youtrack.jetbrains.com/issue/KT-47195
// implementation("io.github.xlopec:tea-time-travel:[version]")
implementation("io.github.xlopec:tea-time-travel-adapter-gson:[version]")
implementation("io.github.xlopec:tea-time-travel-protocol:[version]")
```

Make sure that you have `mavenCentral()` in the list of repositories.

## Plugin

<p align="center">
  <img alt="Demo" src="res/demo.gif">
</p>

Plugin is available on [JetBrains marketplace](https://plugins.jetbrains.com/plugin/14254-time-travel-debugger)

## Main Modules

- **tea-core** - contains core types along with basic component implementation
- **tea-time-travel** - contains debuggable version of the component (broken due
  to [bug in coroutines](https://youtrack.jetbrains.com/issue/KT-47195))
- **tea-time-travel-adapter-gson** - implements debug protocol and serialization by means
  of [Gson](https://github.com/google/gson) library. Should be added as dependency together with **
  tea-time-travel** module
- **tea-time-travel-protocol** - contains debug protocol types definitions
- **tea-time-travel-plugin** - contains Intellij plugin implementation

## Notes

To build plugin from sources use ```./gradlew tea-time-travel-plugin:buildPlugin``` command.
Installable plugin will be located in ```tea-time-travel-plugin/build/distributions``` directory.

To run Intellij Idea with installed plugin use ```./gradlew tea-time-travel-plugin:runIde```
command.

Currently, the debugger is broken due
to [bug in coroutines implementation](https://youtrack.jetbrains.com/issue/KT-47195)

## Planned features and TODOs

- Release v1.0.0
- Migrate project to KMP
- Add Github Wiki
- Add possibility to dump app's state to a file to restore debug session later
- Rework component builders and possibly replace it with some kind of DSL
- Add keyboard shortcuts for plugin, consider improving plugin UX
- Consider implementing client-server communication protocol from scratch

## Contribution

Contributions are more than welcome. If something cannot be done, not convenient, or does not work -
create an issue or PR  