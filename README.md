# TEA Core
[![Version](https://jitpack.io/v/Xlopec/TEA-core.svg)](https://jitpack.io/#Xlopec/TEA-core)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Build Status](https://travis-ci.org/Xlopec/Tea-bag.svg?branch=master)](https://travis-ci.org/Xlopec/TEA-core)

The Elm Architecture implementation in Kotlin.

## What's This?
TEA Core is the most simple implementation of [TEA](https://guide.elm-lang.org/architecture/) architecture
written in Kotlin. This library is based on Kotlin's coroutines and extensively uses extension-based approach.

<p align="center">
  <img alt="Demo" src="demoRes/demo.gif">
</p>

## Main Features
- **Scaleability** it is build on the top of a simple idea of having pure functions that operate on plain data separated from impure one.
Those functions are building blocks and form testable components that can be combined to build complex applications
- **Component binding** components can be bound to each other in any way with automatic lifecycle handling
- **Simplicity** component implementation resides in a single file
- **Extensibility** additional functionality and API is implemented as component extensions which means you can 
easily add your own
- **Debugger** Intellij debugger plugin is available for this library

## Gradle

You have to add the maven repo to your root `build.gradle`

```groovy
allprojects {
    repositories {
        maven { setUrl("https://jitpack.io") }
    }
}
```

Add the dependency:

```kotlin
implementation("com.github.Xlopec:TEA-core:0.0.1-alpha1")
```

## Quick Sample
To show usage case consider a simple example where we want to add items to our TODO list.

Our state will reside in `TodoState` class:

```kotlin
inline class TodoState(val items: List<Item> = emptyList())

data class Item(val what: String) {
    init {
        require(what.isNotBlank() && what.isNotEmpty())
    }
}
```

Messages that trigger state updates:

```kotlin
sealed class Message

data class AddItem(val item: Item) : Message()

data class Updated(val items: List<Item>) : Message()

data class RemoveItem(val item: Item) : Message()
```

And commands that mutate list state

```kotlin
sealed class Command

data class DoAddItem(val item: Item, val from: List<Item>) : Command()

data class DoRemoveItem(val item: Item, val from: List<Item>) : Command()
```

And, finally, our pure `update` and impure `resolve` functions

```kotlin
private suspend fun resolve(cmd: Command): Set<Message> =
    when (cmd) {
        is DoAddItem -> cmd.effect { Updated(from + item) }
        is DoRemoveItem -> cmd.effect { Updated(from - item) }
    }

private fun update(message: Message, state: TodoState): UpdateWith<TodoState, Command> =
    when (message) {
        is Updated -> TodoState(message.items).noCommand()
        is AddItem -> state command DoAddItem(message.item, state.items)
        is RemoveItem -> state command DoRemoveItem(message.item, state.items)
    }
```

To create and use component we should add the following piece of code:

```kotlin
    val todoComponent = Component(Initializer(TodoState()), ::resolve, ::update)
    
    scope.launch { todoComponent(AddItem(Item("some"))).collect { state -> println(state) } }
    scope.launch { todoComponent(AddItem(Item("foo"))).collect { state -> println(state) } }
````

## TODO
- Improve documentation
- Add Github Wiki
- Increase test coverage
- Add possibility to dump plugin's state to a file
- Prepare for release