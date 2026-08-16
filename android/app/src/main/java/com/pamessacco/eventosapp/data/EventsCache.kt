package com.pamessacco.eventosapp.data

import android.content.Context
import java.io.File

/** Cachea el JSON crudo en disco para poder mostrar algo aunque no haya internet. */
class EventsCache(context: Context) {
    private val file: File = File(context.filesDir, "events_cache.json")

    fun read(): String? = if (file.exists()) file.readText() else null

    fun write(json: String) {
        file.writeText(json)
    }

    fun lastModifiedMillis(): Long? = if (file.exists()) file.lastModified() else null
}
