package com.pamessacco.eventosapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pamessacco.eventosapp.sync.SyncScheduler
import com.pamessacco.eventosapp.ui.detail.EventDetailScreen
import com.pamessacco.eventosapp.ui.home.HomeScreen
import com.pamessacco.eventosapp.ui.search.SearchScreen
import com.pamessacco.eventosapp.ui.theme.EventosAppTheme

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

private data class Pestaña(val ruta: String, val etiqueta: String, val icono: androidx.compose.ui.graphics.vector.ImageVector)

private val PESTAÑAS = listOf(
    Pestaña("home", "Inicio", Icons.Filled.Home),
    Pestaña("search", "Buscar", Icons.Filled.Search),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventosNavHost() {
    val viewModel: EventsViewModel = viewModel()
    val navController: NavHostController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = backStackEntry?.destination

    val mostrarBarras = PESTAÑAS.any { p -> rutaActual?.hierarchy?.any { it.route == p.ruta } == true }

    Scaffold(
        topBar = {
            if (mostrarBarras) {
                TopAppBar(
                    title = { Text("Eventos") },
                    actions = {
                        IconButton(onClick = { viewModel.cargar() }) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Actualizar")
                        }
                    },
                )
            }
        },
        bottomBar = {
            if (mostrarBarras) {
                NavigationBar {
                    PESTAÑAS.forEach { pestaña ->
                        val seleccionada = rutaActual?.hierarchy?.any { it.route == pestaña.ruta } == true
                        NavigationBarItem(
                            selected = seleccionada,
                            onClick = {
                                navController.navigate(pestaña.ruta) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(pestaña.icono, contentDescription = pestaña.etiqueta) },
                            label = { Text(pestaña.etiqueta) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(padding),
        ) {
            composable("home") {
                HomeScreen(
                    viewModel = viewModel,
                    onEventoClick = { evento -> navController.navigate("detail/${evento.id}") },
                )
            }
            composable("search") {
                SearchScreen(
                    viewModel = viewModel,
                    onEventoClick = { evento -> navController.navigate("detail/${evento.id}") },
                )
            }
            composable("detail/{eventId}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("eventId")
                val evento = id?.let { viewModel.eventoPorId(it) }
                EventDetailScreen(evento = evento, onBack = { navController.popBackStack() })
            }
        }
    }
}
