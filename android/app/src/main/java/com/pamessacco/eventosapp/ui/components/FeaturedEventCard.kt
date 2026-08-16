package com.pamessacco.eventosapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.pamessacco.eventosapp.data.EventItem
import com.pamessacco.eventosapp.util.LOCALE_ES_AR
import java.time.format.DateTimeFormatter

private val fechaFormatter = DateTimeFormatter.ofPattern("EEE d MMM", LOCALE_ES_AR)

/** Card grande, vertical, para el carrusel de "Destacados" en la pantalla de Inicio. */
@Composable
fun FeaturedEventCard(evento: EventItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier.width(160.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column {
            AsyncImage(
                model = evento.imagenUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(Color.DarkGray),
            )
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = evento.titulo,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(4.dp))
                val fechaTexto = evento.fechaInicio?.format(fechaFormatter) ?: ""
                Text(text = fechaTexto, style = MaterialTheme.typography.bodySmall)
                if (!evento.venue.isNullOrBlank()) {
                    Text(
                        text = evento.venue,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
