package com.max.weatherviewer.persistence

// dumb but working solution, just for example

/*
suspend inline fun <reified T> Context.load(gson: Gson, crossinline ifNone: () -> T): T {
    return withContext(Dispatchers.IO) {
        val file = cacheFile(T::class.java.fileName)

        val state = runCatching { gson.fromJson(FileReader(file), T::class.java) }
            .onFailure { file.delete() }
            .getOrElse { ifNone() }
            ?: ifNone()

        state
    }
}

suspend inline fun <reified T> Context.persist(gson: Gson, state: T) {
    return withContext(Dispatchers.IO) { cacheFile(T::class.java.fileName).writeText(gson.toJson(state)) }
}

val Class<*>.fileName get() = "$simpleName.json"
fun Context.cacheFile(filename: String): File = File(cacheDir, filename).also { it.createNewFile() }*/
