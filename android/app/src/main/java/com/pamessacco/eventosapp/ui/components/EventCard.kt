package com.pamessacco.eventosapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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

private val horaFormatter = DateTimeFormatter.ofPattern("EEE d MMM · HH:mm", LOCALE_ES_AR)
private val fechaSinHoraFormatter = DateTimeFormatter.ofPattern("EEE d MMM", LOCALE_ES_AR)

@Composable
fun EventCard(evento: EventItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = evento.imagenUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.DarkGray),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = evento.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(4.dp))
                val fechaTexto = evento.fechaInicio?.let {
                    if (evento.tieneHora) it.format(horaFormatter) else it.format(fechaSinHoraFormatter)
                } ?: "Fecha a confirmar"
                Text(text = fechaTexto, style = MaterialTheme.typography.bodySmall)

                if (!evento.venue.isNullOrBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = listOfNotNull(evento.venue, evento.ciudad).joinToString(" · "),
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                evento.precioDesde?.let { precio ->
                    Text(
                        text = "Desde ${formatearPrecio(precio, evento.moneda)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

fun formatearPrecio(precio: Double, moneda: String): String {
    val entero = precio.toLong()
    val texto = "%,d".format(entero).replace(',', '.')
    return "$texto $moneda"
}
