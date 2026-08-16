package com.pamessacco.eventosapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pamessacco.eventosapp.data.EventItem
import com.pamessacco.eventosapp.data.EventsRepository
import com.pamessacco.eventosapp.data.EventsResult
import com.pamessacco.eventosapp.data.NewEventsStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

private const val DIAS_RECIENTES = 3L

data class UiState(
    val loading: Boolean = true,
    val events: List<EventItem> = emptyList(),
    val errorMessage: String? = null,
    val usandoCache: Boolean = false,
    val ultimaActualizacion: String? = null,
    val busqueda: String = "",
    val provinciaFiltro: String? = null,
    val primeraAparicion: Map<String, LocalDate> = emptyMap(),
)

class EventsViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = EventsRepository(application)
    private val nuevosStore = NewEventsStore(application)

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
                is EventsResult.Success -> {
                    val primeraAparicion = nuevosStore.registrarYObtener(result.events.map { it.id })
                    _state.update {
                        it.copy(
                            loading = false,
                            events = result.events,
                            usandoCache = false,
                            ultimaActualizacion = result.generadoIso,
                            errorMessage = null,
                            primeraAparicion = primeraAparicion,
                        )
                    }
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

    fun setProvinciaFiltro(provincia: String?) {
        _state.update { it.copy(provinciaFiltro = provincia) }
    }

    /** Eventos que matchean los filtros activos (búsqueda + provincia), ordenados por fecha. */
    fun eventosFiltrados(state: UiState): List<EventItem> {
        val texto = state.busqueda.trim().lowercase()
        val hoy = LocalDate.now()
        return state.events
            .filter { ev ->
                (ev.fechaInicio == null || !ev.fechaInicio.toLocalDate().isBefore(hoy)) &&
                    (state.provinciaFiltro == null || ev.provincia == state.provinciaFiltro) &&
                    (texto.isEmpty() ||
                        ev.titulo.lowercase().contains(texto) ||
                        (ev.venue?.lowercase()?.contains(texto) == true) ||
                        (ev.ciudad?.lowercase()?.contains(texto) == true))
            }
            .sortedBy { it.fechaInicio }
    }

    /** Provincias distintas disponibles entre los eventos cargados, para el desplegable. */
    fun provinciasDisponibles(state: UiState): List<String> =
        state.events.mapNotNull { it.provincia }.distinct().sorted()

    /**
     * Eventos (dentro de la lista dada, ya próximos) cuya primera aparición
     * en este dispositivo fue dentro de los últimos [DIAS_RECIENTES] días.
     */
    fun eventosRecienAgregados(state: UiState, proximos: List<EventItem>): List<EventItem> {
        val limite = LocalDate.now().minusDays(DIAS_RECIENTES)
        return proximos
            .filter { ev -> (state.primeraAparicion[ev.id] ?: LocalDate.MIN) >= limite }
            .sortedByDescending { state.primeraAparicion[it.id] }
    }

    fun eventoPorId(id: String): EventItem? = _state.value.events.firstOrNull { it.id == id }
}
