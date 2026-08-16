package com.pamessacco.eventosapp.data

import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val BA_ZONE: ZoneId = ZoneId.of("America/Argentina/Buenos_Aires")

/** Forma cruda tal cual viene en events.json (backend/scrapers/base.py -> Event.to_dict()). */
@Serializable
data class EventDto(
    val id: String = "",
    val titulo: String = "",
    val venue: String? = null,
    val ciudad: String? = null,
    val provincia: String? = null,
    val fecha_inicio: String? = null,
    val fecha_fin: String? = null,
    val categoria: String? = null,
    val precio_desde: Double? = null,
    val moneda: String = "ARS",
    val imagen_url: String? = null,
    val ticket_url: String? = null,
    val fuente: String = "",
)

@Serializable
data class EventsFeed(
    val generado: String = "",
    val total_eventos: Int = 0,
    val eventos: List<EventDto> = emptyList(),
)

/** Modelo ya "resuelto" que usa la UI: fecha parseada, o null si no se pudo interpretar. */
data class EventItem(
    val id: String,
    val titulo: String,
    val venue: String?,
    val ciudad: String?,
    val provincia: String?,
    val fechaInicio: LocalDateTime?,
    val tieneHora: Boolean,
    val categoria: String?,
    val precioDesde: Double?,
    val moneda: String,
    val imagenUrl: String?,
    val ticketUrl: String?,
    val fuente: String,
)

fun EventDto.toEventItem(): EventItem {
    val (fecha, tieneHora) = parseFecha(fecha_inicio)
    return EventItem(
        id = id,
        titulo = titulo.trim(),
        venue = venue?.trim()?.takeIf { it.isNotEmpty() },
        ciudad = ciudad?.trim()?.takeIf { it.isNotEmpty() },
        provincia = provincia?.trim()?.takeIf { it.isNotEmpty() },
        fechaInicio = fecha,
        tieneHora = tieneHora,
        categoria = categoria,
        precioDesde = precio_desde,
        moneda = moneda,
        imagenUrl = imagen_url,
        ticketUrl = ticket_url,
        fuente = fuente,
    )
}

/**
 * Los scrapers no siempre entregan el mismo formato de fecha:
 *  - con offset:      "2026-08-16T21:00:00-03:00"
 *  - en UTC ("Z"):     "2026-08-16T01:00:00.000Z"
 *  - sin offset (ya en hora local de Argentina): "2026-08-16T20:00:00"
 *  - solo fecha:       "2026-08-15"
 * Normalizamos todo a la hora de Buenos Aires.
 */
private fun parseFecha(raw: String?): Pair<LocalDateTime?, Boolean> {
    if (raw.isNullOrBlank()) return null to false
    return try {
        when {
            raw.endsWith("Z") || Regex(""".*[+-]\d{2}:\d{2}$""").matches(raw) -> {
                val odt = OffsetDateTime.parse(raw, DateTimeFormatter.ISO_DATE_TIME)
                odt.atZoneSameInstant(BA_ZONE).toLocalDateTime() to true
            }
            raw.length == 10 -> LocalDate.parse(raw).atStartOfDay() to false
            else -> LocalDateTime.parse(raw) to true
        }
    } catch (e: Exception) {
        null to false
    }
}
