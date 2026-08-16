package com.pamessacco.eventosapp.ui.detail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.pamessacco.eventosapp.data.EventItem
import com.pamessacco.eventosapp.util.LOCALE_ES_AR
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val fechaCompletaFormatter =
    DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'de' yyyy · HH:mm 'hs'", LOCALE_ES_AR)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(evento: EventItem?, onBack: () -> Unit) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(evento?.titulo ?: "Evento") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        },
    ) { padding ->
        if (evento == null) {
            Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("No se encontró el evento")
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            AsyncImage(
                model = evento.imagenUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp)),
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(evento.titulo, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            evento.fechaInicio?.let {
                val texto = if (evento.tieneHora) it.format(fechaCompletaFormatter)
                else it.format(DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'de' yyyy", LOCALE_ES_AR))
                Text(texto.replaceFirstChar { c -> c.uppercase() }, style = MaterialTheme.typography.bodyLarge)
            }

            if (!evento.venue.isNullOrBlank() || !evento.ciudad.isNullOrBlank()) {
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(listOfNotNull(evento.venue, evento.ciudad).joinToString(" · "))
                }
            }

            evento.precioDesde?.let { precio ->
                Text(
                    text = "Desde ${com.pamessacco.eventosapp.ui.components.formatearPrecio(precio, evento.moneda)}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 12.dp),
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Text(
                text = "Fuente: ${evento.fuente}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp),
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val url = evento.ticketUrl
                    if (!url.isNullOrBlank()) {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    }
                },
                enabled = !evento.ticketUrl.isNullOrBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Filled.ShoppingCart, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Comprar entradas")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = { agregarACalendario(context, evento) },
                enabled = evento.fechaInicio != null,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Filled.CalendarMonth, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Agregar a mi calendario")
            }
        }
    }
}

private fun agregarACalendario(context: android.content.Context, evento: EventItem) {
    val fecha = evento.fechaInicio ?: return
    val zona = ZoneId.of("America/Argentina/Buenos_Aires")
    val inicioMillis = fecha.atZone(zona).toInstant().toEpochMilli()
    val finMillis = fecha.plusHours(2).atZone(zona).toInstant().toEpochMilli()

    val intent = Intent(Intent.ACTION_INSERT).apply {
        data = android.provider.CalendarContract.Events.CONTENT_URI
        putExtra(android.provider.CalendarContract.Events.TITLE, evento.titulo)
        putExtra(android.provider.CalendarContract.Events.EVENT_LOCATION, evento.venue ?: "")
        putExtra(android.provider.CalendarContract.Events.DESCRIPTION, evento.ticketUrl ?: "")
        putExtra(android.provider.CalendarContract.EXTRA_EVENT_BEGIN_TIME, inicioMillis)
        putExtra(android.provider.CalendarContract.EXTRA_EVENT_END_TIME, finMillis)
        putExtra(android.provider.CalendarContract.Events.ALL_DAY, !evento.tieneHora)
    }
    context.startActivity(intent)
}
