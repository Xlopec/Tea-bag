package io.github.xlopec.tea.time.travel.plugin.environment

import com.intellij.diagnostic.ActivityCategory
import com.intellij.openapi.extensions.PluginDescriptor
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Condition
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.messages.MessageBus
import org.picocontainer.PicoContainer

/**
 * NoOp implementation of [Project]
 */
open class TestProject : Project {
    override fun <T : Any?> getUserData(p0: Key<T>): T? {
        TODO("Not yet implemented")
    }

    override fun <T : Any?> putUserData(
        p0: Key<T>,
        p1: T?
    ) {
        TODO("Not yet implemented")
    }

    override fun dispose() {
        TODO("Not yet implemented")
    }

    override fun <T : Any?> getComponent(p0: Class<T>): T {
        TODO("Not yet implemented")
    }

    override fun <T : Any?> getComponents(p0: Class<T>): Array<T> {
        TODO("Not yet implemented")
    }

    override fun getPicoContainer(): PicoContainer {
        TODO("Not yet implemented")
    }

    override fun isInjectionForExtensionSupported(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getMessageBus(): MessageBus {
        TODO("Not yet implemented")
    }

    override fun isDisposed(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getDisposed(): Condition<*> {
        TODO("Not yet implemented")
    }

    override fun <T : Any?> getService(p0: Class<T>): T {
        TODO("Not yet implemented")
    }

    override fun <T : Any?> instantiateClassWithConstructorInjection(
        p0: Class<T>,
        p1: Any,
        p2: PluginId
    ): T {
        TODO("Not yet implemented")
    }

    override fun createError(
        p0: Throwable,
        p1: PluginId
    ): RuntimeException {
        TODO("Not yet implemented")
    }

    override fun createError(
        p0: String,
        p1: PluginId
    ): RuntimeException {
        TODO("Not yet implemented")
    }

    override fun createError(
        p0: String,
        p1: Throwable?,
        p2: PluginId,
        p3: MutableMap<String, String>?
    ): RuntimeException {
        TODO("Not yet implemented")
    }

    override fun <T : Any> loadClass(
        p0: String,
        p1: PluginDescriptor
    ): Class<T> {
        TODO("Not yet implemented")
    }

    override fun getActivityCategory(p0: Boolean): ActivityCategory {
        TODO("Not yet implemented")
    }

    override fun getName(): String {
        TODO("Not yet implemented")
    }

    override fun getBaseDir(): VirtualFile {
        TODO("Not yet implemented")
    }

    override fun getBasePath(): String? {
        TODO("Not yet implemented")
    }

    override fun getProjectFile(): VirtualFile? {
        TODO("Not yet implemented")
    }

    override fun getProjectFilePath(): String? {
        TODO("Not yet implemented")
    }

    override fun getWorkspaceFile(): VirtualFile? {
        TODO("Not yet implemented")
    }

    override fun getLocationHash(): String {
        TODO("Not yet implemented")
    }

    override fun save() {
        TODO("Not yet implemented")
    }

    override fun isOpen(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isInitialized(): Boolean {
        TODO("Not yet implemented")
    }
}
