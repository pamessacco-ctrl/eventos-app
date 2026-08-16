package com.pamessacco.eventosapp.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

private const val EVENTS_URL =
    "https://raw.githubusercontent.com/pamessacco-ctrl/eventos-app/main/backend/output/events.json"

private val json = Json { ignoreUnknownKeys = true }

sealed class EventsResult {
    data class Success(val events: List<EventItem>, val generadoIso: String?, val fromCache: Boolean) : EventsResult()
    data class Error(val message: String, val cached: List<EventItem>?) : EventsResult()
}

class EventsRepository(context: Context) {
    private val cache = EventsCache(context.applicationContext)
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun refresh(): EventsResult = withContext(Dispatchers.IO) {
        try {
            val body = fetch(EVENTS_URL)
            cache.write(body)
            val feed = json.decodeFromString(EventsFeed.serializer(), body)
            EventsResult.Success(
                events = feed.eventos.map { it.toEventItem() },
                generadoIso = feed.generado,
                fromCache = false,
            )
        } catch (e: Exception) {
            val cached = loadFromCacheOrNull()
            EventsResult.Error(e.message ?: "No se pudo actualizar", cached)
        }
    }

    /** Para arrancar rápido mostrando lo último guardado, sin esperar la red. */
    fun peekCache(): List<EventItem>? = loadFromCacheOrNull()

    private fun loadFromCacheOrNull(): List<EventItem>? {
        val raw = cache.read() ?: return null
        return try {
            json.decodeFromString(EventsFeed.serializer(), raw).eventos.map { it.toEventItem() }
        } catch (e: Exception) {
            null
        }
    }

    private fun fetch(url: String): String {
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response: okhttp3.Response ->
            if (!response.isSuccessful) throw IOException("HTTP ${response.code}")
            return response.body?.string() ?: throw IOException("Respuesta vacía")
        }
    }
}
