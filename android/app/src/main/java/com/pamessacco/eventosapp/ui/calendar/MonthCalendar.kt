package com.pamessacco.eventosapp.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pamessacco.eventosapp.ui.theme.EventosRosa
import com.pamessacco.eventosapp.util.LOCALE_ES_AR
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import kotlin.math.ceil

private val DIAS_SEMANA = listOf("L", "M", "M", "J", "V", "S", "D")

@Composable
fun MonthCalendar(
    mes: YearMonth,
    diasConEventos: Set<LocalDate>,
    onDiaClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hoy = LocalDate.now()
    val primerDiaMes = mes.atDay(1)
    val offset = primerDiaMes.dayOfWeek.value - 1 // Monday = 0
    val diasEnMes = mes.lengthOfMonth()
    val filas = ceil((offset + diasEnMes) / 7.0).toInt()

    Column(modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth()) {
            DIAS_SEMANA.forEach { d ->
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = d,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        for (fila in 0 until filas) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val numeroDia = fila * 7 + col - offset + 1
                    val fecha = if (numeroDia in 1..diasEnMes) mes.atDay(numeroDia) else null
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (fecha != null) {
                            DiaCelda(
                                fecha = fecha,
                                esHoy = fecha == hoy,
                                tieneEventos = fecha in diasConEventos,
                                onClick = { onDiaClick(fecha) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DiaCelda(
    fecha: LocalDate,
    esHoy: Boolean,
    tieneEventos: Boolean,
    onClick: () -> Unit,
) {
    val fondoColor = if (esHoy) EventosRosa.copy(alpha = 0.25f) else Color.Transparent
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(CircleShape)
            .background(fondoColor)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = fecha.dayOfMonth.toString(),
            fontWeight = if (esHoy) FontWeight.Bold else FontWeight.Normal,
        )
        if (tieneEventos) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(EventosRosa),
            )
        }
    }
}

fun YearMonth.nombreCapitalizado(): String {
    val nombre = this.month.getDisplayName(TextStyle.FULL, LOCALE_ES_AR)
    return "${nombre.replaceFirstChar { it.uppercase() }} ${this.year}"
}
