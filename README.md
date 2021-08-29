# TEA Bag

The Elm Architecture implementation in Kotlin.

## What's This?
TEA Core is the most simple implementation of [TEA](https://guide.elm-lang.org/architecture/) architecture
written in Kotlin. This library is based on Kotlin's coroutines and extensively uses extension-based approach.

This library isn't production ready yet and was originally intended as pet project to give TEA a try. Later I found that 
it'd be nice to make it more simple and lightweight than analogs, add debugging capabilities...

## Main Features
- **Scalability** it is build on the top of a simple idea of having pure functions that operate on plain data separated from impure one.
Those functions are building blocks and form testable components that can be combined to build complex applications
- **Simplicity** component implementation resides in a single file
- **Extensibility** additional functionality and API is implemented as component extensions which means you can 
easily add your own
- **Debugger** [Intellij debugger plugin](https://plugins.jetbrains.com/plugin/14254-time-travel-debugger) is available for this library, though it's not production ready yet

<p align="center">
  <img alt="Demo" src="demoRes/demo.gif">
</p>

## Main Modules

- **tea-core** - contains core types along with basic component implementation
- **tea-time-travel** - contains debuggable version of the component
- **tea-time-travel-adapter-gson** - implements debug protocol and serialization by means of [Gson](https://github.com/google/gson) library. Should 
be added as dependency together with **tea-time-travel** module
- **tea-time-travel-protocol** - contains debug protocol types definitions
- **tea-time-travel-plugin** - contains Intellij plugin implementation

## Gradle

Add the dependency:

```kotlin
implementation("io.github.xlopec:tea-core:[version]")
implementation("io.github.xlopec:tea-time-travel:[version]")
implementation("io.github.xlopec:tea-time-travel-adapter-gson:[version]")
implementation("io.github.xlopec:tea-time-travel-protocol:[version]")
```

## Notes

Android application that use this library and demonstrates how it can be used together with Jetpack Compose can be found [here](https://github.com/Xlopec/Tea-bag/tree/master/app).

To build plugin from sources use ```./gradlew tea-time-travel-plugin:buildPlugin``` command. Installable plugin will be located
in ```tea-time-travel-plugin/build/distributions``` directory.

To run Intellij Idea with installed plugin use ```./gradlew tea-time-travel-plugin:runIde``` command.

Currently, the debugger is broken due to [bug in coroutines implementation](https://youtrack.jetbrains.com/issue/KT-47195)

## Planned features and TODOs
- Release v1.0.0
- Migrate project to KMP
- Add Github Wiki
- Add possibility to dump app's state to a file to restore debug session later
- Rework component builders and possibly replace it with some kind of DSL
- Add keyboard shortcuts for plugin, consider improving plugin UX