/*
 * MIT License
 *
 * Copyright (c) 2026. Maksym Oliinyk.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.xlopec.tea.compose

/**
 * Policies for snapshot change notification in a composition.
 *
 * Compose uses a snapshot system to maintain state consistency. However, mutations occurring outside
 * of the composition's immediate context are not automatically observed, and notification of these
 * changes must be manually dispatched.
 *
 * This is typically managed by registering a global write observer, with at least one required per process.
 * Applications using other Compose-based frameworks (like Compose UI) usually have this mechanism in place.
 * Applications using only this library can rely on its automatic notifier registration.
 *
 * @see androidx.compose.runtime.snapshots.Snapshot.sendApplyNotifications
 */
public enum class SnapshotNotifierPolicy {
    /**
     * Delegates snapshot change notification to an external system.
     *
     * This policy should only be used if another component in the application is guaranteed to listen for
     * global snapshot writes and send apply notifications. This is common when using other Compose-based
     * frameworks that are initialized alongside or before the composition.
     *
     * Warning: If no external system sends apply notifications, state changes will not trigger recomposition.
     *
     * Examples:
     * - **Android with Compose UI**: Calling `setContent { }` on an `Activity` or `ComposeView`
     *   automatically starts a global snapshot write listener.
     * - **Compose HTML**: The framework manages snapshot notifications internally.
     * - **Compose UI for Desktop**: Calling `application { }` ensures the global snapshot listener
     *   is active, even without showing a window.
     */
    External,

    /**
     * Automatically manages snapshot notifications during the composition's lifecycle.
     *
     * Registers a global snapshot write observer and dispatches apply notifications via a coroutine
     * launched in the composition's scope. The observer is automatically unregistered and the coroutine
     * canceled when the composition is disposed.
     */
    WhileActive,
}
