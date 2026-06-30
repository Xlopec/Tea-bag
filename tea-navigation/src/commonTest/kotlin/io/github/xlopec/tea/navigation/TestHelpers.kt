package io.github.xlopec.tea.navigation

/** Minimal NavStackEntry whose id is the value itself. */
internal data class TestEntry(override val id: String) : NavStackEntry<String>

/** Stack from string ids. The first id is the bottom, the last is the top. */
internal fun stack(vararg ids: String): NavigationStack<TestEntry> =
    stackOf(TestEntry(ids.first()), ids.drop(1).map(::TestEntry))
