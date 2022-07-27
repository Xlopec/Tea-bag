package io.github.xlopec.tea.time.travel.plugin.util

import com.intellij.openapi.diagnostic.Logger
import org.apache.log4j.Level

open class LoggerStub : Logger() {
    override fun isDebugEnabled(): Boolean {
        TODO("Not yet implemented")
    }

    override fun debug(p0: String?) {
        TODO("Not yet implemented")
    }

    override fun debug(p0: Throwable?) {
        TODO("Not yet implemented")
    }

    override fun debug(p0: String?, p1: Throwable?) {
        TODO("Not yet implemented")
    }

    override fun info(p0: String?) {
        TODO("Not yet implemented")
    }

    override fun info(p0: String?, p1: Throwable?) {
        TODO("Not yet implemented")
    }

    override fun warn(p0: String?, p1: Throwable?) {
        TODO("Not yet implemented")
    }

    override fun error(p0: String?, p1: Throwable?, vararg p2: String?) {
        TODO("Not yet implemented")
    }

    @Deprecated("Deprecated in Java")
    override fun setLevel(@Suppress("UnstableApiUsage") p0: Level) {
        TODO("Not yet implemented")
    }
}
