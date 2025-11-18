package io.github.xlopec.tea.compose

import androidx.compose.runtime.AbstractApplier

internal object NoOpApplier : AbstractApplier<Unit>(Unit) {
    override fun insertTopDown(index: Int, instance: Unit) {
        // no-op
    }

    override fun insertBottomUp(index: Int, instance: Unit) {
        // no-op
    }

    override fun remove(index: Int, count: Int) {
        // no-op
    }

    override fun move(from: Int, to: Int, count: Int) {
        // no-op
    }

    override fun onClear() {
        // no-op
    }
}
