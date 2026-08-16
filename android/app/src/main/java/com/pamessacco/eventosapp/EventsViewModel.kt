package com.pamessacco.eventosapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pamessacco.eventosapp.data.EventItem
import com.pamessacco.eventosapp.data.EventsRepository
import com.pamessacco.eventosapp.data.EventsResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class UiState(
    val loading: Boolean = true,
    val events: List<EventItem> = emptyList(),
    val errorMessage: String? = null,
    val usandoCache: Boolean = false,
    val ultimaActualizacion: String? = null,
    val busqueda: String = "",
    val localidadFiltro: String? = null,
)

class EventsViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = EventsRepository(application)

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    init {
        // mostrar algo ya guardado mientras se actualiza en segundo plano
        repo.peekCache()?.let { cached ->
            _state.update { it.copy(events = cached, usandoCache = true) }
        }
        cargar()
    }

    fun cargar() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, errorMessage = null) }
            when (val result = repo.refresh()) {
                is EventsResult.Success -> _state.update {
                    it.copy(
                        loading = false,
                        events = result.events,
                        usandoCache = false,
                        ultimaActualizacion = result.generadoIso,
                        errorMessage = null,
                    )
                }
                is EventsResult.Error -> _state.update {
                    it.copy(
                        loading = false,
                        events = result.cached ?: it.events,
                        usandoCache = result.cached != null,
                        errorMessage = result.message,
                    )
                }
            }
        }
    }

    fun setBusqueda(texto: String) {
        _state.update { it.copy(busqueda = texto) }
    }

    fun setLocalidadFiltro(localidad: String?) {
        _state.update { it.copy(localidadFiltro = localidad) }
    }

    /** Eventos que matchean los filtros activos (búsqueda + localidad), ordenados por fecha. */
    fun eventosFiltrados(state: UiState): List<EventItem> {
        val texto = state.busqueda.trim().lowercase()
        val hoy = LocalDate.now()
        return state.events
            .filter { ev ->
                (ev.fechaInicio == null || !ev.fechaInicio.toLocalDate().isBefore(hoy)) &&
                    (state.localidadFiltro == null || ev.ciudad == state.localidadFiltro) &&
                    (texto.isEmpty() ||
                        ev.titulo.lowercase().contains(texto) ||
                        (ev.venue?.lowercase()?.contains(texto) == true) ||
                        (ev.ciudad?.lowercase()?.contains(texto) == true))
            }
            .sortedBy { it.fechaInicio }
    }

    /** Localidades distintas disponibles entre los eventos cargados, para el desplegable. */
    fun localidadesDisponibles(state: UiState): List<String> =
        state.events.mapNotNull { it.ciudad }.distinct().sorted()

    fun eventoPorId(id: String): EventItem? = _state.value.events.firstOrNull { it.id == id }
}
