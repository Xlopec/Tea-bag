# TEA Core
The Elm Architecture implementation in Kotlin for Android.

## What' this?
TEA Core is the most simple implementation of [TEA](https://guide.elm-lang.org/architecture/) architecture for Android
written in Kotlin. This library is based on Kotlin's coroutines and extensively uses extension-based approach.

## Main features
- **Scaleability** it is build on the top of a simple idea of having pure functions that operate on plain data separated from impure one.
Those functions are building blocks and form testable components that can be combined to build complex applications
- **Component binding** components can be bound to each other in any way with automatic lifecycle handling
- **Simplicity** component is implemented in 50 loc
- **Extensibility** additional functionality and API is implemented as component extensions which means you can 
easily add your own

## How to run?
To run sample you should build [Compose library](https://developer.android.com/jetpack/compose) by following official instructions or
you can unzip [prebuilt library](https://drive.google.com/file/d/1WRpBPyQwAxC5kUjI89g-2tm_I0JHIQbd/view?usp=sharing) to the project's root directory

## TODO
- increase test coverage
- add test for corner cases
- implement persistence API (ability to store and load data from DB, file, etc.)
- finish logging API
- consider time travelling debugger