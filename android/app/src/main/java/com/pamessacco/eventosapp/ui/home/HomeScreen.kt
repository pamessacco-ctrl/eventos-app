package com.pamessacco.eventosapp.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pamessacco.eventosapp.EventsViewModel
import com.pamessacco.eventosapp.data.EventItem
import com.pamessacco.eventosapp.ui.components.EventCard
import com.pamessacco.eventosapp.ui.components.FeaturedEventCard
import java.time.LocalDate

private const val CANTIDAD_DESTACADOS = 5

@Composable
fun HomeScreen(viewModel: EventsViewModel, onEventoClick: (EventItem) -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    if (state.loading && state.events.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val proximos = state.events
        .filter { it.fechaInicio != null && !it.fechaInicio.toLocalDate().isBefore(LocalDate.now()) }
        .sortedBy { it.fechaInicio }

    if (proximos.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay eventos para mostrar", style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    // Destacados = artistas internacionales (lista curada en el backend),
    // agrupados por artista (un mismo artista puede tocar en varias
    // provincias y no queremos que ocupe varios lugares del carrusel), con
    // el show más próximo de cada uno como representante, pero ORDENADOS
    // por qué tan destacado/masivo es el artista (prioridadDestacado, ránking
    // curado en el backend) y no por fecha. Se recalcula solo cada vez que
    // cambian los eventos cargados (nueva sincronización), no hace falta
    // nada manual.
    val destacados = proximos
        .filter { it.artistaInternacional != null }
        .distinctBy { it.artistaInternacional }
        .sortedBy { it.prioridadDestacado }
        .take(CANTIDAD_DESTACADOS)

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Text(
                text = "Destacados",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )
            if (destacados.isEmpty()) {
                Text(
                    text = "No hay artistas internacionales próximamente",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(destacados, key = { "destacado-${it.id}" }) { evento ->
                        FeaturedEventCard(evento = evento, onClick = { onEventoClick(evento) })
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Text(
                text = "Próximos eventos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
        items(proximos, key = { it.id }) { evento ->
            EventCard(
                evento = evento,
                onClick = { onEventoClick(evento) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}
