package io.github.xlopec.tea.compose

/**
 * Clock policy defines which clock should be used for composition.
 */
public enum class ClockPolicy {
    /**
     * Use external clock.
     */
    External,

    /**
     * Use internal clock.
     */
    Internal
}
