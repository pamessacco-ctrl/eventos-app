package com.pamessacco.eventosapp.ui.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pamessacco.eventosapp.data.EventItem
import com.pamessacco.eventosapp.ui.components.EventCard
import com.pamessacco.eventosapp.util.LOCALE_ES_AR
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val tituloFormatter = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", LOCALE_ES_AR)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayEventsScreen(
    dia: LocalDate,
    eventos: List<EventItem>,
    onBack: () -> Unit,
    onEventoClick: (EventItem) -> Unit,
) {
    val titulo = dia.format(tituloFormatter).replaceFirstChar { it.uppercase() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titulo) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        },
    ) { padding ->
        if (eventos.isEmpty()) {
            Box(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text("No hay eventos ese día", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    Text(
                        text = "${eventos.size} evento${if (eventos.size == 1) "" else "s"}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                }
                items(eventos, key = { it.id }) { evento ->
                    EventCard(evento = evento, onClick = { onEventoClick(evento) })
                }
            }
        }
    }
}
