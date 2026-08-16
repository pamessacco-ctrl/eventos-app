package com.pamessacco.eventosapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val EventosRosa = Color(0xFFE9426D)
val EventosNavy = Color(0xFF1B1B3A)
val EventosNavyClaro = Color(0xFF2A2A55)
val EventosFondo = Color(0xFF121225)

private val EsquemaColores = darkColorScheme(
    primary = EventosRosa,
    onPrimary = Color.White,
    secondary = EventosNavyClaro,
    background = EventosFondo,
    surface = EventosNavyClaro,
    onBackground = Color.White,
    onSurface = Color.White,
)

@Composable
fun EventosAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EsquemaColores,
        typography = MaterialTheme.typography,
        content = content,
    )
}
