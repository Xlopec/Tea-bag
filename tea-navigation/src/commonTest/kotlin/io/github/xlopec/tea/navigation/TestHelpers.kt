package io.github.xlopec.tea.navigation

/**
 * Minimal NavStackEntry. [payload] lets tests represent "same id, different
 * data" — the shape mutations produced by `stack.mutate { updateInstanceOfById(...) }`
 * in real callers, where a screen's inner state changes without a push/pop.
 */
internal data class TestEntry(override val id: String, val payload: Int = 0) : NavStackEntry<String>

/** Stack from string ids. The first id is the bottom, the last is the top. */
internal fun stack(vararg ids: String): NavigationStack<TestEntry> =
    stackOf(TestEntry(ids.first()), ids.drop(1).map(::TestEntry))
