package com.oliynick.max.elm.time.travel.app

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Collectors

internal class FileSystemClassLoader(files: Collection<File>) : ClassLoader() {

    private companion object {
        const val CLASS_EXTENSION = "class"
    }

    constructor(first: File, vararg other: File) : this(setOf(first, *other))

    private val files = files.mapTo(HashSet()) { file -> file.children() }.flatten()

    override fun findClass(name: String): Class<*> {
        return fromFile(name)
            .let { bytes -> defineClass(name, bytes, 0, bytes.size) }
    }

    private fun fromFile(name: String): ByteArray {
        val fileName = "${name.replace('.', File.separatorChar)}.$CLASS_EXTENSION"

        return files.find { f -> f.absolutePath.endsWith(fileName) }?.readBytes()
            ?: throw ClassNotFoundException("Couldn't find class for class $name")
    }

    private fun File.children(): Set<File> {
        return children { it.extension == CLASS_EXTENSION }
    }

}

internal fun File.children(predicate: (File) -> Boolean): Set<File> {
    return Files.walk(Paths.get(toURI()))
        .filter(Files::isRegularFile)
        .map { path -> path.toFile() }
        .filter(predicate)
        .collect(Collectors.toSet())
}