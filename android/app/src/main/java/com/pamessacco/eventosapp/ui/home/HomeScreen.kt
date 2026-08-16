package com.pamessacco.eventosapp.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pamessacco.eventosapp.EventsViewModel
import com.pamessacco.eventosapp.data.EventItem
import com.pamessacco.eventosapp.ui.components.EventCard
import com.pamessacco.eventosapp.ui.components.FeaturedEventCard
import kotlinx.coroutines.launch
import java.time.LocalDate

private const val CANTIDAD_DESTACADOS = 10
private val PESTAÑAS_LISTA = listOf("Próximos eventos", "Recién agregados")

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
    // el show más próximo de cada uno como representante, ordenados por qué
    // tan destacado/masivo es el artista (ránking curado en el backend).
    val destacados = proximos
        .filter { it.artistaInternacional != null }
        .distinctBy { it.artistaInternacional }
        .sortedBy { it.prioridadDestacado }
        .take(CANTIDAD_DESTACADOS)

    val recienAgregados = viewModel.eventosRecienAgregados(state, proximos)

    Column(modifier = Modifier.fillMaxSize()) {
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

        // "Próximos eventos" y "Recién agregados" como dos páginas deslizables
        // horizontalmente, cada una con su propia lista vertical.
        val listasPorPagina = listOf(proximos, recienAgregados)
        val pagerState = rememberPagerState(pageCount = { PESTAÑAS_LISTA.size })
        val scope = rememberCoroutineScope()

        TabRow(selectedTabIndex = pagerState.currentPage) {
            PESTAÑAS_LISTA.forEachIndexed { index, titulo ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                    text = {
                        val etiqueta = if (index == 1 && listasPorPagina[1].isNotEmpty())
                            "$titulo (${listasPorPagina[1].size})"
                        else titulo
                        Text(etiqueta)
                    },
                )
            }
        }

        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
            val lista = listasPorPagina[page]
            if (lista.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (page == 1) "No hay eventos nuevos por ahora" else "No hay eventos para mostrar",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(lista, key = { it.id }) { evento ->
                        EventCard(evento = evento, onClick = { onEventoClick(evento) })
                    }
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }
}
