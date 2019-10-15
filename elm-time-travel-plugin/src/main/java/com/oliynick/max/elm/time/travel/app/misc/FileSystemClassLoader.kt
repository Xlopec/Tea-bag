/*
 * Copyright (C) 2019 Maksym Oliinyk.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.oliynick.max.elm.time.travel.app.misc

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