package io.github.xlopec.tea.navigation

/**
 * Represents navigation stack entry, each navigation entry should provide a unique identifier
 */
public interface NavStackEntry<out T : Any> {
    /**
     * Unique stack entry identifier
     */
    public val id: T
}
