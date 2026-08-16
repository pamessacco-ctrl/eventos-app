package com.pamessacco.eventosapp.ui.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pamessacco.eventosapp.EventsViewModel
import com.pamessacco.eventosapp.data.EventItem
import com.pamessacco.eventosapp.ui.components.EventCard
import java.time.LocalDate

/**
 * Sin búsqueda ni filtro activo, muestra los eventos de HOY (como el resto de
 * las apps de este tipo). En cuanto el usuario escribe algo o elige una
 * provincia, pasa a mostrar todos los próximos que matchean, no solo hoy.
 */
@Composable
fun SearchScreen(viewModel: EventsViewModel, onEventoClick: (EventItem) -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {

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

        val provincias = viewModel.provinciasDisponibles(state)
        FiltroDesplegable(
            etiqueta = "Provincia",
            opciones = provincias,
            seleccionado = state.provinciaFiltro,
            onSeleccionar = viewModel::setProvinciaFiltro,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.height(8.dp))

        val sinFiltrosActivos = state.busqueda.isBlank() && state.provinciaFiltro == null
        val filtrados = viewModel.eventosFiltrados(state)
        val mostrar = if (sinFiltrosActivos) {
            filtrados.filter { it.fechaInicio?.toLocalDate() == LocalDate.now() }
        } else {
            filtrados
        }

        if (state.loading && state.events.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (mostrar.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = if (sinFiltrosActivos) "No hay eventos hoy" else "No hay eventos para mostrar",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        } else {
            Text(
                text = if (sinFiltrosActivos) "Hoy" else "${mostrar.size} evento${if (mostrar.size == 1) "" else "s"}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(mostrar, key = { it.id }) { evento ->
                    EventCard(evento = evento, onClick = { onEventoClick(evento) })
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FiltroDesplegable(
    etiqueta: String,
    opciones: List<String>,
    seleccionado: String?,
    onSeleccionar: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expandido by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expandido,
        onExpandedChange = { expandido = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = seleccionado ?: "Todas",
            onValueChange = {},
            readOnly = true,
            label = { Text(etiqueta) },
            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
            maxLines = 1,
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
            DropdownMenuItem(
                text = { Text("Todas") },
                onClick = { onSeleccionar(null); expandido = false },
            )
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    onClick = { onSeleccionar(opcion); expandido = false },
                )
            }
        }
    }
}
