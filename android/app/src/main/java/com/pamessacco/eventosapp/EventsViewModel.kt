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
import java.time.YearMonth

data class UiState(
    val loading: Boolean = true,
    val events: List<EventItem> = emptyList(),
    val errorMessage: String? = null,
    val usandoCache: Boolean = false,
    val ultimaActualizacion: String? = null,
    val mesVisible: YearMonth = YearMonth.now(),
    val diaSeleccionado: LocalDate? = null,
    val busqueda: String = "",
    val fuenteFiltro: String? = null,
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

    fun cambiarMes(delta: Long) {
        _state.update { it.copy(mesVisible = it.mesVisible.plusMonths(delta), diaSeleccionado = null) }
    }

    fun seleccionarDia(dia: LocalDate?) {
        _state.update { it.copy(diaSeleccionado = dia) }
    }

    fun setBusqueda(texto: String) {
        _state.update { it.copy(busqueda = texto) }
    }

    fun setFuenteFiltro(fuente: String?) {
        _state.update { it.copy(fuenteFiltro = fuente) }
    }

    /** Eventos que matchean los filtros activos (búsqueda + fuente), sin importar el día. */
    fun eventosFiltrados(state: UiState): List<EventItem> {
        val texto = state.busqueda.trim().lowercase()
        return state.events.filter { ev ->
            (state.fuenteFiltro == null || ev.fuente == state.fuenteFiltro) &&
                (texto.isEmpty() ||
                    ev.titulo.lowercase().contains(texto) ||
                    (ev.venue?.lowercase()?.contains(texto) == true) ||
                    (ev.ciudad?.lowercase()?.contains(texto) == true))
        }
    }

    /** Días (dentro del mes visible) que tienen al menos un evento, para pintar el punto en el calendario. */
    fun diasConEventos(eventos: List<EventItem>, mes: YearMonth): Set<LocalDate> =
        eventos.mapNotNull { it.fechaInicio?.toLocalDate() }
            .filter { YearMonth.from(it) == mes }
            .toSet()

    fun eventoPorId(id: String): EventItem? = _state.value.events.firstOrNull { it.id == id }
}
