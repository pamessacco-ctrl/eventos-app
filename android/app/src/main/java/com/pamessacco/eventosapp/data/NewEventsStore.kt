package com.pamessacco.eventosapp.data

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.time.LocalDate

@Serializable
private data class PrimeraAparicionDto(val vistos: Map<String, String> = emptyMap())

/**
 * Guarda en disco, por evento, la fecha en que este dispositivo lo vio por
 * primera vez. Sirve para la sección "Recién agregados": un evento que
 * antes no estaba en el feed y ahora sí, es "nuevo". Se poda automáticamente
 * lo que ya no aparece en el feed actual (eventos vencidos o que el sitio
 * de origen sacó de cartelera), para no crecer sin límite.
 */
class NewEventsStore(context: Context) {
    private val file = File(context.filesDir, "primeras_apariciones.json")
    private val json = Json { ignoreUnknownKeys = true }

    /** Actualiza el registro con los ids actuales y devuelve fecha de primera vez por id. */
    fun registrarYObtener(idsActuales: Collection<String>): Map<String, LocalDate> {
        val original = leer()
        val actualesSet = idsActuales.toSet()
        val hoy = LocalDate.now().toString()

        val actualizado = original.toMutableMap()
        for (id in actualesSet) {
            actualizado.putIfAbsent(id, hoy)
        }
        val podado = actualizado.filterKeys { it in actualesSet }

        if (podado != original) {
            escribir(podado)
        }
        return podado.mapValues { (_, fecha) -> LocalDate.parse(fecha) }
    }

    private fun leer(): Map<String, String> {
        if (!file.exists()) return emptyMap()
        return try {
            json.decodeFromString(PrimeraAparicionDto.serializer(), file.readText()).vistos
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private fun escribir(mapa: Map<String, String>) {
        try {
            file.writeText(json.encodeToString(PrimeraAparicionDto.serializer(), PrimeraAparicionDto(mapa)))
        } catch (e: Exception) {
            // si falla el guardado no rompemos la app; simplemente no persiste esta vez
        }
    }
}
