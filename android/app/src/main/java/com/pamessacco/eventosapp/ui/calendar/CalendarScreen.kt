package com.pamessacco.eventosapp.ui.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pamessacco.eventosapp.EventsViewModel
import com.pamessacco.eventosapp.data.EventItem
import com.pamessacco.eventosapp.ui.components.EventCard
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: EventsViewModel,
    onEventoClick: (EventItem) -> Unit,
    onDiaClick: (LocalDate) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Eventos") },
                actions = {
                    IconButton(onClick = { viewModel.cargar() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Actualizar")
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            if (state.errorMessage != null) {
                Surface(color = MaterialTheme.colorScheme.errorContainer, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (state.usandoCache)
                            "No se pudo actualizar (${state.errorMessage}). Mostrando lo último guardado."
                        else "No se pudo cargar: ${state.errorMessage}",
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            OutlinedTextField(
                value = state.busqueda,
                onValueChange = viewModel::setBusqueda,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Buscar por artista, venue o ciudad") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(onClick = { viewModel.cambiarMes(-1) }) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = "Mes anterior")
                }
                Text(state.mesVisible.nombreCapitalizado(), style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = { viewModel.cambiarMes(1) }) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = "Mes siguiente")
                }
            }

            val filtrados = viewModel.eventosFiltrados(state)
            val diasConEventos = viewModel.diasConEventos(filtrados, state.mesVisible)

            MonthCalendar(
                mes = state.mesVisible,
                diasConEventos = diasConEventos,
                onDiaClick = onDiaClick,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            )

            Spacer(modifier = Modifier.height(8.dp))

            val proximos = filtrados
                .filter { it.fechaInicio != null && !it.fechaInicio!!.toLocalDate().isBefore(LocalDate.now()) }
                .sortedBy { it.fechaInicio }
                .take(50)

            if (state.loading && state.events.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (proximos.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay eventos para mostrar", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                Text(
                    text = "Próximos eventos",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(proximos, key = { it.id }) { evento ->
                        EventCard(evento = evento, onClick = { onEventoClick(evento) })
                    }
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }
}
