package com.pamessacco.eventosapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pamessacco.eventosapp.sync.SyncScheduler
import com.pamessacco.eventosapp.ui.calendar.CalendarScreen
import com.pamessacco.eventosapp.ui.calendar.DayEventsScreen
import com.pamessacco.eventosapp.ui.detail.EventDetailScreen
import com.pamessacco.eventosapp.ui.theme.EventosAppTheme
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SyncScheduler.schedule(applicationContext)
        setContent {
            EventosAppTheme {
                EventosNavHost()
            }
        }
    }
}

@Composable
fun EventosNavHost() {
    val viewModel: EventsViewModel = viewModel()
    val navController: NavHostController = rememberNavController()

    NavHost(navController = navController, startDestination = "calendar") {
        composable("calendar") {
            CalendarScreen(
                viewModel = viewModel,
                onEventoClick = { evento -> navController.navigate("detail/${evento.id}") },
                onDiaClick = { dia -> navController.navigate("day/$dia") },
            )
        }
        composable("day/{fecha}") { backStackEntry ->
            val fecha = backStackEntry.arguments?.getString("fecha")?.let { LocalDate.parse(it) }
            if (fecha != null) {
                DayEventsScreen(
                    dia = fecha,
                    eventos = viewModel.eventosDelDia(fecha),
                    onBack = { navController.popBackStack() },
                    onEventoClick = { evento -> navController.navigate("detail/${evento.id}") },
                )
            }
        }
        composable("detail/{eventId}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("eventId")
            val evento = id?.let { viewModel.eventoPorId(it) }
            EventDetailScreen(evento = evento, onBack = { navController.popBackStack() })
        }
    }
}
